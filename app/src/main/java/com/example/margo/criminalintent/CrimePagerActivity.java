package com.example.margo.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;
import java.util.UUID;

public class CrimePagerActivity extends AppCompatActivity
    implements CrimeFragment.Callbacks{
	private static final String TAG = "CrimePagerActivity";
	public static final String EXTRA_CRIME_ID = "com.example.margo.criminalintent.crime_id";
	public static final String ARG_DELETED_CRIME_ID = "is_crime_deleted";

	private ViewPager mViewPager;
	private List<Crime> mCrimes;
	private boolean mSubtitleVisible;
	private boolean mIsCrimeDeleted;
	private UUID mCrimeId;
	//private CrimePagerAdapter mCrimePagerAdapter;

	public static Intent newIntent(Context packageContext, UUID crimeId) {
		Intent intent = new Intent(packageContext, CrimePagerActivity.class);
		intent.putExtra(EXTRA_CRIME_ID, crimeId);
		return intent;
	}
	public static Intent newIntent(Context packageContext, UUID crimeId, boolean isCrimeDeleted) {
		Intent intent = new Intent(packageContext, CrimePagerActivity.class);
		intent.putExtra(EXTRA_CRIME_ID, crimeId);
		intent.putExtra(ARG_DELETED_CRIME_ID, isCrimeDeleted);
		return intent;
	}

	@Override
	public void onCrimeUpdated(Crime crime) {

	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crime_pager);

		mCrimeId = (UUID) getIntent().getSerializableExtra(EXTRA_CRIME_ID);

		mViewPager = (ViewPager) findViewById(R.id.activity_crime_pager_view_pager);

//		mSubtitleVisible = (boolean) getIntent().getBooleanExtra(CrimeListFragment.EXTRA_SUBTITLE_VISIBLE, false);

		mCrimes = CrimeLab.get(this).getCrimes();
		FragmentManager fragmentManager = getSupportFragmentManager();
		mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
			@Override
			public Fragment getItem(int position) {
				Crime crime = mCrimes.get(position);
				return CrimeFragment.newInstance(crime.getId());
			}

			@Override
			public int getCount() {
				return mCrimes.size();
			}
		});

		for(int i = 0; i < mCrimes.size(); ++i) {
			if (mCrimes.get(i).getId().equals(mCrimeId)) {
				mViewPager.setCurrentItem(i);
				break;
			}
		}
	}

	@Override
	public void onBackPressed() {
		Intent data = new Intent();
		data.putExtra(EXTRA_CRIME_ID, mCrimeId);

		setResult(Activity.RESULT_OK, data);
		super.onBackPressed();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(EXTRA_CRIME_ID, mCrimeId);
	}
}
