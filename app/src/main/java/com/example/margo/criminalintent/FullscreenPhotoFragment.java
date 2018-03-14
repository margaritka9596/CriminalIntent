package com.example.margo.criminalintent;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

public class FullscreenPhotoFragment extends DialogFragment {
	private static String ARG_PHOTO_FILE = "photo_file";

	private ImageView mPhotoView;

	public static FullscreenPhotoFragment newInstance(File file) {
		Bundle args = new Bundle();
		args.putSerializable(ARG_PHOTO_FILE, file);

		FullscreenPhotoFragment fragment = new FullscreenPhotoFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_fullscreen_photo, container, false);

		File file = (File) getArguments().getSerializable(ARG_PHOTO_FILE);

		mPhotoView = (ImageView) v.findViewById(R.id.dialog_fullscreen_photo_image);

		if (file == null || !file.exists()) {
			mPhotoView.setImageDrawable(null);
		} else {
			Bitmap bitmap = PictureUtils.getScaleBitmap(file.getPath(),
					getActivity());
			mPhotoView.setImageBitmap(bitmap);
		}

		mPhotoView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return v;
	}
}
