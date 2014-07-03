package com.example.android.pedometer;

import android.hardware.Sensor;

public class BackPocketDetector implements Detector {

    private final long startTime;
    private float lastValue, currentValue;
	private float earlierValue1, earlierValue2, earlierValue3;
    private int currentTick;
    private int lastDetectionTick = -ticksToWaitForNextStep;
	private int lastTickUnderThreshold = 0;
	private int negsInRowCnt = 0;

    private static final int ticksToWaitForNextStep = 8;
	private static final int ticksToRememberTickUnderThreshold = 4;
	private static final int maximumNegsInRowBeforePositive = 3;
	private static final float threshold = -0.6f;
	private static final int ticksToWait = 8;


	public BackPocketDetector() {
        startTime = System.nanoTime();
    }

    @Override
    public synchronized boolean feed(float _x, float _y, float _z) {

		/*
        currentTick++;
        lastValue = currentValue;
        currentValue = _z;

        boolean condition = currentValue < threshold;
        if (condition) {
            if (currentTick > lastDetectionTick + ticksToWait) {
                lastDetectionTick = currentTick;
                return true;
            } else
                return false;
        } else
            return false;*/

		/*
		currentTick++;
		lastValue = currentValue;
		currentValue = _z;

		boolean underThreshold = currentValue < threshold;
		if (underThreshold) // && notToLongSequenceOfNegative)
			lastTickUnderThreshold = currentTick;

		if (currentValue < 0)
			negsInRowCnt++;
		else {
			boolean notTooEarlyAfterPreviousStep = currentTick > lastDetectionTick + ticksToWaitForNextStep;
			boolean notTooLateAfterLastUnderThreshold = currentTick <= lastTickUnderThreshold + ticksToRememberTickUnderThreshold;
			boolean notAfterTooLongSeqOfNegs = negsInRowCnt < maximumNegsInRowBeforePositive;
			negsInRowCnt = 0;

			if (notTooEarlyAfterPreviousStep && notTooLateAfterLastUnderThreshold && notAfterTooLongSeqOfNegs) {
				lastDetectionTick = currentTick;
				return true;
			}
		}

		return false;*/

		currentTick++;
		earlierValue3 = earlierValue2;
		earlierValue2 = earlierValue1;
		earlierValue1 = currentValue;
		currentValue = _z;

		boolean condition = ((currentValue >= 0.8f && (earlierValue1 > 0 || earlierValue2 > 0 || earlierValue3 > 0))
				|| currentValue > 1.7f) && (earlierValue1 < 0 || earlierValue2 < 0 || earlierValue3 < 0);
		if (condition) {
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
