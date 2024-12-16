import os
from flask import Flask, request, jsonify
import mysql.connector
from mysql.connector import Error

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

# MySQL 데이터베이스 연결 정보
DB_CONFIG = {
    "host": "10.104.24.229",
    "user": "reactone",
    "password": get_db_password(),
    "database": "user_db"
}

# 나이에 맞는 연령대 반환
def get_age_group(age):
    """나이에 맞는 연령대 반환"""
    age = int(age)
    if 10 <= age < 20:
        return "10대"
    elif 20 <= age < 30:
        return "20대"
    elif 30 <= age < 40:
        return "30대"
    elif 40 <= age < 50:
        return "40대"
    elif 50 <= age < 60:
        return "50대"
    elif 60 <= age < 70:
        return "60대"
    elif 70 <= age < 80:
        return "70대"
    elif age >= 80:
        return "80대"
    return None

@app.route('/submitData', methods=['POST'])
def submit_data():
    try:
        # JSON 데이터 받기
        data = request.get_json()

        # 데이터에서 개별 항목 추출
        gender = data.get('gender')
        age = data.get('age')
        category = data.get('category')
        styles = data.get('styles')

        # 성별 가공 (Male -> 남성, Female -> 여성)
        gender_map = {
            "Male": "남성",
            "Female": "여성"
        }
        gender = gender_map.get(gender, gender)

        # 스타일 파싱 및 # 제거
        if isinstance(styles, str):
            styles = styles.strip('[]').replace(' ', '').replace("'", "").split(',')
        if isinstance(styles, list):
            styles = [style.lstrip('#').strip() for style in styles]

        # 나이대 범위 설정
        age_group = get_age_group(age)

        # 스타일 조건 생성 (AND로 결합)
        style_query = ""
        if styles:
            style_conditions = " AND ".join([f"(style1 = '{style}' OR style2 = '{style}' OR style3 = '{style}')" for style in styles])
            style_query = f"AND ({style_conditions})"

        # SQL 쿼리 생성
        query = f"""
            SELECT brand
            FROM brand_info
            WHERE category = '{category}'
            AND (gender1 = '{gender}' OR gender2 = '{gender}')
            {style_query}
            {"AND (age1 = '" + age_group + "' OR age2 = '" + age_group + "' OR age3 = '" + age_group + "' OR age4 = '" + age_group + "' OR age5 = '" + age_group + "' OR age6 = '" + age_group + "' OR age7 = '" + age_group + "' OR age8 = '" + age_group + "')" if age_group else ""}
        """

        # 쿼리 출력 (터미널에서 확인)
        print("Generated SQL Query:")
        print(query)

        # 데이터베이스 연결 및 쿼리 실행
        connection = mysql.connector.connect(**DB_CONFIG)
        cursor = connection.cursor(dictionary=True)
        cursor.execute(query)
        results = cursor.fetchall()

        # 결과 확인
        brands = [row['brand'] for row in results]

        # 터미널에 결과 출력
        print("Query Results:")
        print(brands)

        # 연결 종료
        cursor.close()
        connection.close()

        # 결과 반환
        return jsonify({"brands": brands}), 200

    except Error as e:
        print(f"MySQL Error: {e}")
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        print(f"General Error: {e}")
        return jsonify({"error": str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5003, debug=True)