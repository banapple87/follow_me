package com.example.follow_me;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapView;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.indoor.IndoorLevel;
import com.naver.maps.map.indoor.IndoorSelection;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolylineOverlay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private NaverMap naverMap;
    private LatLng startPoint = null;
    private static final String TAG = "MapClick";

    private final List<Marker> markers = new ArrayList<>();
    private final List<PolylineOverlay> polylines = new ArrayList<>();

    private final Map<Integer, List<LatLng>> pathCoordinatesByFloor = new HashMap<>();
    private final Map<Integer, List<BrandMarker>> brandMarkersByFloor = new HashMap<>();

    private String lastSelectedFloor = null;
    private int currentFloor = -1;
    private final android.os.Handler floorUpdateHandler = new android.os.Handler();

    private final List<String> brandList = List.of("A.P.C 골프", "Lee", "노스페이스", "톰그레이하운드", "더캐시미어", "클럽모나코", "준지", "지포어", "헤지스키즈", "네파키즈", "뉴발란스", "나이키", "닥스종합관", "내셔널지오그래픽키즈", "디아도라", "캘빈클라인진", "리바이스", "노르디스크");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        findViewById(R.id.calculate_path_button).setOnClickListener(v -> calculateAndDrawPath());
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        naverMap.setIndoorEnabled(true);

        LatLng initialLocation = new LatLng(35.192, 129.213);
        CameraUpdate scrollUpdate = CameraUpdate.scrollTo(initialLocation).animate(CameraAnimation.Easing);
        naverMap.moveCamera(scrollUpdate);

        new android.os.Handler().postDelayed(() -> {
            CameraUpdate zoomUpdate = CameraUpdate.zoomTo(17).animate(CameraAnimation.Easing);
            naverMap.moveCamera(zoomUpdate);
        }, 300);

        naverMap.setOnMapClickListener((point, coord) -> {
            if (startPoint == null) {
                startPoint = coord;
                addMarkerAtLocation(coord, "Start Point");
            }
        });

        startFloorUpdateChecker();
    }

    private void startFloorUpdateChecker() {
        floorUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (naverMap != null) {
                    IndoorSelection selection = naverMap.getIndoorSelection();
                    if (selection != null) {
                        IndoorLevel level = selection.getLevel();
                        String detectedFloor = level != null ? level.getName() : null;

                        if (detectedFloor != null && !detectedFloor.equals(lastSelectedFloor)) {
                            lastSelectedFloor = detectedFloor;
                            Log.d(TAG, "Floor changed to: " + detectedFloor);

                            currentFloor = parseFloorNumber(detectedFloor);
                            clearMarkersAndPolylines();
                            updateMapForCurrentFloor();
                        }
                    }
                }
                floorUpdateHandler.postDelayed(this, 500);
            }
        }, 500);
    }

    private int parseFloorNumber(String floorName) {
        try {
            if (floorName.startsWith("B")) {
                return -Integer.parseInt(floorName.replaceAll("[^0-9]", ""));
            } else {
                return Integer.parseInt(floorName.replaceAll("[^0-9]", ""));
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing floor name: " + floorName, e);
            return -1;
        }
    }

    private void clearMarkersAndPolylines() {
        for (Marker marker : markers) {
            marker.setMap(null);
        }
        markers.clear();

        for (PolylineOverlay polyline : polylines) {
            polyline.setMap(null);
        }
        polylines.clear();
    }

    private void updateMapForCurrentFloor() {
        if (pathCoordinatesByFloor.containsKey(currentFloor)) {
            drawPathSequentially(pathCoordinatesByFloor.get(currentFloor));
        } else {
            Log.d(TAG, "No path data for current floor.");
        }

        if (brandMarkersByFloor.containsKey(currentFloor)) {
            displayBrandMarkers(brandMarkersByFloor.get(currentFloor));
        } else {
            Log.d(TAG, "No brand markers for current floor.");
        }
    }

    private void addMarkerAtLocation(LatLng coord, String caption) {
        Marker marker = new Marker();
        marker.setPosition(coord);
        marker.setCaptionText(caption);
        marker.setCaptionTextSize(12);
        marker.setMap(naverMap);
        markers.add(marker);
    }

    private void drawPathSequentially(List<LatLng> pathCoordinates) {
        if (naverMap == null || pathCoordinates.isEmpty()) return;

        PolylineOverlay polyline = new PolylineOverlay();
        List<LatLng> partialPath = new ArrayList<>();
        polyline.setWidth(8);
        polyline.setColor(getResources().getColor(R.color.red, null));
        polyline.setMap(naverMap);
        polylines.add(polyline);

        new Thread(() -> {
            for (LatLng point : pathCoordinates) {
                runOnUiThread(() -> {
                    partialPath.add(point);
                    if (partialPath.size() > 1) {
                        polyline.setCoords(partialPath);
                    }
                });

                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Animation interrupted", e);
                }
            }
        }).start();
    }

    private void displayBrandMarkers(List<BrandMarker> brandMarkers) {
        for (BrandMarker marker : brandMarkers) {
            addMarkerAtLocation(marker.getCoordinates(), marker.getBrandName());
        }
    }

    private void fetchOptimalPath(LatLng start, int floor, List<String> brands) {
        OkHttpClient client = new OkHttpClient();

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
                    MediaType.parse("application/json"),
                    requestData.toString()
            );

            Request request = new Request.Builder()
                    .url("http://10.104.24.229:5000/optimal-path")
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
                        String responseBody = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);

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

                            runOnUiThread(() -> updateMapForCurrentFloor());
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

    private void calculateAndDrawPath() {
        if (startPoint != null && currentFloor != -1) {
            fetchOptimalPath(startPoint, currentFloor, brandList);
        } else {
            Log.e(TAG, "Start point or floor information is missing");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

class BrandMarker {
    private final String brandName;
    private final LatLng coordinates;

    public BrandMarker(String brandName, LatLng coordinates) {
        this.brandName = brandName;
        this.coordinates = coordinates;
    }

    public String getBrandName() {
        return brandName;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
}
