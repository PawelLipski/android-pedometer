package com.example.android.pedometer;

import android.hardware.Sensor;

public class DummyRotationDetector implements Detector {

    private final long startTime;
    private float x, y, z;

	public DummyRotationDetector() {
        startTime = System.nanoTime();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {
        x = _x;
		y = _y;
		z = _z;
        return false;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ROTATION_VECTOR;
    }

    @Override
    public synchronized String getLog() {
        return String.format("%.1f %.1f %.1f <font color='#888888'>t=%.3f</font>",
                x, y, z, (System.nanoTime() - startTime) / 1e9);
    }

    @Override
    public int getSensorDelay() {
        return 20000;
    }
}
