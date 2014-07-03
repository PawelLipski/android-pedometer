package com.example.android.pedometer;

import android.hardware.Sensor;

public class FrontPocketDetector implements Detector {

    private float lastValue, currentValue;
    private int currentTick;
    private int lastDetectionTick = -ticksToWait;

    private static final int ticksToWait = 4;
    private static final float threshold = 8.0f;

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {

        currentTick++;
        lastValue = currentValue;
        currentValue = _z;

        boolean conditionB2B = currentValue > threshold && lastValue > 0 && lastValue < threshold;
        boolean conditionS2B = currentValue < -threshold && lastValue < 0 && lastValue > -threshold;
        if (conditionB2B || conditionS2B) {
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
        return 50000; // 50 ms //SensorManager.SENSOR_DELAY_NORMAL;
    }
}
