package com.example.follow_me.data;

public class SensorData {
    private float time;
    private float[] gyroscope;
    private float[] accelerometer;

    public SensorData(float time, float[] gyroscope, float[] accelerometer) {
        this.time = time;
        this.gyroscope = gyroscope;
        this.accelerometer = accelerometer;
    }

    // Getter methods
    public float getTime() {
        return time;
    }

    public float[] getGyroscope() {
        return gyroscope;
    }

    public float[] getAccelerometer() {
        return accelerometer;
    }
}