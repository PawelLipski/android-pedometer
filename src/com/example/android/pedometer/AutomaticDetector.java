package com.example.android.pedometer;

import android.hardware.Sensor;
import android.speech.tts.TextToSpeech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutomaticDetector implements Detector {

	public static final int UNRECOGNIZED = -1;
	public static final int NO_MOTION = 0;
	public static final int FRONT_POCKET = 1;
	public static final int BACK_POCKET = 2;
	public static final int SIDE_POCKET_OR_LAPTOP_BAG = 3;
	public static final int BACKPACK = 4;

	private static final int NEGATIVE = -1;
	private static final int POSITIVE = 1;

	private static final int WINDOW_SIZE = 75;
	//private static final int MIN_VALUES_COLLECTED = 200;
	private static final int RECOGNITION_GAP = WINDOW_SIZE * 3;

	private TextToSpeech tts;
	private String spoken;

	private List<Float> xv;
	private List<Float> yv;
	private List<Float> zv;
	private float xc, yc, zc;
	private int lastRecognitionTick, currentTick;

	public AutomaticDetector(TextToSpeech tts) {
		this.xv = Collections.synchronizedList(new ArrayList<Float>());
		this.yv = Collections.synchronizedList(new ArrayList<Float>());
		this.zv = Collections.synchronizedList(new ArrayList<Float>());
		this.tts = tts;
	}

	@Override
	public boolean feed(float x, float y, float z) {
		xc = x;
		yc = y;
		zc = z;
		xv.add(x);
		yv.add(y);
		zv.add(z);
		currentTick++;
		return false;
	}

	private String recognize() {
		float xpp = getPercentageAbove(xv, 0);
		float ypp = getPercentageAbove(yv, 0);
		float zpp = getPercentageAbove(zv, 0);

		float xgt12 = getPercentageAbove(xv, 12);
		float xlt_12 = getPercentageBelow(xv, -12);

		spoken = "";
// comments refer to Learning
		if (currentTick >= lastRecognitionTick + RECOGNITION_GAP) {
			if (xpp == 0 && xgt12 == 0 && xlt_12 == 0 && ypp == 100 && 60 <= zpp && zpp < 100) {
				// OK, z>0 = 56-100
				speak("At the right ear");
			} else if (xpp >= 95 && xgt12 == 0 && xlt_12 == 0 && ypp == 100 && 50 <= zpp && zpp < 100) {
				// :S, x<-12 = 0-9, z>0 = 100
				speak("At the left ear");
			} else if (40 <= xpp && xpp <= 95 && xgt12 == 0 && xlt_12 == 0 && ypp == 100 && zpp == 100) {
				// OK, x>0 = 42-100
				speak("Portrait orientation in front of your face");
			} else if ((xpp == 0 || xpp == 100) && xgt12 == 0 && xlt_12 == 0 && 35 <= ypp && ypp <= 70 && zpp == 100) {
				// OK, y>0 = 0-100 (sic!)
				speak("Landscape orientation in front of your face");
			} else if ((xpp == 100 && xgt12 >= 1 || xpp == 0 && xlt_12 >= 1) && (zpp < 10 || 90 < zpp)) {
				// OK, y>0 = 12-100, z>0 = 66-100
				speak("In the hand");
			} else if (0 < xpp && xpp < 95 && ypp == 100 && 40 <= zpp && zpp <= 60) {
				//:S, x>0 = 94-99, z>0 = 29-56
				speak("Front pocket");
			} else if (55 <= zpp && zpp <= 90 && (xlt_12 >= 15 || xgt12 >= 15)) {
				speak("Side pocket screen to body");
			} else if (18 <= zpp && zpp <= 26 && (xlt_12 >= 15 || xgt12 >= 15)) {
				speak("Side pocket back to body");
			} else if (xpp < 100 && xgt12 < 5 && xlt_12 < 5 && ypp == 100 && 75 <= zpp && zpp <= 92) {
				speak("Back pocket screen to body");
			} else if (xpp < 100 && xgt12 < 5 && xlt_12 < 5 && ypp == 100 && 17 <= zpp && zpp <= 36) {
				speak("Back pocket back to body");
			}
		}

		return
			"xpp: " + xpp + "%, ypp: " + ypp + "%, zpp: " + zpp + "%<br/> &nbsp; " +
			"xgt12: " + xgt12 + "%, xlt_12: " + xlt_12 + "%" +
			(spoken.length() > 0 ? " (<font color='red'><b>" + spoken + "</b></font>)" : "") + "<br/> &nbsp; " +
			"x: " + xc + ", y: " + yc + "%, z: " + zc;
	}

	private void speak(String what) {
		//if (xv.size() >= MIN_VALUES_COLLECTED && !spoken) {
		tts.speak(what, TextToSpeech.QUEUE_FLUSH, null);
		lastRecognitionTick = currentTick;
		spoken = what;
			//spoken = true;
		//}
	}

	private float getPercentage(List<Float> v, int sign) {
		int c = 0;
		int ws = getWindowSize(v);
		for (int i = v.size() - ws; i < v.size(); i++)
			if (equalSigns(v.get(i), sign))
				c++;
		return 100f * c / ws;
	}

	private float getPercentageAbove(List<Float> v, float value) {
		int c = 0;
		int ws = getWindowSize(v);
		for (int i = v.size() - ws; i < v.size(); i++)
			if (v.get(i) > value)
				c++;
		return 100f * c / ws;
	}

	private float getPercentageBelow(List<Float> v, float value) {
		int c = 0;
		int ws = getWindowSize(v);
		for (int i = v.size() - ws; i < v.size(); i++)
			if (v.get(i) < value)
				c++;
		return 100f * c / ws;
	}

	private boolean equalSigns(Float a, int sign) {
		return (a < 0) == (sign < 0);
	}

	private int getWindowSize(List<Float> v) {
		return Math.min(v.size(), WINDOW_SIZE);
	}

	private float getAverageSequenceLength(int sign) {
		int l = 0;
		int ls = 0, lc = 0;
		for (int i = zv.size() - getWindowSize(zv); i < zv.size(); i++) {
			if (equalSigns(zv.get(i), sign)) {
				l++;
			} else if (l > 0) {
				lc++;
				ls += l;
				l = 0;
			}
		}
		if (l > 0) {
			lc++;
			ls += l;
		}
		if (lc == 0)
			lc = 1;
		return (float)ls / lc;
	}

	@Override
	public String getLog() {
		return recognize();
	}

	@Override
	public int getSensorDelay() {
		return 50000;
	}

	@Override
	public int getSensorType() {
		return Sensor.TYPE_ACCELEROMETER;
	}
}
