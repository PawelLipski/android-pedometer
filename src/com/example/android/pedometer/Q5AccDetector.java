package com.example.android.pedometer;

import android.hardware.Sensor;

import java.util.ArrayList;
import java.util.List;

public class Q5AccDetector implements Detector {

	private int pos;

    private final long startTime;
	private int currentTick;
    private float currentValue, lastValue;

	private float q5up;

	private static final int TICKS_PER_SECOND = 20;

	private static final int SECONDS_IN_WINDOW = 4;
	private static final int TICKS_IN_WINDOW = SECONDS_IN_WINDOW * TICKS_PER_SECOND;

	private List<Float> vals = new ArrayList<Float>();
	private float[] top = new float[TICKS_IN_WINDOW / 20 + 1];
	private float[] bottom = new float[TICKS_IN_WINDOW / 20 + 1];

	public Q5AccDetector(int pos) {
		this.pos = pos;
        startTime = System.nanoTime();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {
        currentValue = new float[] {_x, _y, _z}[pos];
		vals.add(currentValue);
		currentTick++;
		getQ5Up();
		getQ5Dn();
        return false;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

	private float getQ5Up() {
		if (currentTick <= TICKS_IN_WINDOW)
			return 0.0f;

		int sz = TICKS_IN_WINDOW / 20;
		top = new float[sz + 1];
		for (int i = 0; i <= sz; i++)
			top[i] = -99;
		for (int i = currentTick - TICKS_IN_WINDOW; i < currentTick; i++) {
			int j;
			float v = vals.get(i);
			for (j = 1; j <= sz && top[j] < v; j++)
				top[j-1] = top[j];
			top[j-1] = v;
		}
		return top[1];
	}

	private float getQ5Dn() {
		if (currentTick <= TICKS_IN_WINDOW)
			return 0.0f;

		int sz = TICKS_IN_WINDOW / 20;
		bottom = new float[sz + 1];
		for (int i = 0; i <= sz; i++)
			bottom[i] = 99;
		for (int i = currentTick - TICKS_IN_WINDOW; i < currentTick; i++) {
			int j;
			float v = vals.get(i);
			for (j = 1; j <= sz && bottom[j] > v; j++)
				bottom[j-1] = bottom[j];
			bottom[j-1] = v;
		}
		return bottom[1];
	}

	@Override
    public synchronized String getLog() {
		String log = String.format("<font color='#888888'> %.1f q5up=", currentValue);
		for (int i = 1; i <= TICKS_IN_WINDOW / 20; i++)
        	log += String.format("%.1f ", top[i]);
		log += String.format("</font> t=%.3f <br/> q5dn=", (System.nanoTime() - startTime) / 1e9);
		for (int i = 1; i <= TICKS_IN_WINDOW / 20; i++)
			log += String.format("%.1f ", bottom[i]);
		return log;
    }

    @Override
    public int getSensorDelay() {
        return 1000000 / TICKS_PER_SECOND;
    }
}
