package com.example.android.pedometer;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ShagomerActivity extends ListActivity {

	private String logName;

	private final static String[] VALUES = new String[]{
			"Load profile",
			"Create new profile",
			"To the ear",
			"In front of you",
			"Front pocket",
			"Back pocket",
			"Side (pocket, hand, bag)",
			"Backpack",
			"Quit"};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, VALUES);
		setListAdapter(adapter);

		logName = "logs_" + getDateAsString() + ".htm";
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		/*String[] values = Arrays.copyOf(VALUES, VALUES.length);
		values[position] = "   " + VALUES[position];
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);*/

		if (position == VALUES.length - 1)
			finish();
		else {
			Intent intent = new Intent(this, WalkActivity.class);
			intent.putExtra("CHOICE_NAME", ((TextView) v).getText());
			intent.putExtra("CHOICE_NUMBER", position);
			intent.putExtra("LOG_FILE", logName);
			startActivity(intent);
		}
	}

	private String getDateAsString() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		return sdf.format(cal.getTime());
	}
}
