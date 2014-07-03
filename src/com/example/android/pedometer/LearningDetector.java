package com.example.android.pedometer;

import android.content.Context;
import android.hardware.Sensor;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class LearningDetector implements Detector {

	private static final int TICKS_PER_SECOND = 20;

	private static final int SECONDS_PER_LOCATION = 10;
	private static final int TICKS_PER_LOCATION = SECONDS_PER_LOCATION * TICKS_PER_SECOND;

	private static final int SECONDS_IN_WINDOW = 4;
	private static final int TICKS_IN_WINDOW = SECONDS_IN_WINDOW * TICKS_PER_SECOND;

	private static final int SECONDS_BETWEEN_RECOGNITIONS = SECONDS_IN_WINDOW * 2;
	private static final int TICKS_BETWEEN_RECOGNITIONS = SECONDS_BETWEEN_RECOGNITIONS * TICKS_PER_SECOND;


	private final Context context;
	private TextToSpeech tts;

	private List<Float> xv;
	private List<Float> yv;
	private List<Float> zv;

	private int lastSpeakTick, currentTick;

	private boolean learning = true;
	private int lastLearnBeginTick;
	private int currentLocationBeingLearnt;
	private int ticksToPrepare;

	private List<StatCube> cubes = new ArrayList<StatCube>();
	private StatCube cube;
	private float xg0, xg12, xlm12, yg0, zg0, x5dn, x5up, z5dn, z5up, avd;

	private Logger log;
	private final long startTime;

	private JsonWriter jsonWriter;

	private Detector currentDetector;
	private Map<String, String> detectorNameForTag = new HashMap<String, String>();

	private static final String[] locationProperties = {
			"10 xg0,yg0,avd @EarDetector At the left ear",
			"5 xg0,yg0,avd @EarDetector At the right ear",

			"10 yg0,zg0,x5up @InFrontOfFaceDetector In front of your face, portrait orientation",
			"10 xg0,zg0,x5up @InFrontOfFaceDetector In front of your face, landscape orientation, turned on left side",
			"10 xg0,zg0,x5up @InFrontOfFaceDetector In front of your face, landscape orientation, turned on right side",

			////"10 xg0,x5up @SideLocationDetector In the left hand screen to the body landscape top to front",
			////"10 xg0,x5up @SideLocationDetector In the right hand screen to the body landscape top to front",
			"10 xg0,x5up,avd @SideLocationDetector On your left/in hand or a bag, screen to the body, landscape, top to front",
			"10 xg0,x5up,avd @SideLocationDetector On your right/in hand or a bag, screen to the body, landscape, top to front",

			"15 yg0,zg0,avd @FrontPocketDetector Pocket/the front one, screen to the body, portrait, upright",
			"15 xg0,yg0,zg0 @BackPocketDetector Pocket/the back one, screen to the body, portrait, upright",
			////"15 @FrontPocketDetector Front pocket back to the body portrait",
			////"15 @BackPocketDetector Back pocket back to the body portrait",

			"15 xg0,x5up,avd @SideLocationDetector Left side pocket/screen to the body, landscape, top to front",
			"15 xg0,x5up,avd @SideLocationDetector Right side pocket/screen to the body, landscape, top to front",

			"30 xg0,yg0,zg0,avd @BackpackDetector Backpack/screen to the body, portrait",
			"20 xg0,yg0,zg0,avd @BackpackDetector Backpack/back to the body, portrait",

			////"30 xg0,x5up @SideLocationDetector Laptop bag screen to the body landscape top to front",
			////"20 xg0,x5up @SideLocationDetector Laptop bag back to the body landscape top to front",
	};

	private String locationNames[];
	private int delays[];
	private String[] relevantParams;
	private boolean relevantParamsParsed;


	public LearningDetector(Context context, TextToSpeech tts, Logger log) {
		this.xv = new ArrayList<Float>();
		this.yv = new ArrayList<Float>();
		this.zv = new ArrayList<Float>();
		this.tts = tts;
		this.context = context;
		this.log = log;
		startTime = System.nanoTime();

		//openLogFile();
		openProfileFile();
		parseLocationProperties();
	}

	public LearningDetector(Context context, TextToSpeech tts, Logger log, File profileFile) {
		this.xv = new ArrayList<Float>();
		this.yv = new ArrayList<Float>();
		this.zv = new ArrayList<Float>();
		this.tts = tts;
		this.context = context;
		this.log = log;
		startTime = System.nanoTime();

		//openLogFile();
		loadSavedProfile(profileFile);
		parseLocationProperties();
	}

	private void parseLocationProperties() {

		int n = locationProperties.length;
		locationNames = new String[n];
		delays = new int[n];
		relevantParams = new String[n];

		for (int i = 0; i < n; i++) {
			String[] timeAndParamsAndExtendedTag = locationProperties[i].split(" ", 3);

			int secondsToPrepare = Integer.parseInt(timeAndParamsAndExtendedTag[0]);
			delays[i] = TICKS_PER_SECOND * secondsToPrepare;
			relevantParams[i] = timeAndParamsAndExtendedTag[1];
			String extendedTag = timeAndParamsAndExtendedTag[2];

			if (extendedTag.startsWith("@")) {
				String[] detectorNameAndTag = extendedTag.substring(1).split(" ", 2);
				locationNames[i] = detectorNameAndTag[1];
				String detectorName = detectorNameAndTag[0];
				detectorNameForTag.put(locationNames[i], detectorName);
				log.log(locationNames[i] + " -> " + detectorName + "<br/>");
			} else
				locationNames[i] = extendedTag;
		}
	}

	/*private void openLogFile() {
		String dateStr = getDateAsString();
		File logFile = new File(context.getExternalFilesDir(null), "logs_" + dateStr + ".htm");
		try {
			logOs = new FileOutputStream(logFile, true);
			logPw = new PrintWriter(logOs);
		} catch (IOException e) {
			Log.e("ExternalStorage", "Error writing " + logFile, e);
		}
	}*/

	private void openProfileFile() {
		String dateStr = getDateAsString();
		File profileFile = new File(context.getExternalFilesDir(null), "profile_" + dateStr + ".txt");
		try {
			FileOutputStream profileOs = new FileOutputStream(profileFile, true);
			jsonWriter = new JsonWriter(
					new BufferedWriter(
							new OutputStreamWriter(profileOs)));
			jsonWriter.beginArray();
		} catch (IOException e) {
			Log.e("ExternalStorage", "Error writing " + profileFile, e);
		}
	}

	private void loadSavedProfile(File profileFile) {
		//File profileFile = new File(context.getExternalFilesDir(null), "saved_profile.txt");
		if (!profileFile.exists())
			return;

		try {
			FileInputStream profileIs = new FileInputStream(profileFile);
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(
					new BufferedReader(
							new InputStreamReader(
									profileIs)));

			StatCube[] c = gson.fromJson(reader, StatCube[].class);
			Collections.addAll(cubes, c);
			learning = false;
			//appendLog("Retrieved: " + c[0].contains() + " " +  c[1].contains());

		} catch (IOException e) {
			Log.e("ExternalStorage", "Error writing " + profileFile, e);
		}
	}

	private String getDateAsString() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		return sdf.format(cal.getTime());
	}

	/*private void appendLog(String log) {
		//logPw.write(log);
		//logPw.flush();
	}*/

	@Override
	synchronized public boolean feed(float x, float y, float z) {
		xv.add(x);
		yv.add(y);
		zv.add(z);

		boolean result = false;
		if (currentDetector != null)
			result = currentDetector.feed(x, y, z);

		int m = currentTick - lastLearnBeginTick;

		if (learning) {
			if (m == 0) {

				ticksToPrepare = delays[currentLocationBeingLearnt];
				String tag = locationNames[currentLocationBeingLearnt];
				cube = new StatCube(tag);
				cubes.add(cube);
				speak(makeFullSpeakableTag(tag));
			} else if (m == ticksToPrepare) {
				speak("Go!");
			} else if (m >= ticksToPrepare + TICKS_IN_WINDOW) {
				updateStats();
				learn();
				if (m == ticksToPrepare + TICKS_PER_LOCATION - 1) {
					log.log(cube.toString() + "<br/>");
					//appendLog(log);
					//parseRelevantParams();
					saveStatsToProfile();
					currentLocationBeingLearnt++;
					if (currentLocationBeingLearnt == locationProperties.length) {
						try {
							jsonWriter.endArray();
							jsonWriter.flush();
						} catch (IOException e) {
							Log.e("json", "endArray", e);
						}
						lastSpeakTick = currentTick + TICKS_BETWEEN_RECOGNITIONS;
						learning = false;
						speak("Ok, learning done");
					}
					lastLearnBeginTick = currentTick + 1;
				}
			}
		} else {
			if (!relevantParamsParsed)
				parseRelevantParams();

			updateStats();
			String msg = String.format("x > 0: %.1f, x > 12: %.1f, x < -12: %.1f<br/>" +
					"y > 0: %.1f, z > 0: %.1f, avd: %.1f <br/>" +
					"x 5%% dn: %.2f, x 5%% up: %.2f <br/>" +
					"z 5%% dn: %.2f, z 5%% up: %.2f <br/>" +
					"<font color='#888888'>t: %.3f #%d / %d</font><br/>",
					xg0, xg12, xlm12,
					yg0, zg0, avd,
					x5dn, x5up,
					z5dn, z5up,
					(System.nanoTime() - startTime) / 1e9, currentTick, lastSpeakTick);
			log.log(msg);

			boolean mustSpeak = currentTick >= lastSpeakTick + TICKS_BETWEEN_RECOGNITIONS || currentTick == lastSpeakTick;
			recognize(mustSpeak);
		}

		currentTick++;
		return result;
	}

	private String makeFullSpeakableTag(String tag) {
		return tag.replace("/", ", ");
	}

	private void parseRelevantParams() {
		for (int i = 0; i < relevantParams.length; i++) {
			String[] params = relevantParams[i].split(",");
			StatCube cube = cubes.get(i);
			for (Field field: StatCube.class.getFields()) {
				if (!field.getName().equals("tag") && !contains(params, field.getName())) {
					try {
						StatCube.Interval iv = (StatCube.Interval) field.get(cube);
						//Log.e("Field.get(cube)",(String)field.get(cube));
						iv.stretchToInfinity();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
		}
		relevantParamsParsed = true;
	}

	private boolean contains(String[] params, String name) {
		for (String s: params)
			if (s.equals(name))
				return true;
		return false;
	}

	private void recognize(boolean mustSpeak) {

		String resultingLocationTag;
		for (StatCube c : cubes) {
			if (c.contains(xg0, xg12, xlm12, yg0, zg0, x5dn, x5up, z5dn, z5up, avd)) {
				resultingLocationTag = c.tag;
				String color = "black";
				String prefix = "";
				if (mustSpeak) {
					speak(makeShortenedSpeakableTag(c.tag));
					color = "red";
					prefix = "SPOKEN: ";
					lastSpeakTick = currentTick;
					if (resultingLocationTag != null) {
						if (currentDetector == null ||
								!currentDetector.getClass().getName().equals(detectorNameForTag.get(resultingLocationTag)))
							currentDetector = tryCreateDetector(resultingLocationTag);
					}
				}
				log.log("<font color='" + color + "'>" + prefix + c.tag + "</font><br/>");
			}
		}
	}

	private String makeShortenedSpeakableTag(String tag) {
		return tag.split("/")[0];
	}

	private Detector tryCreateDetector(String tag) {
		String detectorName = detectorNameForTag.get(tag);
		if (detectorName != null) {
			Class<Detector> clazz;
			try {
				String packageName = getClass().getPackage().getName();
				clazz = (Class<Detector>) Class.forName(packageName + "." + detectorName);
				Detector result = clazz.newInstance();
				//appendLog(result.getClass().getName() + "<br/>");
				return result;
			} catch (ClassCastException e) {
				return null;
			} catch (ClassNotFoundException e) {
				return null;
			} catch (InstantiationException e) {
				return null;
			} catch (IllegalAccessException e) {
				return null;
			}
		}
		return null;
	}

	private void learn() {
		cube.put(xg0, xg12, xlm12, yg0, zg0, x5dn, x5up, z5dn, z5up, avd);
	}

	private void updateStats() {
		xg0 = getPercentageAbove(xv, 0);
		xg12 = getPercentageAbove(xv, 12);
		xlm12 = getPercentageBelow(xv, -12);
		yg0 = getPercentageAbove(yv, 0);
		zg0 = getPercentageAbove(zv, 0);
		x5dn = getQ5Dn(xv);
		x5up = getQ5Up(xv);
		z5dn = getQ5Dn(zv);
		z5up = getQ5Up(zv);
		avd = getAvd();
	}

	private void speak(String what) {
		tts.speak(what, TextToSpeech.QUEUE_ADD, null);
	}

	private void saveStatsToProfile() {
		Gson gson = new Gson();
		gson.toJson(cube, StatCube.class, jsonWriter);
		try {
			jsonWriter.flush();
		} catch (IOException e) {
			Log.e("json", "add element", e);
		}
	}

	private float getAvd() {
		float s = 0;
		int ws = getWindowSize(xv) - 1;
		if (ws == 0)
			return 0.0f;
		for (int i = xv.size() - ws; i < xv.size(); i++) {
			s += Math.abs(xv.get(i) - xv.get(i-1));
			s += Math.abs(yv.get(i) - yv.get(i-1));
			s += Math.abs(zv.get(i) - zv.get(i-1));
		}
		return s / ws / 3;
	}

	private float getQ5Up(List<Float> vals) {
		int sz = TICKS_IN_WINDOW / 20;
		float top[] = new float[sz + 1];
		for (int i = 0; i <= sz; i++)
			top[i] = -99;
		int ws = getWindowSize(vals);
		for (int i = vals.size() - ws; i < vals.size(); i++) {
			int j;
			float v = vals.get(i);
			for (j = 1; j <= sz && top[j] < v; j++)
				top[j-1] = top[j];
			top[j-1] = v;
		}
		return top[1];
	}

	private float getQ5Dn(List<Float> vals) {
		int sz = TICKS_IN_WINDOW / 20;
		float[] bottom = new float[sz + 1];
		for (int i = 0; i <= sz; i++)
			bottom[i] = 99;
		int ws = getWindowSize(vals);
		for (int i = vals.size() - ws; i < vals.size(); i++) {
			int j;
			float v = vals.get(i);
			for (j = 1; j <= sz && bottom[j] > v; j++)
				bottom[j-1] = bottom[j];
			bottom[j-1] = v;
		}
		return bottom[1];
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

	private int getWindowSize(List<Float> v) {
		return Math.min(v.size(), TICKS_IN_WINDOW);
	}

	@Override
	synchronized public String getLog() {
		/*if (log.length() > 0) {
			String l = log;
			log = "";
			return l;
		} else*/
		return "";
	}

	@Override
	public int getSensorDelay() {
		return 1000000 / TICKS_PER_SECOND;
	}

	@Override
	public int getSensorType() {
		return Sensor.TYPE_ACCELEROMETER;
	}
}
