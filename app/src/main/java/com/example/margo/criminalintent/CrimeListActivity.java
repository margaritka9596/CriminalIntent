package com.example.margo.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

public class CrimeListActivity extends SingleFragmentActivity
implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks{
	private static final String TAG = "CrimeListActivity";

	@Override
	protected Fragment createFragment() {
		return new CrimeListFragment();
	}

	@Override
	protected int getLayoutResId() {
		return R.layout.activity_masterdetail;
	}

	public static Intent newIntent(Context packageContext, boolean showSubtitle, boolean isCrimeDeleted) {
		Intent intent = new Intent(packageContext, CrimeListActivity.class);
		intent.putExtra(CrimeListFragment.EXTRA_SUBTITLE_VISIBLE, showSubtitle);
		intent.putExtra(CrimePagerActivity.ARG_DELETED_CRIME_ID, isCrimeDeleted);
		return intent;
	}

	@Override
	public void onCrimeSelected(Crime crime) {
		/*необходимо знать лишь то, имеется ли у макета контейнер
		detail_fragment_container для размещения CrimeFragment.*/
		if (findViewById(R.id.detail_fragment_container) == null) {
			Intent intent = CrimePagerActivity.newIntent(this, crime.getId());
			startActivity(intent);
		} else {
			Fragment newDetail = CrimeFragment.newInstance(crime.getId());

			getSupportFragmentManager().beginTransaction()
			.replace(R.id.detail_fragment_container, newDetail)
			.commit();
		}
	}

	public void onCrimeUpdated(Crime crime) {
		CrimeListFragment listFragment = (CrimeListFragment) getSupportFragmentManager()
				.findFragmentById(R.id.fragment_container);
		listFragment.updateUI();
	}
}
