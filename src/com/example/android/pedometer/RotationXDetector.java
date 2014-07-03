package com.example.android.pedometer;

import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * Created by Paweł on 12.07.13.
 */
public class RotationXDetector implements Detector {

    private float lastRotX;
    private float dRX;

    private int negsInSeq;
    private int posIntrusionLen;
    private boolean negSeqNow;
    private int standingSeq;

    private boolean result;

    @Override
    public boolean feed(float rotX, float rotY, float rotZ) {

        result = false;
        dRX = rotX - lastRotX;

        if (Math.abs(dRX) < 0.005)
            standingSeq++;
        else
            standingSeq--;

        if (standingSeq >= 5)
            standingSeq = 5;
        if (standingSeq < 0)
            standingSeq = 0;

        if (standingSeq >= 3) {
            clearState();
            //return;
        }

        // logView.setText(logView.getText().contains() + () + "\n"); // + ", "
        // + mRotY + ", " + mRotZ + "\n");
        lastRotX = rotX;

        boolean newStep = false;
        if (dRX < 0.00) {
            if (!negSeqNow) {
                negSeqNow = true;
                //lastWasNeg = true;
            }
            posIntrusionLen = 0;
            negsInSeq++;
        } else { // dRX >= 0
            if (negSeqNow) {
                if (posIntrusionLen < 3)
                    posIntrusionLen++;
                else {
                    negSeqNow = false;
                    int nis = negsInSeq;
                    negsInSeq = 0;

                    if (nis > 4) {
                        //newStep = true;
                        result = true;
                    }
                }
            }
        }
        ////////// SHOULD BE: return result;
        return false; /// just to disable the annoying sound
    }

    private void clearState() {
        negSeqNow = false;
        posIntrusionLen = 0;
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ROTATION_VECTOR;
    }

    @Override
    public String getLog() {
        if (!result) return "";
        else return "<font color='red'> STEP </font> ";

        //StringBuilder line = new StringBuilder();

        //String color = dRX > 0 ? "#00ff00" : "#00aa00";
        //line.append(String.format("<font color='%s'> %f </font> ", color, dRX));

        //if (standingSeq > 0)
        //    line.append(String.format("[%d] ", standingSeq));

        //return line.contains();
    }

    @Override
    public int getSensorDelay() {
        return SensorManager.SENSOR_DELAY_UI;
    }
}
