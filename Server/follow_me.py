import os
from flask import Flask, request, jsonify
import math
import json
import heapq
import mysql.connector

app = Flask(__name__)

# 비밀번호 파일에서 비밀번호 읽기
def get_db_password():
    try:
        # 현재 파일과 같은 경로에 있는 db_password.txt를 참조
        credentials_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "db_password.txt")
        with open(credentials_path, "r") as file:
            return file.read().strip()
    except FileNotFoundError:
        raise Exception("Database credentials file not found.")

# MySQL 데이터베이스 설정
db_config = {
    "host": "10.104.24.229",
    "user": "reactone",
    "password": get_db_password(),
    "database": "user_db"
}

# JSON 데이터 로드
with open("path_data.json", "r") as file:
    loaded_data = json.load(file)

# JSON 데이터 파싱
all_path_coordinates = [(coord[0], coord[1], coord[2]) for coord in loaded_data["coordinates"]]
manual_connections = [tuple(map(lambda c: (c[0], c[1], c[2]), conn)) for conn in loaded_data["connections"]]

# 거리 계산 함수 (층 차이 반영)
def haversine_distance(lat1, lon1, floor1, lat2, lon2, floor2):
    R = 6371  # 지구 반지름 (km)
    phi1, phi2 = map(math.radians, [lat1, lat2])
    d_phi = math.radians(lat2 - lat1)
    d_lambda = math.radians(lon2 - lon1)
    a = math.sin(d_phi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(d_lambda / 2) ** 2
    horizontal_distance = R * (2 * math.atan2(math.sqrt(a), math.sqrt(1 - a)))

    # 층 차이에 따른 수직 거리 추가 (층당 20m로 가정)
    vertical_distance = abs(floor2 - floor1) * 0.02  # 0.02 km = 20m

    # Pythagoras를 이용해 총 거리 계산
    return math.sqrt(horizontal_distance ** 2 + vertical_distance ** 2)

# Intermediate points 생성 함수
def generate_intermediate_points(p1, p2, max_points=40, min_points=30, min_distance=0.001, max_distance=0.1):
    """
    두 지점 사이에 중간 점들을 생성하여 경로를 더 촘촘하게 만듦.
    """
    distance = haversine_distance(p1[1], p1[2], p1[0], p2[1], p2[2], p2[0])
    if distance < min_distance:
        num_points = min_points
    elif distance > max_distance:
        num_points = max_points
    else:
        num_points = int(min_points + (max_points - min_points) * (distance - min_distance) / (max_distance - min_distance))
    lat_step = (p2[1] - p1[1]) / (num_points + 1)
    lon_step = (p2[2] - p1[2]) / (num_points + 1)
    return [(p1[0], p1[1] + i * lat_step, p1[2] + i * lon_step) for i in range(1, num_points + 1)]

# Dense path coordinates 생성 함수
def create_dense_path_coordinates(all_path_coordinates, manual_connections):
    """
    연결된 경로에서 촘촘한 노드 좌표를 생성하여 그래프의 밀도를 증가시킴.
    """
    dense_coordinates = set(all_path_coordinates)
    for p1, p2 in manual_connections:
        intermediate_points = generate_intermediate_points(p1, p2)
        dense_coordinates.update(intermediate_points)
    return list(dense_coordinates)

# Dijkstra 알고리즘
def dijkstra(graph, start):
    distances = {node: float('inf') for node in graph}
    distances[start] = 0
    priority_queue = [(0, start)]
    previous_nodes = {node: None for node in graph}
    while priority_queue:
        current_distance, current_node = heapq.heappop(priority_queue)
        if current_distance > distances[current_node]:
            continue
        for neighbor, weight in graph[current_node].items():
            distance = current_distance + weight
            if distance < distances[neighbor]:
                distances[neighbor] = distance
                previous_nodes[neighbor] = current_node
                heapq.heappush(priority_queue, (distance, neighbor))
    return distances, previous_nodes

# 최적 방문 순서 계산 함수
def nearest_neighbor_optimization(start, targets, graph):
    unvisited = set(targets)
    current = start
    order = [start]

    while unvisited:
        nearest = min(unvisited, key=lambda target: haversine_distance(
            current[1], current[2], current[0], target[1], target[2], target[0]
        ))
        order.append(nearest)
        unvisited.remove(nearest)
        current = nearest

    return order

# Dense path coordinates와 그래프 빌드
dense_path_coordinates = create_dense_path_coordinates(all_path_coordinates, manual_connections)
path_graph = {point: {} for point in dense_path_coordinates}
for p1, p2 in manual_connections:
    intermediate_points = generate_intermediate_points(p1, p2)
    all_points = [p1] + intermediate_points + [p2]
    for i in range(len(all_points) - 1):
        distance = haversine_distance(
            all_points[i][1], all_points[i][2], all_points[i][0],  # p1
            all_points[i + 1][1], all_points[i + 1][2], all_points[i + 1][0]  # p2
        )
        path_graph[all_points[i]][all_points[i + 1]] = distance
        path_graph[all_points[i + 1]][all_points[i]] = distance

@app.route('/optimal-path', methods=['POST'])
def optimal_path():
    data = request.json
    print("Received data:", data)
    start_point = (data["start"][0], data["start"][1], data["start"][2])
    brand_list = data.get("brands")  # 클라이언트가 요청한 브랜드 리스트

    # MySQL 연결 및 브랜드에 해당하는 좌표 가져오기
    try:
        conn = mysql.connector.connect(**db_config)
        cursor = conn.cursor(dictionary=True)

        # 브랜드 리스트 기반 좌표 및 층 정보 조회
        format_strings = ','.join(['%s'] * len(brand_list))
        query = f"SELECT brand, floor, coordinate FROM brand_info WHERE brand IN ({format_strings})"
        cursor.execute(query, brand_list)

        brand_coordinates = []
        brand_mapping = {}
        found_brands = set()

        for row in cursor.fetchall():
            # 좌표 데이터가 None이 아닌 경우에만 파싱
            if row['coordinate'] and row['coordinate'].strip():
                try:
                    lat, lon = map(float, row['coordinate'].split(','))  # 위도와 경도 파싱
                    floor = int(row['floor'])  # 층 정보 파싱
                    brand_coordinates.append((floor, lat, lon))
                    brand_mapping[row['brand']] = (floor, lat, lon)
                    found_brands.add(row['brand'])
                except (ValueError, AttributeError):
                    print(f"Warning: Invalid coordinate format for brand '{row['brand']}': {row['coordinate']}")
            else:
                print(f"Warning: No coordinates found for brand '{row['brand']}'")

    except mysql.connector.Error as err:
        return jsonify({"error": str(err)}), 500
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

    # 좌표가 없는 브랜드를 제외한 브랜드 리스트 확인
    missing_brands = [brand for brand in brand_list if brand not in found_brands]
    if missing_brands:
        print(f"Warning: No coordinates found for these brands: {missing_brands}")

    # 조회된 좌표가 없을 경우 에러 반환
    if not brand_coordinates:
        return jsonify({"error": "제공된 브랜드에 해당하는 좌표를 찾을 수 없습니다."}), 404

    # 시작 좌표를 같은 층의 가장 가까운 그래프 노드로 매핑
    start_node = min(
        [node for node in dense_path_coordinates if node[0] == start_point[0]],  # 같은 층의 노드만 고려
        key=lambda node: haversine_distance(
            start_point[1], start_point[2], start_point[0], node[1], node[2], node[0]
        )
    )

    # 브랜드 좌표를 같은 층의 가장 가까운 그래프 노드로 매핑
    target_nodes = [
        min(
            [node for node in dense_path_coordinates if node[0] == coord[0]],  # 같은 층의 노드만 고려
            key=lambda node: haversine_distance(
                coord[1], coord[2], coord[0], node[1], node[2], node[0]
            )
        )
        for coord in brand_coordinates
    ]

    # 최적 방문 순서 계산
    visiting_order = nearest_neighbor_optimization(start_node, target_nodes, path_graph)

    # 방문 순서에 따라 경로 계산
    full_path = []
    for i in range(len(visiting_order) - 1):
        start = visiting_order[i]
        end = visiting_order[i + 1]
        _, previous_nodes = dijkstra(path_graph, start)

        # Dijkstra를 통해 구한 경로 추가
        path_segment = []
        current = end
        while current is not None:
            path_segment.append(current)
            current = previous_nodes[current]
        full_path.extend(path_segment[::-1])  # 역순으로 추가

    # 중복 제거
    optimized_path = [full_path[i] for i in range(len(full_path)) if i == 0 or full_path[i] != full_path[i - 1]]

    print("Optimized Path:", optimized_path)
    print("Brand Mapping:", brand_mapping)

    return jsonify({
        "path": optimized_path,
        "brands": brand_mapping,       # 브랜드 이름과 좌표 매핑 결과 반환
    })
\

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)