package com.example.android.pedometer;

public interface Detector {
    boolean feed(float x, float y, float z);

    String getLog();

    int getSensorDelay();

    int getSensorType();
}
