import os
from flask import Flask, request, jsonify
import mysql.connector
from mysql.connector import Error
from sklearn.metrics.pairwise import cosine_similarity
import pandas as pd
import logging

app = Flask(__name__)

# 로깅 설정
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')

# 비밀번호 파일에서 비밀번호 읽기
def get_db_password():
    try:
        credentials_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "db_password.txt")
        with open(credentials_path, "r") as file:
            return file.read().strip()
    except FileNotFoundError:
        logging.error("Database credentials file not found.")
        raise Exception("Database credentials file not found.")

# MySQL 데이터베이스 연결 정보
DB_CONFIG = {
    "host": "10.104.24.229",
    "user": "reactone",
    "password": get_db_password(),
    "database": "user_db"
}

# MySQL에서 사용자의 방문 기록 가져오기
def get_visited_stores(user_id):
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        
        # 컬럼 이름을 'id'와 'store'로 수정
        query = "SELECT store FROM user_visited_stores WHERE id = %s"
        cursor.execute(query, (user_id,))
        visited_stores = [row['store'] for row in cursor.fetchall()]
        
        logging.debug(f"User {user_id} visited stores: {visited_stores}")
        return visited_stores
    except Error as e:
        logging.error(f"Database error: {e}")
        return []
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

# MySQL에서 brand_info 데이터 가져오기 (의류 카테고리만)
def get_brand_info():
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        
        query = "SELECT * FROM brand_info WHERE category = '의류'"
        cursor.execute(query)
        brand_data = cursor.fetchall()
        
        # logging.debug(f"Fetched brand_info data: {brand_data}")
        return pd.DataFrame(brand_data)
    except Error as e:
        logging.error(f"Database error: {e}")
        return pd.DataFrame()
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

# 협업 필터링 추천 (의류 카테고리만)
def collaborative_filtering(user_id):
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        
        # 모든 사용자의 방문 기록 가져오기
        query = """
        SELECT id, store 
        FROM user_visited_stores 
        WHERE store IN (SELECT brand FROM brand_info WHERE category = '의류')
        """
        cursor.execute(query)
        data = cursor.fetchall()
        
        # 데이터프레임 생성 및 ID를 정수형으로 변환
        df = pd.DataFrame(data)
        if df.empty:
            logging.warning("No user-visited data found for clothing stores.")
            return []
        
        df['id'] = df['id'].astype(int)  # 사용자 ID를 정수형으로 변환
        logging.debug(f"User-Store DataFrame after conversion:\n{df}")

        # 사용자-매장 매트릭스 생성
        user_store_matrix = df.pivot_table(index='id', columns='store', aggfunc='size', fill_value=0)
        logging.debug(f"User-Store Matrix:\n{user_store_matrix}")
        logging.debug(f"User-Store Matrix index type: {user_store_matrix.index.dtype}")

        user_id = int(user_id)  # user_id를 정수형으로 변환

        if user_id not in user_store_matrix.index:
            logging.warning(f"User ID {user_id} not found in user-store matrix.")
            return []

        # 사용자 간 코사인 유사도 계산
        similarity_matrix = cosine_similarity(user_store_matrix)
        similarity_df = pd.DataFrame(similarity_matrix, index=user_store_matrix.index, columns=user_store_matrix.index)

        # 유사한 사용자 찾기
        similar_users = similarity_df[user_id].sort_values(ascending=False).drop(user_id).head(3).index
        logging.debug(f"Top similar users to {user_id}: {similar_users}")

        # 유사 사용자가 방문한 매장 추천
        recommendations = []
        for similar_user in similar_users:
            similar_user_visited = df[df['id'] == similar_user]['store'].tolist()
            recommendations.extend(similar_user_visited)

        # 중복 제거 및 이미 방문한 매장 제외
        recommendations = list(set(recommendations) - set(df[df['id'] == user_id]['store'].tolist()))
        logging.debug(f"Collaborative recommendations for user {user_id}: {recommendations}")
        
        return recommendations

    except Error as e:
        logging.error(f"Database error: {e}")
        return []
    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()

# 콘텐츠 기반 추천 (의류 카테고리만)
def content_based_filtering(visited_stores):
    # brand_info 데이터 가져오기 (의류만)
    brand_df = get_brand_info()
    
    if brand_df.empty:
        return []

    # 방문한 매장의 특성 가져오기
    visited_df = brand_df[brand_df['brand'].isin(visited_stores)]
    logging.debug(f"Visited stores DataFrame:\n{visited_df}")
    
    if visited_df.empty:
        logging.warning("No visited stores found in clothing category.")
        return []

    # TF-IDF 벡터화 (style1만 사용)
    from sklearn.feature_extraction.text import TfidfVectorizer
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(brand_df['style1'].fillna(""))
    
    # 방문한 매장과 다른 매장 간의 유사도 계산
    cosine_sim = cosine_similarity(tfidf_matrix, tfidf_matrix)
    cosine_sim_df = pd.DataFrame(cosine_sim, index=brand_df['brand'], columns=brand_df['brand'])
    
    # 방문한 매장과 유사한 매장 추천 (상위 5개)
    recommendations = set()
    for store in visited_stores:
        if store in cosine_sim_df.index:
            similar_stores = cosine_sim_df[store].sort_values(ascending=False).drop(store).head(1).index
            recommendations.update(similar_stores)
    
    # 중복 제거 및 리스트로 변환
    recommendations = list(recommendations)
    logging.debug(f"Content-based recommendations: {recommendations}")
    
    return recommendations

# 추천 엔드포인트
@app.route('/recommend', methods=['POST'])
def recommend():
    data = request.json
    user_id = data.get("user_id")
    
    logging.info(f"Received request for user_id: {user_id}")
    
    if not user_id:
        return jsonify({"error": "User ID is required"}), 400
    
    # 방문 기록 가져오기
    visited_stores = get_visited_stores(user_id)
    
    if not visited_stores:
        logging.warning(f"No visited stores found for user {user_id}")
        return jsonify({"error": "No visited stores found for this user."}), 404
    
    # 협업 필터링 추천
    collaborative_recommendations = collaborative_filtering(user_id)
    
    # 콘텐츠 기반 추천
    content_based_recommendations = content_based_filtering(visited_stores)
    
    response = {
        "visited_stores": visited_stores,
        "collaborative_recommendations": collaborative_recommendations,
        "content_based_recommendations": content_based_recommendations
    }
    
    logging.info(f"Response for user_id {user_id}: {response}")
    return jsonify(response)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5004, debug=True)
