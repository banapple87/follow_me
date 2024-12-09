package com.example.follow_me;

import android.content.Context;
import android.util.Log;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.PolylineOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FloorManager {
    private static final String TAG = "FloorManager";

    private final Context context;
    private final Map<Integer, List<LatLng>> pathCoordinatesByFloor;
    private final Map<Integer, List<BrandMarker>> brandMarkersByFloor;
    private final List<Marker> markers = new ArrayList<>();
    private final List<PolylineOverlay> polylines = new ArrayList<>();

    public FloorManager(Context context, Map<Integer, List<LatLng>> pathCoordinatesByFloor,
                        Map<Integer, List<BrandMarker>> brandMarkersByFloor) {
        this.context = context;
        this.pathCoordinatesByFloor = pathCoordinatesByFloor;
        this.brandMarkersByFloor = brandMarkersByFloor;
    }

    public void addMarkerAtLocation(LatLng coord, String caption, NaverMap naverMap) {
        if (naverMap == null) {
            Log.e(TAG, "NaverMap is null. Cannot add marker.");
            return;
        }

        ((MainActivity) context).runOnUiThread(() -> {
            Marker marker = new Marker();
            marker.setPosition(coord);
            marker.setCaptionText(caption);
            marker.setCaptionTextSize(12);
            marker.setMap(naverMap);
            markers.add(marker);
            Log.d(TAG, "Marker added at: " + coord.toString());
        });
    }

    public void updateMapForFloor(int currentFloor, NaverMap naverMap) {
        clearMarkersAndPolylines();

        if (pathCoordinatesByFloor.containsKey(currentFloor)) {
            drawPathSequentially(pathCoordinatesByFloor.get(currentFloor), naverMap);
        } else {
            Log.d(TAG, "No path data for current floor.");
        }

        if (brandMarkersByFloor.containsKey(currentFloor)) {
            displayBrandMarkers(brandMarkersByFloor.get(currentFloor), naverMap);
        } else {
            Log.d(TAG, "No brand markers for current floor.");
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

    private void drawPathSequentially(List<LatLng> pathCoordinates, NaverMap naverMap) {
        if (pathCoordinates.isEmpty()) {
            Log.d(TAG, "Path coordinates are empty.");
            return;
        }

        // 제외할 좌표 리스트
        List<LatLng> excludedPoints = List.of(
                new LatLng(35.19144338464835, 129.21369493093476),
                new LatLng(35.192132190342164, 129.21371457602288),
                new LatLng(35.191653434224534, 129.2130171253134),
                new LatLng(35.19201520242625, 129.21315764667446),
                new LatLng(35.191394445950095, 129.21231419024843),
                new LatLng(35.19291498277528, 129.21345822866675),
                new LatLng(35.19160726654752, 129.2116812641865),
                new LatLng(35.19286223902206, 129.21311286507154),
                new LatLng(35.192413176977695, 129.2124211104043),
                new LatLng(35.19209430031134, 129.2113800279564),
                new LatLng(35.19307552851066, 129.21248211558168),
                new LatLng(35.19299257338001, 129.21203572251545),
                new LatLng(35.19365025433626, 129.21254945715805),
                new LatLng(35.19160934023736, 129.21400270154987),
                new LatLng(35.19112731954448, 129.21268202896175),
                new LatLng(35.192327947971435, 129.2137974755994),
                new LatLng(35.19192811960015, 129.21341772020668),
                new LatLng(35.19173871638391, 129.21276940004816),
                new LatLng(35.192850349504, 129.21336219246325),
                new LatLng(35.19236772353568, 129.2129716184167),
                new LatLng(35.19180110876471, 129.21197846086585),
                new LatLng(35.19262770099036, 129.2127422269799),
                new LatLng(35.193315095786204, 129.2125530887747),
                new LatLng(35.19352788831908, 129.21252863095162),
                new LatLng(35.192909368681356, 129.2121192414773)
        );

        Wrapper<PolylineOverlay> currentPolylineWrapper = new Wrapper<>(new PolylineOverlay());
        List<LatLng> partialPath = new ArrayList<>();

        ((MainActivity) context).runOnUiThread(() -> {
            currentPolylineWrapper.value.setWidth(8);
            currentPolylineWrapper.value.setMap(naverMap);
            polylines.add(currentPolylineWrapper.value);
        });

        new Thread(() -> {
            for (LatLng point : pathCoordinates) {
                if (excludedPoints.contains(point)) {
                    if (partialPath.size() >= 2) {
                        List<LatLng> finalPartialPath = new ArrayList<>(partialPath);
                        ((MainActivity) context).runOnUiThread(() -> currentPolylineWrapper.value.setCoords(finalPartialPath));
                    }

                    partialPath.clear();
                    PolylineOverlay newPolyline = new PolylineOverlay();
                    ((MainActivity) context).runOnUiThread(() -> {
                        newPolyline.setWidth(8);
                        newPolyline.setMap(naverMap);
                        polylines.add(newPolyline);
                        currentPolylineWrapper.value = newPolyline;
                    });
                }

                partialPath.add(point);

                if (partialPath.size() >= 2) {
                    List<LatLng> finalPartialPath = new ArrayList<>(partialPath);
                    ((MainActivity) context).runOnUiThread(() -> currentPolylineWrapper.value.setCoords(finalPartialPath));
                }

                try {
                    Thread.sleep(5); // 애니메이션 딜레이
                } catch (InterruptedException e) {
                    Log.e(TAG, "Animation interrupted", e);
                }
            }

            if (partialPath.size() >= 2) {
                List<LatLng> finalPartialPath = new ArrayList<>(partialPath);
                ((MainActivity) context).runOnUiThread(() -> currentPolylineWrapper.value.setCoords(finalPartialPath));
            } else {
                Log.d(TAG, "Final partialPath size is less than 2. Skipping final setCoords.");
            }
        }).start();
    }

    // Wrapper 클래스
    private static class Wrapper<T> {
        T value;

        Wrapper(T value) {
            this.value = value;
        }
    }

    private void displayBrandMarkers(List<BrandMarker> brandMarkers, NaverMap naverMap) {
        for (BrandMarker marker : brandMarkers) {
            Marker mapMarker = new Marker();
            mapMarker.setPosition(marker.getCoordinates());
            mapMarker.setCaptionText(marker.getBrandName());
            mapMarker.setCaptionTextSize(12);
            mapMarker.setMap(naverMap);
            markers.add(mapMarker);
        }
    }
}
