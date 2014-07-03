package com.example.android.pedometer;

import android.hardware.Sensor;

import java.util.ArrayList;
import java.util.List;

public class AvAccDetector implements Detector {

    private final long startTime;
	private int currentTick;
    private float currentValue, lastValue;

	private float avd;

	private static final int TICKS_PER_SECOND = 20;

	private static final int SECONDS_IN_WINDOW = 4;
	private static final int TICKS_IN_WINDOW = SECONDS_IN_WINDOW * TICKS_PER_SECOND;

	private List<Float> xv, yv, zv;
	private float x, y, z;

	public AvAccDetector() {
        startTime = System.nanoTime();
		this.xv = new ArrayList<Float>();
		this.yv = new ArrayList<Float>();
		this.zv = new ArrayList<Float>();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {

		x = _x;
		y = _y;
		z = _z;
		xv.add(_x);
		yv.add(_y);
		zv.add(_z);

		currentTick++;
		avd = getAvDiff();
        return false;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

	private float getAvDiff() {
		if (currentTick <= TICKS_IN_WINDOW)
			return 0.0f;

		float s = 0;
		for (int i = currentTick - TICKS_IN_WINDOW; i < currentTick; i++) {
			s += Math.abs(xv.get(i) - xv.get(i-1));
			s += Math.abs(yv.get(i) - yv.get(i-1));
			s += Math.abs(zv.get(i) - zv.get(i-1));
		}
		return s / TICKS_IN_WINDOW / 3;
	}


	@Override
    public synchronized String getLog() {
		String log = String.format("%.1f %.1f %.1f avd=%.1f", x, y, z, avd);
		log += String.format("<font color='#888888'>t=%.3f </font> <br/> ", (System.nanoTime() - startTime) / 1e9);
		return log;
    }

    @Override
    public int getSensorDelay() {
        return 1000000 / TICKS_PER_SECOND;
    }
}
