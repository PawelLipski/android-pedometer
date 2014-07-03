package com.example.android.pedometer;

import android.hardware.Sensor;

public class DummyZAccDetector implements Detector {

    private final long startTime;
    private float currentValue, lastValue;

	public DummyZAccDetector() {
        startTime = System.nanoTime();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {
		lastValue = currentValue;
        currentValue = _z;
        return false;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    @Override
    public synchronized String getLog() {
        return String.format("<font color='#888888'> %.1f </font> t=%.3f",
                currentValue, (System.nanoTime() - startTime) / 1e9);
    }

    @Override
    public int getSensorDelay() {
        return 20000;
    }
}
