package com.example.margo.criminalintent;

import org.threeten.bp.LocalTime;

import java.sql.Time;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Crime {
	private UUID mId;
	private String mTitle;
	private Date mDate;
	//	private Time mTime;
	private LocalTime mTime;
	private boolean mSolved;
	private String mSuspect;
	private Long mSuspectId;

	public void setId(UUID id) {
		mId = id;
	}

	public UUID getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public Date getDate() {
		return mDate;
	}

	public void setDate(Date date) {
		mDate = date;
	}

	public LocalTime getTime() {
		return mTime;
	}

	public void setTime(LocalTime time) {
		mTime = time;
	}

	public boolean isSolved() {
		return mSolved;
	}

	public void setSolved(boolean solved) {
		mSolved = solved;
	}

	public String getSuspect() {
		return mSuspect;
	}

	public void setSuspect(String suspect) {
		mSuspect = suspect;
	}

	public Long getSuspectId() {
		return mSuspectId;
	}

	public void setSuspectId(Long suspectId) {
		mSuspectId = suspectId;
	}

	public Crime() {
		//Generate unique identifier
		this(UUID.randomUUID());
	}

	public Crime(UUID id) {
		mId = id;
		mDate = new Date();
		mTime = LocalTime.now();
	}

	public String getPhotoFilename() {
		return "IMG_" + getId().toString() + ".jpg";
	}
}
