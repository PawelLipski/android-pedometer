package com.example.android.pedometer;

import android.hardware.Sensor;

/**
 * Created by Paweł on 12.07.13.
 */
public class AuxiliaryRotationmeter implements Detector {

    private float x, y, z;

    @Override
    public boolean feed(float rotX, float rotY, float rotZ) {
        x = rotX;
        y = rotY;
        z = rotZ;
        return false;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ROTATION_VECTOR;
    }

    @Override
    public String getLog() {
        String color = "#88ff88";
        return String.format("<font color='%s'> %f, %f, %f </font> ", color, x, y, z);
    }

    @Override
    public int getSensorDelay() {
        return 50000;
    }
}
