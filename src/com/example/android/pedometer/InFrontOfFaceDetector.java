package com.example.android.pedometer;

import android.hardware.Sensor;

public class InFrontOfFaceDetector implements Detector {

    private float lastValue, currentValue;
    private int currentTick;
    private int lastDetectionTick = -ticksToWait;

    private static final int ticksToWait = 8;
	private static final float minimalDifference = 1.0f;
    private static final float threshold = 9.8f;

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {

        currentTick++;
        lastValue = currentValue;
        currentValue = _z;

        if (currentValue >= threshold && currentValue - lastValue >= minimalDifference) {
            if (currentTick > lastDetectionTick + ticksToWait) {
                lastDetectionTick = currentTick;
                return true;
            } else
                return false;
        } else
            return false;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    @Override
    public synchronized String getLog() {
        StringBuilder line = new StringBuilder();
        String color;
        if (lastDetectionTick == currentTick)
            color = "#ffffff";
        else
            color = "#888888";

        line.append(String.format("<font color='%s'> %.1f </font>",
                color, currentValue));
        return line.toString();
    }

    @Override
    public int getSensorDelay() {
        return 50000;
    }
}
