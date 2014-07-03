package com.example.android.pedometer;

import android.hardware.Sensor;

public class BackpackDetector implements Detector {

    private final long startTime;
    private float lastValue, currentValue;
    private int currentTick;
    private int lastDetectionTick = -ticksToWait;
	private int negsInRow;

    private static final int ticksToWait = 6;
    private static final float thresholdS2B = 5.2f;
    // previously for B2B:  0.6f; // 1.2f middle-paced walk // 1.5f fast walk;
	private static final int minimumNegsInRowBeforePositiveS2B = 4;

	public BackpackDetector() {
        startTime = System.nanoTime();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {

        currentTick++;
        lastValue = currentValue;
        currentValue = _z;


        boolean backToBodyCondition = currentValue > 0 && negsInRow >= minimumNegsInRowBeforePositiveS2B; //&& lastValue < 0;
        boolean screenToBodyCondition = currentValue > thresholdS2B;

		if (currentValue < 0)
			negsInRow++;
		else
			negsInRow = 0;

        if (/*backToBodyCondition || screenToBodyCondition*/currentValue > 0 && lastValue < 0) {
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

        line.append(String.format("<font color='%s'> %.1f </font> t=%.3f",
                color, currentValue, (System.nanoTime() - startTime) / 1e9));
        return line.toString();
    }

    @Override
    public int getSensorDelay() {
        return 50000; // 50 ms //SensorManager.SENSOR_DELAY_NORMAL;
    }
}
