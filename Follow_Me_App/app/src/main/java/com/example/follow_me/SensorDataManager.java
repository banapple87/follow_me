package com.example.follow_me;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

    private final float[] gyroscopeData = new float[3];
    private final float[] accelerometerData = new float[3];

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

        // Retrofit 설정
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
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
    }

    public void stopSensorUpdates() {
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(dataSender);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(event.values, 0, gyroscopeData, 0, event.values.length);
            Log.d(TAG, "Gyroscope data: " + Arrays.toString(gyroscopeData));
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerData, 0, event.values.length);
            Log.d(TAG, "Accelerometer data: " + Arrays.toString(accelerometerData));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서 정확도 변경 처리 (필요 시 구현)
    }

    private void sendSensorData() {
        float time = System.currentTimeMillis() / 1000f;
        SensorData sensorData = new SensorData(time, gyroscopeData, accelerometerData);

        Log.d(TAG, "Sending sensor data: " + sensorData.toString());

        api.predict(sensorData).enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PredictionResponse predictionResponse = response.body();
                    String result = "Prediction: " + Arrays.toString(predictionResponse.getEscalatorPredictions());
                    predictionResult.setText(result);
                } else {
                    predictionResult.setText("Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                predictionResult.setText("Connection Failed: " + t.getMessage());
            }
        });
    }
}
