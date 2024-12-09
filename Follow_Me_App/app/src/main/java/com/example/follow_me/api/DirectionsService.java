package com.example.follow_me.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface DirectionsService {
    @GET("/map-direction/v1/driving")
    Call<ResponseBody> getDrivingRoute(
            @Query("start") String start,
            @Query("goal") String goal,
            @Query("option") String option,
            @Header("X-NCP-APIGW-API-KEY-ID") String clientId,
            @Header("X-NCP-APIGW-API-KEY") String clientSecret
    );
}
