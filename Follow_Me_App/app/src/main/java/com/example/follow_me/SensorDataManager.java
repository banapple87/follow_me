package com.example.follow_me;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import com.example.follow_me.data.PredictionResponse;
import com.example.follow_me.data.SensorData;
import com.example.follow_me.network.PredictionAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SensorDataManager implements SensorEventListener {

    private static final String TAG = "SensorDataManager";
    private final SensorManager sensorManager;
    private final Sensor gyroscopeSensor;
    private final Sensor accelerometerSensor;
    private final Sensor barometerSensor; // 바로미터 센서 추가

    private final float[] gyroscopeData = new float[3];
    private final float[] accelerometerData = new float[3];
    private float lastPressure = -1;
    private final float PRESSURE_TO_ALTITUDE_RATIO = 8.3f; // 1 hPa ≈ 8.3m 고도 변화
    private final float ALTITUDE_THRESHOLD = 1.0f;         // 누적 고도 변화 임계값 (1.0m)
    private float cumulativeAltitudeChange = 0.0f;         // 누적 고도 변화 변수

    private final PredictionAPI api;
    private final Handler handler = new Handler();
    private final Runnable dataSender;
    private final TextView predictionResult;

    public SensorDataManager(Context context, TextView predictionResult) {
        this.predictionResult = predictionResult;

        // SensorManager 설정
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        barometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE); // 바로미터 센서 초기화

        // 바로미터 센서 지원 여부 확인
        if (barometerSensor == null) {
            Log.e(TAG, "Pressure sensor not available on this device");
        } else {
            Log.d(TAG, "Pressure sensor initialized successfully");
        }

        // Retrofit 설정
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                //.addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.104.24.229:5001") // Flask 서버 IP 주소
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        api = retrofit.create(PredictionAPI.class);

        // 센서 등록
        registerSensors();

        // 1초마다 데이터 전송
        dataSender = new Runnable() {
            @Override
            public void run() {
                sendSensorData();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(dataSender);
    }

    private void registerSensors() {
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (barometerSensor != null) {
            sensorManager.registerListener(this, barometerSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stopSensorUpdates() {
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(dataSender);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, gyroscopeData, 0, event.values.length);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerData, 0, event.values.length);
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            Log.d(TAG, "Pressure sensor value: " + event.values[0] + " hPa"); // 압력 값 로그 출력
            handlePressureChange(event.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서 정확도 변경 처리 (필요 시 구현)
    }

    // 바로미터 센서의 압력 변화 처리
    private void handlePressureChange(float pressure) {
        if (lastPressure != -1) {
            // 고도 변화 계산
            double altitudeChange = (lastPressure - pressure) * PRESSURE_TO_ALTITUDE_RATIO;

            // 누적 고도 변화 업데이트
            cumulativeAltitudeChange += altitudeChange;

            Log.d(TAG, "Altitude change: " + altitudeChange + " m, Cumulative change: " + cumulativeAltitudeChange + " m");

            // 누적 고도 변화가 ±1.0m 이상일 경우 층 이동
            if (cumulativeAltitudeChange >= ALTITUDE_THRESHOLD) {
                Log.d(TAG, "Moving up to higher floor");
                ((MainActivity) predictionResult.getContext()).moveToHigherFloor();
                cumulativeAltitudeChange = 0.0f; // 누적값 초기화
            } else if (cumulativeAltitudeChange <= -ALTITUDE_THRESHOLD) {
                Log.d(TAG, "Moving down to lower floor");
                ((MainActivity) predictionResult.getContext()).moveToLowerFloor();
                cumulativeAltitudeChange = 0.0f; // 누적값 초기화
            }
        }

        lastPressure = pressure;
    }

    private void sendSensorData() {
        float time = System.currentTimeMillis() / 1000f;
        SensorData sensorData = new SensorData(time, gyroscopeData, accelerometerData);

        api.predict(sensorData).enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PredictionResponse predictionResponse = response.body();
                    String[] predictions = predictionResponse.getEscalatorPredictions();
                    String result = "Prediction: " + Arrays.toString(predictions);
                    predictionResult.setText(result);

                    if (Arrays.toString(predictions).contains("걷는 중")) {
                        ((MainActivity) predictionResult.getContext()).updatePrediction("걷는 중");
                    } else if (Arrays.toString(predictions).contains("탑승 중")) {
                        ((MainActivity) predictionResult.getContext()).updatePrediction("탑승 중");
                    } else {
                        ((MainActivity) predictionResult.getContext()).updatePrediction("정지");
                    }
                } else {
                    predictionResult.setText("Server Error: " + response.code());
                    ((MainActivity) predictionResult.getContext()).updatePrediction("정지");
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                predictionResult.setText("Connection Failed: " + t.getMessage());
                ((MainActivity) predictionResult.getContext()).updatePrediction("정지");
            }
        });
    }
}