package com.example.myapplication;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.PredictionResponse;
import com.example.myapplication.data.SensorData;
import com.example.myapplication.network.PredictionAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SensorPredictionActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "SensorPredictionActivity";
    private TextView predictionResult;
    private Retrofit retrofit;
    private PredictionAPI api;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;
    private Sensor accelerometerSensor;

    private float[] gyroscopeData = new float[3];
    private float[] accelerometerData = new float[3];

    private Handler handler;
    private Runnable dataSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_prediction);

        predictionResult = findViewById(R.id.prediction_result);

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
        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.104.24.229:5001") // Flask 서버 주소
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        api = retrofit.create(PredictionAPI.class);

        // 센서 설정
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_UI);
        }
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
        }

        // 1초마다 센서 데이터 전송
        handler = new Handler();
        dataSender = new Runnable() {
            @Override
            public void run() {
                sendSensorData();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(dataSender);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        handler.removeCallbacks(dataSender);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroscopeData = Arrays.copyOf(event.values, event.values.length);
            Log.d(TAG, "Gyroscope: " + Arrays.toString(gyroscopeData));
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerData = Arrays.copyOf(event.values, event.values.length);
            Log.d(TAG, "Accelerometer: " + Arrays.toString(accelerometerData));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 필요시 구현
    }

    private void sendSensorData() {
        float time = System.currentTimeMillis() / 1000f;
        SensorData sensorData = new SensorData(time, gyroscopeData, accelerometerData);

        api.predict(sensorData).enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PredictionResponse prediction = response.body();
                    String result = formatPredictionResult(prediction);
                    predictionResult.setText(result);
                } else {
                    Log.e(TAG, "Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                Log.e(TAG, "Failure: " + t.getMessage());
                predictionResult.setText("Failed to connect to server.");
            }
        });
    }

    private String formatPredictionResult(PredictionResponse prediction) {
        String[] predictions = prediction.getEscalatorPredictions();
        Map<String, String> probabilities = prediction.getProbabilities();

        String result = "Prediction: " + (predictions != null ? predictions[0] : "N/A") + "\n";
        result += "Walking Probability: " + probabilities.getOrDefault("걷는 중 (walking)", "N/A") + "\n";
        result += "Escalator Probability: " + probabilities.getOrDefault("탑승 중 (escalator)", "N/A");
        return result;
    }
}
