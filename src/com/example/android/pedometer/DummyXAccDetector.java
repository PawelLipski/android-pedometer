package com.example.android.pedometer;

import android.hardware.Sensor;

public class DummyXAccDetector implements Detector {

    private final long startTime;
    private float currentValue, lastValue;

	public DummyXAccDetector() {
        startTime = System.nanoTime();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {
		lastValue = currentValue;
        currentValue = _x;
        return false;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    @Override
    public synchronized String getLog() {
        return String.format("<font color='#888888'> %.1f %.1f </font> t=%.3f",
                currentValue, currentValue - lastValue, (System.nanoTime() - startTime) / 1e9);
    }

    @Override
    public int getSensorDelay() {
        return 50000;
    }
}
