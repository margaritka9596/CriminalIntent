package com.example.margo.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
	private static final String TAG = "CrimeListFragment";
	private static final int REQUEST_TIME = 0, REQUEST_CRIME_ID = 1;
	private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";
	public static final String EXTRA_SUBTITLE_VISIBLE = "com.example.margo.criminalintent.subtitle_visible";


	private RecyclerView mCrimeRecyclerView;
	private CrimeAdapter mAdapter;
	private boolean mSubtitleVisible;
	private boolean mIsCrimeDeleted;
	private LinearLayout mLinearLayout;
	private Button mAddCrimeButton;
	private Callbacks mCallbacks;

	/*обязательный интерфейс для активности хоста*/
	public interface Callbacks{
		void onCrimeSelected(Crime crime);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		Activity activity = (Activity) context;
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_crime_list, container, false);


		mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);
		mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		if (savedInstanceState != null) {
			mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
		}
		/*Challenge 3*/
		mLinearLayout = (LinearLayout) view.findViewById(R.id.fragment_crime_list_linear_layout);
		mAddCrimeButton = (Button) view.findViewById(R.id.new_crime);

		updateUI();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateUI();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime_list, menu);

		MenuItem subtitleItem = menu.findItem(R.id.menu_item_show_subtitle);
		if (mSubtitleVisible) {
			subtitleItem.setTitle(R.string.hide_subtitle);
		} else {
			subtitleItem.setTitle(R.string.show_subtitle);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_item_new_crime:
				Crime crime = new Crime();
				CrimeLab.get(getActivity()).addCrime(crime);
				/*Intent intent = CrimePagerActivity
						.newIntent(getActivity(), crime.getId(), mIsCrimeDeleted);
				startActivity(intent);*/
				updateUI();
				mCallbacks.onCrimeSelected(crime);
				return true;
			case R.id.menu_item_show_subtitle:
				mSubtitleVisible = !mSubtitleVisible;
				getActivity().invalidateOptionsMenu();
				updateSubtitle();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void updateSubtitle() {
		CrimeLab crimeLab = CrimeLab.get(getActivity());
		int crimeCount = crimeLab.getCrimes().size();
		String subtitle = getResources()
				.getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);

		if (!mSubtitleVisible) {
			subtitle = null;
		}
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		activity.getSupportActionBar().setSubtitle(subtitle);
	}

	public void updateUI() {
		updateSubtitle();

		CrimeLab crimeLab = CrimeLab.get(getActivity());
		List<Crime> crimes = crimeLab.getCrimes();

		if (mAdapter == null) {
			mAdapter = new CrimeAdapter(crimes);
			mCrimeRecyclerView.setAdapter(mAdapter);
		} else {
			mAdapter.setCrimes(crimes);
			mAdapter.notifyDataSetChanged();
		}

		if (CrimeLab.get(getActivity()).getCrimes().size() == 0) {
			mLinearLayout.setVisibility(View.VISIBLE);
			mAddCrimeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					addCrime();
				}
			});
		} else {
			mLinearLayout.setVisibility(View.GONE);
		}

	}

	private void addCrime() {
		Crime crime = new Crime();
		CrimeLab.get(getActivity()).addCrime(crime);
		Intent intent = CrimePagerActivity
				.newIntent(getActivity(), crime.getId(), mIsCrimeDeleted);
		startActivity(intent);
	}

	private class CrimeHolder extends RecyclerView.ViewHolder
			implements View.OnClickListener {

		private TextView mTitleTextView;
		private TextView mDateTextView;
		private TextView mTimeTextView;
		private CheckBox mSolvedCheckBox;
		private Crime mCrime;

		public CrimeHolder(View itemView) {
			super(itemView);
			itemView.setOnClickListener(this); //?this

			mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
			mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_date_text_view);
			mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_check_box);
		}

		public void bindCrime(Crime crime) {
			mCrime = crime;
			mTitleTextView.setText(mCrime.getTitle());
			mDateTextView.setText(DateFormat.format("EEEE ,MMM d, yyyy.", mCrime.getDate()));
			mSolvedCheckBox.setChecked(mCrime.isSolved());
		}

		@Override
		public void onClick(View v) {
			/*Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId(), mIsCrimeDeleted);
			startActivityForResult(intent, REQUEST_CRIME_ID);*/
			mCallbacks.onCrimeSelected(mCrime);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK) {
			return;
		} else {
			if (requestCode == REQUEST_CRIME_ID) {
				UUID crimeId = (UUID) data
						.getSerializableExtra(CrimePagerActivity.EXTRA_CRIME_ID);

				int crimePosition = mAdapter.getItemPosition(crimeId);
				if (data.hasExtra(CrimeFragment.EXTRA_DELETED_CRIME_ID)) {
					mAdapter.deleteCrime(crimePosition);
				} else {
					mAdapter.notifyItemChanged(crimePosition);
				}
			}
		}
		updateUI();
	}

	private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

		private List<Crime> mCrimes;

		public CrimeAdapter(List<Crime> crimes) {
			mCrimes = crimes;
		}

		@Override
		public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
			View view = layoutInflater
					.inflate(R.layout.list_item_crime, parent, false);
			return new CrimeHolder(view);
		}

		@Override
		public void onBindViewHolder(CrimeHolder holder, int position) {
			Crime crime = mCrimes.get(position);
			holder.bindCrime(crime);
		}

		@Override
		public int getItemCount() {
			return mCrimes.size();
		}

		public void setCrimes(List<Crime> crimes) {
			mCrimes = crimes;
		}

		public int getItemPosition(UUID crimeId) {
			int position = 0;
			for (Crime crime : mCrimes) {
				if (crime.getId().equals(crimeId)) {
					return position;
				}
				++position;
			}
			return 0;
		}

		public void deleteCrime(int position) {
			mCrimes.remove(position);
			notifyItemRemoved(position);
		}
	}
}
