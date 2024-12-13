import logging
from flask import Flask, request, jsonify
import pandas as pd
import joblib
from flask_cors import CORS
import os

# Flask 애플리케이션 로깅 설정
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger()

# 모델 경로 설정 (한 디렉토리 위에 있는 Model 폴더에 있는 경우)
model_path = os.path.join(os.path.dirname(__file__), '..', 'Model', 'random_forest_model.pkl')

try:
    escalator_model = joblib.load(model_path)
    logger.info("Model loaded successfully from %s", model_path)
except Exception as e:
    logger.error("Failed to load model: %s", e)
    raise e

app = Flask(__name__)
CORS(app)

def convert_input_data(input_json):
    """
    Convert the input JSON data to the expected DataFrame format.
    """
    try:
        gyroscope = input_json['gyroscope']
        accelerometer = input_json['accelerometer']
        
        # 예상되는 데이터프레임 생성
        df = pd.DataFrame([{
            "gyroX": gyroscope[0],
            "gyroY": gyroscope[1],
            "gyroZ": gyroscope[2],
            "accelX": accelerometer[0],
            "accelY": accelerometer[1],
            "accelZ": accelerometer[2]
        }])
        logger.debug("Converted input JSON to DataFrame: %s", df)
        return df
    except KeyError as e:
        error_message = f"Missing key in input JSON: {str(e)}"
        logger.error(error_message)
        raise ValueError(error_message)

def map_predictions_to_text(predictions):
    """
    Map numeric predictions to human-readable text.
    """
    mapping = {
        0: "걷는 중",
        1: "탑승 중"
    }
    return [mapping.get(pred, "알 수 없음") for pred in predictions]

@app.route('/predict', methods=['POST'])
def predict():
    """
    Handle POST requests to perform predictions and return probabilities.
    """
    try:
        logger.info("Received a POST request on /predict")
        logger.debug("Request headers: %s", request.headers)
        logger.debug("Request remote address: %s", request.remote_addr)

        input_data = request.get_json()
        logger.debug("Received JSON data: %s", input_data)

        # 데이터 변환
        df = convert_input_data(input_data)

        # 에스컬레이터 예측 실행
        probabilities = escalator_model.predict_proba(df)  # 각 클래스의 확률
        predictions = escalator_model.predict(df)  # 최종 예측

        # 확률 정보
        probability_text = {
            "걷는 중 (walking)": f"{probabilities[0][0] * 100:.2f}%",
            "탑승 중 (escalator)": f"{probabilities[0][1] * 100:.2f}%"
        }

        # 최종 예측 텍스트 변환
        predictions_text = map_predictions_to_text(predictions)

        # 응답 작성
        response = {
            "escalator_predictions": predictions_text,
            "probabilities": probability_text
        }
        logger.info("Response sent: %s", response)
        return jsonify(response)

    except ValueError as e:
        logger.error("Validation Error: %s", e)
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        logger.error("Error in /predict endpoint: %s", e)
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)