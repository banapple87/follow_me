package com.example.follow_me.network;

import com.example.follow_me.data.PredictionResponse;
import com.example.follow_me.data.SensorData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PredictionAPI {
    @POST("/predict")
    Call<PredictionResponse> predict(@Body SensorData sensorData);
}