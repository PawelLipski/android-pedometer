package com.example.android.pedometer;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

public class WalkActivity extends Activity implements TextToSpeech.OnInitListener {

	private int lastDigit = 0;

	private SensorManager sensorManager;
	private SensorChangeListener listener;
	private Detector detector;

	private SoundPool soundPool;
	private boolean loaded;

	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakeLock;

	private TextToSpeech tts;
	private Logger log;


	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.walk);

		TextView caption = (TextView) findViewById(R.id.caption);
		String captionText = getIntent().getStringExtra("CHOICE_NAME");
		caption.setText(captionText);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, getClass().getName());

		loaded = false;
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
									   int status) {
				loaded = true;
			}
		});

		String logName = getIntent().getStringExtra("LOG_FILE");
		log = new FileLogger(this, logName);
		log.log("<b>" + captionText + "</b><br/>");

	}

	@Override
	protected void onResume() {
		super.onResume();

		mWakeLock.acquire();

		tts = new TextToSpeech(this, this);

		if (detector != null)
			setupDetector(detector);
		else {
			int choice = getIntent().getIntExtra("CHOICE_NUMBER", 7);
			followUserChoice(choice);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (listener != null)
			unregisterListener();

		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}

		mWakeLock.release();
	}

	private void followUserChoice(int choice) {
		switch (choice) {
			case 0:
				File profileFile = new File(getExternalFilesDir(null), "saved_profile.txt");
				setupDetector(new LearningDetector(this, tts, log, profileFile));
				break;
			case 1:
				setupDetector(new LearningDetector(this, tts, log));
				break;
			case 2:
				setupDetector(new EarDetector());
				break;
			case 3:
				setupDetector(new InFrontOfFaceDetector());
				break;
			case 4:
				setupDetector(new FrontPocketDetector());
				break;
			case 5:
				setupDetector(new BackPocketDetector());
				break;
			case 6:
				setupDetector(new SideLocationDetector());
				break;
			case 7:
				setupDetector(new BackpackDetector());
				break;
			default:
				finish();
				return;
		}
	}

	@Override
	public void onBackPressed() {
	}

	private class SensorChangeListener implements SensorEventListener {

		private Detector detector;
		private final int soundId;

		public SensorChangeListener(Detector detector, int soundResId) {
			this.detector = detector;
			this.soundId = soundPool.load(WalkActivity.this, soundResId, 1);
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			Log.e("SensorChangeListener", "Sensor changed!");
			synchronized (WalkActivity.this) {
				if (event.sensor.getType() != detector.getSensorType())
					return;
			}

			Log.e("SensorChangeListener", detector.getClass().getName());
			boolean newStep;
			String logLine;

			synchronized (WalkActivity.this) {
				newStep = detector.feed(event.values[0], event.values[1], event.values[2]);
				logLine = detector.getLog();
				Log.e("SensorChangeListener", "newStep = " + newStep + ", loaded = " + loaded);
			}

			if (newStep)
				playNotificationSound(soundId);

			if (logLine.length() > 0)
				log.log(logLine + "<br/>");
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int i) {
		}
	}

	private void setupDetector(Detector detector) {
		this.detector = detector;

		Sensor sensor = sensorManager
				.getDefaultSensor(detector.getSensorType());
		if (listener != null)
			unregisterListener();
		listener = new SensorChangeListener(detector, R.raw.beep);

		sensorManager.registerListener(listener, sensor, detector.getSensorDelay());
	}


	public void digitClicked(View view) {
		TextView digitView = (TextView)view;

		int digit = Integer.parseInt((String) digitView.getText());
		Log.e(getClass().getName(), digit + " " + lastDigit);
		if (digit == lastDigit + 1) {
			lastDigit = digit;
			if (lastDigit == 9)
				finish();
		} else
			lastDigit = 0;
	}

	private void unregisterListener() {
		sensorManager.unregisterListener(listener);
		listener = null;
	}

	private void playNotificationSound(int soundId) {

		AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		float maxVolume = (float) audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		// Is the sound loaded already?
		if (loaded)
			soundPool.play(soundId, maxVolume, maxVolume, 1, 0, 1f);
	}


	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			}

		} else {
			Log.e("TTS", "Initialization Failed!");
		}

	}

}
