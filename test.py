import logging
from flask import Flask, request, jsonify
import pandas as pd
import joblib
from flask_cors import CORS

# Flask 애플리케이션 로깅 설정
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger()

# 모델 로드
model_escalator_path = 'model_escalator.pkl'
model_direction_path = 'model_direction.pkl'

try:
    escalator_model = joblib.load(model_escalator_path)
    logger.info("Escalator model loaded successfully from %s", model_escalator_path)
    direction_model = joblib.load(model_direction_path)
    logger.info("Direction model loaded successfully from %s", model_direction_path)
except Exception as e:
    logger.error("Failed to load models: %s", e)
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

def map_direction_to_text(predictions):
    """
    Map string direction predictions to human-readable text.
    """
    mapping = {
        0: "위로 이동",
        1: "아래로 이동"
    }
    return [mapping.get(pred, "알 수 없음") for pred in predictions]


def detect_escalator(data):
    """
    Perform prediction using the trained escalator model.
    """
    required_columns = ['gyroX', 'gyroY', 'gyroZ', 'accelX', 'accelY', 'accelZ']
    if not all(col in data.columns for col in required_columns):
        error_message = f"Input data must contain the following columns: {required_columns}"
        logger.error(error_message)
        return {"error": error_message}, 400

    try:
        logger.debug("Input DataFrame: %s", data)
        predictions = escalator_model.predict(data[required_columns])
        if isinstance(predictions, tuple):
            predictions = list(predictions)  # 튜플을 리스트로 변환
        
        # 예측 결과를 텍스트로 변환
        predictions_text = map_predictions_to_text(predictions)
        logger.info("Prediction completed successfully with results: %s", predictions_text)
        return predictions, predictions_text
    except Exception as e:
        logger.error("Error during prediction: %s", e)
        raise e

def predict_direction(data):
    """
    Perform direction prediction using the trained direction model.
    """
    try:
        logger.debug("Input DataFrame for direction prediction: %s", data)
        # 예측 수행
        predictions = direction_model.predict(data.values)
        logger.debug("Raw predictions from direction model: %s", predictions)
        
        # 문자열 매핑
        predictions_text = map_direction_to_text(predictions)
        logger.info("Direction prediction completed successfully with results: %s", predictions_text)
        return predictions_text
    except Exception as e:
        logger.error("Error during direction prediction: %s", e)
        raise e

@app.route('/predict', methods=['POST'])
def predict():
    """
    Handle POST requests to perform predictions.
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
        escalator_predictions, escalator_text = detect_escalator(df)

        # 방향 예측 (탑승 중일 경우만 수행)
        if 1 in escalator_predictions:  # '탑승 중'일 때만 방향 예측
            direction_predictions = predict_direction(df)
        else:
            direction_predictions = ["N/A"]

        # 응답 작성
        response = {
            "escalator_predictions": escalator_text,
            "direction_predictions": direction_predictions
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
    app.run(host='0.0.0.0', port=5000, debug=True)
