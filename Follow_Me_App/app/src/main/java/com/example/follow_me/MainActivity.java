package com.example.follow_me;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.MapView;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.indoor.IndoorLevel;
import com.naver.maps.map.indoor.IndoorSelection;
import com.naver.maps.map.LocationTrackingMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private MapView mapView;
    private NaverMap naverMap;
    private FusedLocationSource locationSource;
    private LatLng startPoint = null;
    private SensorDataManager sensorDataManager;
    private TextView predictionResult;
    private TextView probabilityResult;

    private int currentFloor = -1;
    private String lastSelectedFloor = null;

    private final Handler locationUpdateHandler = new Handler(Looper.getMainLooper());
    private final Handler floorUpdateHandler = new Handler(Looper.getMainLooper());

    private final List<String> brandList = new ArrayList<>(List.of(
            "A.P.C 골프", "Lee", "노스페이스", "톰그레이하운드", "더캐시미어",
            "클럽모나코", "준지", "지포어", "헤지스키즈", "네파키즈",
            "뉴발란스", "나이키", "닥스종합관", "내셔널지오그래픽키즈",
            "디아도라", "캘빈클라인진", "리바이스", "노르디스크"
    ));

    private final Map<Integer, List<LatLng>> pathCoordinatesByFloor = new HashMap<>();
    private final Map<Integer, List<BrandMarker>> brandMarkersByFloor = new HashMap<>();

    private FloorManager floorManager;
    private NetworkManager networkManager;

    private ImageView walkingAnimationView;
    private AnimationDrawable walkingAnimation;
    private ImageView escalatorAnimationView;
    private AnimationDrawable escalatorAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        walkingAnimationView = findViewById(R.id.walking_animation);
        walkingAnimationView.setBackgroundResource(R.drawable.walking_animation);
        walkingAnimation = (AnimationDrawable) walkingAnimationView.getBackground();
        escalatorAnimationView = findViewById(R.id.escalator_animation);
        escalatorAnimationView.setBackgroundResource(R.drawable.escalator_animation);
        escalatorAnimation = (AnimationDrawable) escalatorAnimationView.getBackground();

        predictionResult = findViewById(R.id.prediction_result);
        probabilityResult = findViewById(R.id.probability_result);

        // SensorDataManager 초기화
        sensorDataManager = new SensorDataManager(this, predictionResult);

        Intent intent = getIntent();
        ArrayList<String> receivedBrandList = intent.getStringArrayListExtra("brandList");
        if (receivedBrandList != null && !receivedBrandList.isEmpty()) {
            brandList.clear();
            brandList.addAll(receivedBrandList);
        }

        // 상태바 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        // 위치 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // 위치 소스 초기화
        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        // FloorManager 및 NetworkManager 초기화
        floorManager = new FloorManager(this, pathCoordinatesByFloor, brandMarkersByFloor);
        networkManager = new NetworkManager();

        // MapView 초기화
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // 버튼 클릭 리스너
        findViewById(R.id.calculate_path_button).setOnClickListener(v -> calculateAndDrawPath());
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.getUiSettings().setLocationButtonEnabled(true);

        // 위치 추적 모드 설정
        naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
        naverMap.setIndoorEnabled(true);

        // 지도 클릭 이벤트 리스너 제거
        naverMap.setOnMapClickListener(null);
        naverMap.setOnSymbolClickListener(null);

        // 초기 카메라 위치 설정
        LatLng initialLocation = new LatLng(35.192, 129.213);
        naverMap.moveCamera(CameraUpdate.scrollTo(initialLocation).animate(CameraAnimation.Easing));

        new Handler().postDelayed(() ->
                naverMap.moveCamera(CameraUpdate.zoomTo(17).animate(CameraAnimation.Easing)), 300);

        // 3초마다 위치 업데이트 시작
        startLocationUpdates();
        startFloorUpdateChecker();

        // 마커 클릭 리스너 설정
        setupMarkerClickListener();
    }

    private void startLocationUpdates() {
        locationUpdateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Location lastLocation = locationSource.getLastLocation();
                if (lastLocation != null) {
                    startPoint = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    floorManager.addMarkerAtLocation(startPoint, "Current Location", naverMap);
                    Log.d(TAG, "Updated start point: " + startPoint);
                } else {
                    Log.e(TAG, "Unable to get current location");
                }
                locationUpdateHandler.postDelayed(this, 3000); // 3초마다 실행
            }
        }, 3000);
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

    public void markStoreAsVisited(String visitedStore) {
        if (brandList.contains(visitedStore)) {
            brandList.remove(visitedStore);
            Toast.makeText(this, visitedStore + " 방문 완료!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, visitedStore + " removed from the brand list.");
            calculateAndDrawPath();
        }
    }

    private void setupMarkerClickListener() {
        naverMap.setOnSymbolClickListener(symbol -> {
            String storeName = symbol.getCaption();
            markStoreAsVisited(storeName);
            return true;
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (naverMap != null) {
                    naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
                }
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationUpdateHandler.removeCallbacksAndMessages(null);
        floorUpdateHandler.removeCallbacksAndMessages(null);
        sensorDataManager.stopSensorUpdates();
    }

    // 애니메이션 시작 메서드
    private void startWalkingAnimation() {
        walkingAnimationView.setVisibility(View.VISIBLE);
        walkingAnimation.start();
    }

    private void startescalatorAnimation() {
        escalatorAnimationView.setVisibility(View.VISIBLE);
        escalatorAnimation.start();
    }

    // 모든 애니메이션 중지
    private void stopAllAnimations() {
        if (walkingAnimation.isRunning()) {
            walkingAnimation.stop();
            walkingAnimationView.setVisibility(View.GONE);
        }
        if (escalatorAnimation.isRunning()) {
            escalatorAnimation.stop();
            escalatorAnimationView.setVisibility(View.GONE);
        }
    }

    // 예측 결과를 업데이트하는 메서드
    public void updatePrediction(String prediction, double walkingProbability, double escalatorProbability) {
        stopAllAnimations();

        // 확률 텍스트 업데이트
        String probabilityText = String.format("걷는 중: %.2f%%\n탑승 중: %.2f%%", walkingProbability * 100, escalatorProbability * 100);
        probabilityResult.setText(probabilityText);

        // 애니메이션 실행
        if ("걷는 중".equals(prediction)) {
            startWalkingAnimation();
        } else if ("탑승 중".equals(prediction)) {
            startescalatorAnimation();
        }
    }

    public void moveToHigherFloor() {
        IndoorSelection currentSelection = naverMap.getIndoorSelection();
        if (currentSelection != null) {
            IndoorLevel currentLevel = currentSelection.getLevel();
            Toast.makeText(this, "현재 층: " + currentLevel.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "내부지도가 활성화되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void moveToLowerFloor() {
        IndoorSelection currentSelection = naverMap.getIndoorSelection();
        if (currentSelection != null) {
            IndoorLevel currentLevel = currentSelection.getLevel();
            Toast.makeText(this, "현재 층: " + currentLevel.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "내부지도가 활성화되지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
