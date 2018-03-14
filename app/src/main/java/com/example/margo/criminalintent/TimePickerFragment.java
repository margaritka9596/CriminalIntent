package com.example.margo.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import org.threeten.bp.LocalTime;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class TimePickerFragment extends DialogFragment {
	private static String TAG = "TimePickerFragment";
	private static String ARG_TIME = "time";
	public static String EXTRA_TIME = "com.example.margo.criminalintent.time";

	private TimePicker mTimePicker;

	public static TimePickerFragment newInstance(LocalTime time) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_TIME, time);

		TimePickerFragment fragment = new TimePickerFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LocalTime time = (LocalTime) getArguments().getSerializable(ARG_TIME);

		int hour = time.getHour();
		int minute = time.getMinute();

		View v = LayoutInflater.from(getActivity())
				.inflate(R.layout.dialog_time, null);

		mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_time_picker);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mTimePicker.setHour(hour);
			mTimePicker.setMinute(minute);
		} else {
			mTimePicker.setCurrentHour(hour);
			mTimePicker.setCurrentMinute(minute);
		}

		return new AlertDialog.Builder(getActivity())
				.setView(v)
				.setTitle(R.string.time_picker_title)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								int hour = 0, minute = 0;
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
									hour = mTimePicker.getHour();
									minute = mTimePicker.getMinute();
								} else {
									hour = mTimePicker.getCurrentHour();
									minute = mTimePicker.getCurrentMinute();
								}
								LocalTime time = LocalTime.of(hour, minute);
//								Time time = new Time( TimeUnit.MILLISECONDS.convert(hour, TimeUnit.HOURS) + TimeUnit.MILLISECONDS.convert(minute, TimeUnit.MINUTES));
								//.  new GregorianCalendar(hour, minute).set;
								sendResult(Activity.RESULT_OK, time);
							}
						})
				.create();
	}

	private void sendResult(int resultCode, LocalTime time) {
		if (getTargetFragment() == null) {
			return;
		}

		Intent intent = new Intent();
		intent.putExtra(EXTRA_TIME, time);

		getTargetFragment()
				.onActivityResult(getTargetRequestCode(), resultCode,intent);
		Log.d(TAG, "sendResult() is called");
	}
}
