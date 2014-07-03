package com.example.android.pedometer;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class FileLogger implements Logger {
	private FileOutputStream logOs;
	private PrintWriter logPw;

	public FileLogger(Context context, String fileName) {
		File logFile = new File(context.getExternalFilesDir(null), fileName);
		try {
			logOs = new FileOutputStream(logFile, true);
			logPw = new PrintWriter(logOs);
		} catch (IOException e) {
			Log.e("ExternalStorage", "Error writing " + logFile, e);
		}
	}

	public void log(String l) {
		logPw.write(l);
		logPw.flush();
	}
}
