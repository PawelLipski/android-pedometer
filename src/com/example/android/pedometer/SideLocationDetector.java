package com.example.android.pedometer;

import android.hardware.Sensor;

public class SideLocationDetector implements Detector {

    private final long startTime;
    private float currentValue;
    private int currentTick;
    private int lastDetectionTick = -ticksToWaitForNextStep;

    private static final int ticksToWaitForNextStep = 7;
	// 13.0f for fast walk only, 12.4f for both slow and fast walk
	private static final float posThreshold = 12.4f;
	private static final float negThreshold = -10.5f;

    public SideLocationDetector() {
        startTime = System.nanoTime();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {

        currentTick++;
        currentValue = _x;

        boolean walkBottomToTopCondition = currentValue >= posThreshold;
		boolean walkTopToBottomCondition = currentValue <= negThreshold;
        if (walkBottomToTopCondition || walkTopToBottomCondition) {
            if (currentTick > lastDetectionTick + ticksToWaitForNextStep) {
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

        line.append(String.format("<font color='%s'> %.1f </font> t=%.3f",
                color, currentValue, (System.nanoTime() - startTime) / 1e9));
        return line.toString();
    }

    @Override
    public int getSensorDelay() {
        return 50000; // 50 ms
    }
}
