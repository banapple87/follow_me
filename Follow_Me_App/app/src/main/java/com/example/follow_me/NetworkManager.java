package com.example.follow_me;

import android.util.Log;
import com.naver.maps.geometry.LatLng;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.*;

public class NetworkManager {
    private static final String TAG = "NetworkManager";
    private static final String BASE_URL = "http://10.104.24.229:5000/optimal-path";
    private final OkHttpClient client = new OkHttpClient();

    public void fetchOptimalPath(LatLng start, int floor, List<String> brands,
                                 Map<Integer, List<LatLng>> pathCoordinatesByFloor,
                                 Map<Integer, List<BrandMarker>> brandMarkersByFloor,
                                 Runnable onComplete) {
        try {
            JSONArray brandArray = new JSONArray();
            for (String brand : brands) {
                brandArray.put(brand);
            }

            JSONArray startArray = new JSONArray();
            startArray.put(floor);
            startArray.put(start.latitude);
            startArray.put(start.longitude);

            JSONObject requestData = new JSONObject();
            requestData.put("start", startArray);
            requestData.put("brands", brandArray);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"), requestData.toString());

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Request failed", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());

                            // Parse paths
                            JSONArray pathArray = jsonResponse.getJSONArray("path");
                            pathCoordinatesByFloor.clear();
                            for (int i = 0; i < pathArray.length(); i++) {
                                JSONArray coord = pathArray.getJSONArray(i);
                                int floor = coord.getInt(0);
                                double lat = coord.getDouble(1);
                                double lng = coord.getDouble(2);

                                pathCoordinatesByFloor
                                        .computeIfAbsent(floor, k -> new ArrayList<>())
                                        .add(new LatLng(lat, lng));
                            }

                            // Parse brands
                            JSONObject brandData = jsonResponse.getJSONObject("brands");
                            brandMarkersByFloor.clear();
                            for (String brand : brands) {
                                if (brandData.has(brand)) {
                                    JSONArray coord = brandData.getJSONArray(brand);
                                    int floor = coord.getInt(0);
                                    double lat = coord.getDouble(1);
                                    double lng = coord.getDouble(2);

                                    brandMarkersByFloor
                                            .computeIfAbsent(floor, k -> new ArrayList<>())
                                            .add(new BrandMarker(brand, new LatLng(lat, lng)));
                                }
                            }

                            onComplete.run();
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing response", e);
                        }
                    } else {
                        Log.e(TAG, "Request failed with code: " + response.code());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
        }
    }
}