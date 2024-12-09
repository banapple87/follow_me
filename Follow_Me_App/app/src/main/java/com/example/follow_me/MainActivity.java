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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";

    private MapView mapView;
    private NaverMap naverMap;
    private LatLng startPoint = null;

    private int currentFloor = -1;
    private String lastSelectedFloor = null;

    private final android.os.Handler floorUpdateHandler = new android.os.Handler();
    private final List<String> brandList = List.of("A.P.C 골프", "Lee", "노스페이스", "톰그레이하운드", "더캐시미어", "클럽모나코", "준지", "지포어", "헤지스키즈", "네파키즈", "뉴발란스", "나이키", "닥스종합관", "내셔널지오그래픽키즈", "디아도라", "캘빈클라인진", "리바이스", "노르디스크");

    private final Map<Integer, List<LatLng>> pathCoordinatesByFloor = new HashMap<>();
    private final Map<Integer, List<BrandMarker>> brandMarkersByFloor = new HashMap<>();

    private FloorManager floorManager;
    private NetworkManager networkManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FloorManager 초기화
        floorManager = new FloorManager(this, pathCoordinatesByFloor, brandMarkersByFloor);

        // NetworkManager 초기화
        networkManager = new NetworkManager();

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 버튼 클릭 리스너
        findViewById(R.id.calculate_path_button).setOnClickListener(v -> calculateAndDrawPath());
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setIndoorEnabled(true);

        // Initialize camera
        LatLng initialLocation = new LatLng(35.192, 129.213);
        naverMap.moveCamera(CameraUpdate.scrollTo(initialLocation).animate(CameraAnimation.Easing));

        new android.os.Handler().postDelayed(() ->
                naverMap.moveCamera(CameraUpdate.zoomTo(17).animate(CameraAnimation.Easing)), 300);

        // Set map click listener
        naverMap.setOnMapClickListener((point, coord) -> {
            if (startPoint == null) {
                startPoint = coord;
                floorManager.addMarkerAtLocation(coord, "Start Point", naverMap);
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
                            floorManager.updateMapForFloor(currentFloor, naverMap);
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

    private void calculateAndDrawPath() {
        if (startPoint != null && currentFloor != -1) {
            networkManager.fetchOptimalPath(
                    startPoint, currentFloor, brandList, pathCoordinatesByFloor, brandMarkersByFloor,
                    () -> runOnUiThread(() -> floorManager.updateMapForFloor(currentFloor, naverMap))
            );
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