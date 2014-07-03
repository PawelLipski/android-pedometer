package com.example.android.pedometer;

import android.hardware.Sensor;

import java.util.ArrayList;
import java.util.List;

public class EarDetector implements Detector {

    private float lastValue, currentValue;
    private int currentTick;
    private int lastDetectionTick = -ticksToWait;

    private static final int ticksToWait = 8;
    private static final float threshold = -7.2f;
	private static final float difference = 1.0f;
	private float average;

	private List<Float> hist = new ArrayList<Float>();

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {

        currentTick++;
        lastValue = currentValue;
        currentValue = _x;
		hist.add(currentValue);
		average = getHistoryAverage();

        if (currentValue <= average - difference) {
            if (currentTick > lastDetectionTick + ticksToWait) {
                lastDetectionTick = currentTick;
				hist.clear();
                return true;
            } else
                return false;
        } else
            return false;
    }

	private float getHistoryAverage() {
		float s = 0;
		for (Float v: hist)
			s += v;
		return s / hist.size();
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

        line.append(String.format("<font color='%s'> %.1f </font> %.2f",
                color, currentValue, average));
        return line.toString();
    }

    @Override
    public int getSensorDelay() {
        return 50000;
    }
}
