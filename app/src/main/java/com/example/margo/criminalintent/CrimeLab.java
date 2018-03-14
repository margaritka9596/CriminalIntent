package com.example.margo.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.example.margo.criminalintent.database.CrimeBaseHelper;
import com.example.margo.criminalintent.database.CrimeCursorWrapper;
import com.example.margo.criminalintent.database.CrimeDbSchema;
import com.example.margo.criminalintent.database.CrimeDbSchema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeLab {
	private static final String TAG = "CrimeLab";
	private static CrimeLab sCrimeLab;

	private Context mContext;
	private SQLiteDatabase mDatabase;

	public static CrimeLab get(Context context) {
		if (sCrimeLab == null) {
			sCrimeLab = new CrimeLab(context);
		}
		return sCrimeLab;
	}

	private CrimeLab(Context context) {
		mContext = context.getApplicationContext();
		mDatabase = new CrimeBaseHelper(mContext)
				.getWritableDatabase();
	}

	public void addCrime(Crime crime) {
		ContentValues values = getContentValues(crime);

		mDatabase.insert(CrimeTable.NAME, null, values);
	}

	public void deleteCrime(UUID id) {
	/*	for (Crime crime : mCrimes) {
			if (crime.getId().equals(id)) {
				Log.d(TAG, "equal");
				Log.d(TAG, "size = " + mCrimes.size());
				mCrimes.remove(crime);
				Log.d(TAG, "size = " + mCrimes.size());
				return;
			}
		}*/
		mDatabase.delete(CrimeTable.NAME, CrimeTable.Cols.UUID + " = ?",
				new String[]{id.toString()});
	}

	public List<Crime> getCrimes() {
		List<Crime> crimes = new ArrayList<>();

		CrimeCursorWrapper cursor = queryCrimes(null, null);

		try {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				crimes.add(cursor.getCrime());
				cursor.moveToNext();
			}
		} finally {
			cursor.close();
		}

		return crimes;
	}

	public Crime getCrime(UUID id) {
		CrimeCursorWrapper cursor = queryCrimes(
				CrimeTable.Cols.UUID + " = ?",
				new String[]{id.toString()}
		);

		try{
			if (cursor.getCount() == 0) {
				return null;
			}

			cursor.moveToFirst();
			return cursor.getCrime();
		} finally {
			cursor.close();
		}
	}

	public void updateCrime(Crime crime) {
		String uuidString = crime.getId().toString();
		ContentValues values = getContentValues(crime);

		mDatabase.update(CrimeTable.NAME, values,
				CrimeTable.Cols.UUID + " = ?",
				new String[]{uuidString});
	}

	private static ContentValues getContentValues(Crime crime) {
		ContentValues values = new ContentValues();

		values.put(CrimeTable.Cols.UUID, crime.getId().toString());
		values.put(CrimeTable.Cols.TITLE, crime.getTitle());
		values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
		values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);
		values.put(CrimeTable.Cols.SUSPECT, crime.getSuspect());
//		values.put(CrimeTable.Cols.SUSPECT_NUMBER, crime.getSuspectNumber());
		values.put(CrimeTable.Cols.SUSPECT_ID, crime.getSuspectId());

		return values;
	}

	private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
		Cursor cursor = mDatabase.query(
				CrimeTable.NAME,
				null, //Columns - null выбирает все столбцы
				whereClause,
				whereArgs,
				null, //groupBy
				null, // having
				null // orderBy
		);
		return new CrimeCursorWrapper(cursor);
	}

	//Этот код не создает никакие файлы в файловой системе.
	//Он только возвращает объекты File, представляющие нужные места.
	public File getPhotoFile(Crime crime) {
		File externalFileDir = mContext.
				getExternalFilesDir(Environment.DIRECTORY_PICTURES);

		//проверяет наличие внешнего хранилища для сохранения данных
		if (externalFileDir == null) {
			return null;
		}

		return new File(externalFileDir, crime.getPhotoFilename());
	}
}

