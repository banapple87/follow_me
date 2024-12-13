package com.example.follow_me;

import com.naver.maps.geometry.LatLng;

public class BrandMarker {
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