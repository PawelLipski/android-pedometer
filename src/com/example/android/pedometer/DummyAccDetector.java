package com.example.android.pedometer;

import android.hardware.Sensor;

public class DummyAccDetector implements Detector {

    private final long startTime;
    private float x, y, z, lx, ly, lz;

	public DummyAccDetector() {
        startTime = System.nanoTime();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {
		lx = x;
		ly = y;
		lz = z;
        x = _x;
		y = _y;
		z = _z;
        return false;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    @Override
    public synchronized String getLog() {
        return String.format("<font color='#888888'>%.1f %.1f %.1f</font>" +
				" %.1f %.1f %.1f <font color='#888888'>t=%.3f</font>",
                x, y, z, x - lx, y - ly, z - lz, (System.nanoTime() - startTime) / 1e9);
    }

    @Override
    public int getSensorDelay() {
        return 50000;
    }
}
