package com.example.margo.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.margo.criminalintent.CrimeListFragment.Callbacks;
import com.example.margo.criminalintent.database.CrimeCursorWrapper;

import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.example.margo.criminalintent.CrimePagerActivity.EXTRA_CRIME_ID;

public class CrimeFragment extends Fragment
{
	private static final String TAG = "CrimeFragment";
	private static final String ARG_CRIME_ID = "crime_id";
	public static final String EXTRA_DELETED_CRIME_ID = "is_crime_deleted";
	public static final String EXTRA_CRIME_FRAGMENT = "com.example.margo.criminalintent.crimefragment";
	private static final String DIALOG_DATE = "DialogDate";
	private static final String DIALOG_TIME = "DialogTime";
	private static final String DIALOG_PHOTO = "DialogPhoto";
	private static final int REQUEST_DATE = 0, REQUEST_TIME = 1, REQUEST_FULL_PHOTO = 6;
	private static final int REQUEST_CONTACT = 2;
	private static final int REQUEST_PERMISSION_CONTACTS = 3;
	private static final int REQUEST_PERMISSION_CALL_PHONE = 4;
	private static final int REQUEST_PHOTO = 5;


	private Crime mCrime;
	private File mPhotoFile;
	private EditText mTitleField;
	private Button mDateButton;
	private Button mTimeButton;
	private CheckBox mSolvedCheckBox;
	private Button mReportButton;
	private Button mSuspectButton;
	private Button mCallToSuspectButton;
	private ImageButton mPhotoButton;
	private ImageView mPhotoView;
	private Point mPhotoViewSize;
	private Long mSuspectId;
	private Uri mSuspectNumber;
	private Callbacks mCallbacks;

	public interface Callbacks{
		void onCrimeUpdated(Crime crime);
	}

	public static CrimeFragment newInstance(UUID crimeId) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_CRIME_ID, crimeId);

		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		return fragment;
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
		UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
		mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
		mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);

		setHasOptionsMenu(true);
	}

	@Override
	public void onPause() {
		super.onPause();

		CrimeLab.get(getActivity())
				.updateCrime(mCrime);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_crime, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case (R.id.menu_item_delete_crime):
				UUID crimeId = mCrime.getId();

				CrimeLab.get(getActivity())
						.deleteCrime(crimeId);

				Intent data = new Intent();
				data.putExtra(EXTRA_CRIME_ID, crimeId);
				data.putExtra(EXTRA_DELETED_CRIME_ID, true);
				getActivity().setResult(Activity.RESULT_OK, data);

				getActivity().finish();
				return true;
			case (android.R.id.home):
				getActivity().finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}

	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_crime, container, false);

		mTitleField = (EditText) v.findViewById(R.id.crime_title);
		mTitleField.setText(mCrime.getTitle());
		mTitleField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mCrime.setTitle(s.toString());
				updateCrime();
			}

			@Override
			public void afterTextChanged(Editable s) {

			}

		});

		mDateButton = (Button) v.findViewById(R.id.crime_date);
		updateDate();
		mDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager manager = getFragmentManager();
				DatePickerFragment dialog = DatePickerFragment
						.newInstance(mCrime.getDate());
				dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				dialog.show(manager, DIALOG_DATE);
			}
		});

		mTimeButton = (Button) v.findViewById(R.id.crime_time);
		updateTime();
		mTimeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager manager = getFragmentManager();
				TimePickerFragment dialogTime = TimePickerFragment
						.newInstance(mCrime.getTime());
				dialogTime.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
				dialogTime.show(manager, DIALOG_TIME);
			}
		});

		mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
		mSolvedCheckBox.setChecked(mCrime.isSolved());
		mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//Set the crime's solved property
				mCrime.setSolved(isChecked);
				updateCrime();
			}
		});

		mReportButton = (Button) v.findViewById(R.id.crime_report);
		mReportButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Chapter 15. Implicit intent
				//построение интента "руками"
				/*Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
				intent.putExtra(Intent.EXTRA_SUBJECT,
						getString(R.string.crime_report_subject));
				intent = Intent.createChooser(intent, getString(R.string.send_report));
				*/

				//построение интента с помощбю класса ShareCompat
				Intent intent = ShareCompat.IntentBuilder.from(getActivity())
						.setType("text/plain")
						.setText(getCrimeReport())
						.setSubject(getString(R.string.crime_report_subject))
						.setChooserTitle(getString(R.string.send_report))
						.createChooserIntent();

				startActivity(intent);
			}
		});

		final Intent pickContact = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		//фиктивный код для проверки фильтра
//		pickContact.addCategory(Intent.CATEGORY_HOME);
		mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
		mSuspectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//вставка из статьи и форума на проверку разрешений
				//+в манифесте прописывать обязательно!
				int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
						Manifest.permission.READ_CONTACTS);
				if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
					startActivityForResult(pickContact, REQUEST_CONTACT);
				} else {
					Log.w(TAG, "permissionCheck != PackageManager.PERMISSION_GRANTED");
					requestPermissions(
							new String[]{Manifest.permission.READ_CONTACTS},
							REQUEST_PERMISSION_CONTACTS);
				}
			}
		});

		if (mCrime.getSuspect() != null) {
			mSuspectButton.setText(mCrime.getSuspect());
		}

		PackageManager packageManager = getActivity().getPackageManager();
		if (packageManager.resolveActivity(pickContact,
				PackageManager.MATCH_DEFAULT_ONLY) == null) {
			mSuspectButton.setEnabled(false);
		}

		//Chapter 15. Challenge2.
		final Intent callContact = new Intent(Intent.ACTION_CALL, ContactsContract.Contacts.CONTENT_URI);   //Intent.ACTION_DIAL - не сразу звонит
		mCallToSuspectButton = (Button) v.findViewById(R.id.crime_call_to_suspect);
		mCallToSuspectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
					Manifest.permission.CALL_PHONE);
			if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
					Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

					//Определение полей, значения которых должны быть возвращены запросом
					String[] queryFields = new String[]{
							ContactsContract.CommonDataKinds.Phone.NUMBER};
					String whereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
					mSuspectId = mCrime.getSuspectId();
					String[] args = {String.valueOf(mSuspectId)};

					//Выполнение запроса - contactUri здесь выполняет функции условия "where"
					Cursor cursor = getActivity().getContentResolver()
							.query(contactUri,
									queryFields,
									whereClause,
									args,
									null);
					try {
						if (cursor.getCount() == 0) {
							return;
						}
						cursor.moveToFirst();
						String number = cursor.getString(0);
						mSuspectNumber = Uri.parse("tel:" + number);
						//теперь неявный интент с номером телефона
						callContact.setData(mSuspectNumber);
						startActivity(callContact);
					} finally {
						cursor.close();
					}
			} else {
				Log.w(TAG, "permissionCheck != PackageManager.PERMISSION_GRANTED");
				requestPermissions(
						new String[]{Manifest.permission.CALL_PHONE},
						REQUEST_PERMISSION_CALL_PHONE);
			}
			}
		});

		//чет не варит башка
		//должно работать, блокируется заранее
		/*packageManager = getActivity().getPackageManager();
		if (packageManager.resolveActivity(callContact,
				PackageManager.MATCH_DEFAULT_ONLY) == null) {
			mCallToSuspectButton.setEnabled(false);
		}
		//version2
		boolean canCall = callContact.resolveActivity(packageManager) != null;
		mCallToSuspectButton.setEnabled(canCall);
		*/

		mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
		final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		boolean canTakePhoto = (mPhotoFile != null) &&
				(captureImage.resolveActivity(packageManager) != null);
		mPhotoButton.setEnabled(canTakePhoto);

		if (canTakePhoto) {
			Uri uri = Uri.fromFile(mPhotoFile);
			captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}

		mPhotoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(captureImage, REQUEST_PHOTO);
			}
		});

		mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
		//Chapter 16. Challenge 2
		mPhotoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				boolean isFirstPass = (mPhotoViewSize == null);
				mPhotoViewSize = new Point();
				mPhotoViewSize.set(mPhotoView.getWidth(), mPhotoView.getHeight());

				if (isFirstPass) {
					updatePhotoView();
				}
			}
		});

		mPhotoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager manager = getFragmentManager();
				FullscreenPhotoFragment dialog = FullscreenPhotoFragment
						.newInstance(mPhotoFile);
				dialog.setTargetFragment(CrimeFragment.this, REQUEST_FULL_PHOTO);
				dialog.show(manager, DIALOG_PHOTO);
			}
		});


		return v;
	}

	public void returnResult() {
		getActivity().setResult(Activity.RESULT_OK, null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_OK) {
			return;
		}

		if (requestCode == REQUEST_DATE) {
			Date date = (Date) data
					.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
			mCrime.setDate(date);
			updateCrime();
			updateDate();
		} else if (requestCode == REQUEST_TIME) {
			LocalTime time = (LocalTime) data
					.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
			mCrime.setTime(time);
			updateCrime();
			updateTime();
		} else if (requestCode == REQUEST_CONTACT && data != null) {
			Uri contactUri = data.getData();

			//Определение полей, значения которых должны быть возвращены запросом
			String[] queryFields = new String[]{
					ContactsContract.Contacts.DISPLAY_NAME,
					ContactsContract.Contacts._ID
			};
			//Выполнение запроса - contactUri здесь выполняет функции условия "where"
			Cursor cursor = getActivity().getContentResolver()
					.query(contactUri, queryFields, null, null, null);

			try {
				//проверка получения результатов
				if (cursor.getCount() == 0) {
					return;
				}
				//Извлечение первого столбца данных - имени подозреваемого
				cursor.moveToFirst();
				String suspect = cursor.getString(0);
				mSuspectId = cursor.getLong(1); //?
				mCrime.setSuspectId(mSuspectId);
				updateCrime();
				Log.w(TAG, "suspectId = " + mSuspectId + ", suspect name = " + suspect);
				mCrime.setSuspect(suspect);
				mSuspectButton.setText(suspect);
			} finally {
				cursor.close();
			}
		} else if (requestCode == REQUEST_PHOTO) {
			updateCrime();
			updatePhotoView();
		}
	}

	public void updateCrime() {
		CrimeLab.get(getActivity()).updateCrime(mCrime);
		mCallbacks.onCrimeUpdated(mCrime);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_PERMISSION_CONTACTS) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(pickContact, REQUEST_CONTACT);
			} else {
				Toast.makeText(this.getContext(), "Until you grant the permission, we cannot open the contacts", Toast.LENGTH_SHORT).show();
			}
		} else if (requestCode == REQUEST_PERMISSION_CALL_PHONE) {  //впринципе можно было оставить с одним reauestcode на два permission и сделать один универсальный toast
			if (grantResults.length <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this.getContext(), "Until you grant the permission, we cannot call to suspect", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void updateDate() {
		mDateButton.setText(DateFormat.format("EEEE ,MMM d, yyyy.", mCrime.getDate()));
	}

	private void updateTime() {
		mTimeButton.setText(mCrime.getTime().format(DateTimeFormatter.ofPattern("H:m:s")));
	}

	private String getCrimeReport() {

		String solvedString = null;
		if (mCrime.isSolved()) {
			solvedString = getString(R.string.crime_report_solved);
		} else {
			solvedString = getString(R.string.crime_report_unsolved);
		}

		String dateFormat = "EEE, MMM, dd";
		String dateString = DateFormat.format(dateFormat,
				mCrime.getDate()).toString();

		String suspect = mCrime.getSuspect();
		if (suspect == null) {
			suspect = getString(R.string.crime_report_no_suspect);
		} else {
			suspect = getString(R.string.crime_report_suspect, suspect);
		}

		String report = getString(R.string.crime_report, mCrime.getTitle(),
				dateString, solvedString, suspect);

		return report;
	}

	private void updatePhotoView() {
		if (mPhotoFile == null || !mPhotoFile.exists()) {
			mPhotoView.setImageDrawable(null);
		} else {
			/*Bitmap bitmap = PictureUtils.getScaleBitmap(mPhotoFile.getPath(),
					getActivity());*/
			Bitmap bitmap = (mPhotoViewSize == null) ?
					PictureUtils.getScaleBitmap(mPhotoFile.getPath(),
							getActivity()) :
					PictureUtils.getScaleBitmap(mPhotoFile.getPath(),
							mPhotoViewSize.x, mPhotoViewSize.y);
			mPhotoView.setImageBitmap(bitmap);
		}
	}
}
