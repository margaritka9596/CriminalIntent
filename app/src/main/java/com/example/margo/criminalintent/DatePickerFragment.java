package com.example.margo.criminalintent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerFragment extends DialogFragment {
	private static final String TAG = "DatePickerFragment";
	public static final String EXTRA_DATE = "com.example.margo.criminalintent.date";
	private static final String ARG_DATE = "date";

	private DatePicker mDatePicker;
	private Button mOkDateButton;

	public static DatePickerFragment newInstance(Date date) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_DATE, date);

		DatePickerFragment fragment = new DatePickerFragment();
		fragment.setArguments(args);
		return fragment;
	}

	//second version with overriding onCreateView() instead of onCreateDialog
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_date, container, false);

		Date date = (Date) getArguments().getSerializable(ARG_DATE);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_date_picker);
		mDatePicker.init(year, month, day, null);

		mOkDateButton = (Button) v.findViewById(R.id.dialog_date_date_picker_ok_button);
		mOkDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int year = mDatePicker.getYear();
				int month = mDatePicker.getMonth();
				int day = mDatePicker.getDayOfMonth();
				Date date = new GregorianCalendar(year, month, day).getTime();
				sendResult(Activity.RESULT_OK, date);
				dismiss();
			}
		});

		return v;
	}

	//first version with overriding onCreateDialog()
	/*@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Date date = (Date) getArguments().getSerializable(ARG_DATE);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);

			View v = LayoutInflater.from(getActivity())
					.inflate(R.layout.dialog_date, null);

			mDatePicker = (DatePicker) v.findViewById(R.id.dialog_date_date_picker);
			mDatePicker.init(year, month, day, null);

			return new AlertDialog.Builder(getActivity())
					.setView(v)
					.setTitle(R.string.date_picker_title)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									int year = mDatePicker.getYear();
									int month = mDatePicker.getMonth();
									int day = mDatePicker.getDayOfMonth();
									Date date = new GregorianCalendar(year, month, day).getTime();
									sendResult(Activity.RESULT_OK, date);
									Log.d(TAG, "AlertDialog.setPositiveButton.OnClick() is called");
								}
							})
					.create();
		}
	*/

	private void sendResult(int resultCode, Date date) {
		if (getTargetFragment() == null) {
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(EXTRA_DATE, date);

		getTargetFragment()
				.onActivityResult(getTargetRequestCode(), resultCode, intent);
		Log.d(TAG, "sendResult() is called");
	}
}
