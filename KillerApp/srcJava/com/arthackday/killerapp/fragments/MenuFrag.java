package com.arthackday.killerapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.arthackday.killerapp.R;
import com.arthackday.killerapp.camera.CaptureVideo;

public class MenuFrag extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.menu_frag,
				container, false);

		Button b = (Button) rootView.findViewById(R.id.games);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent();
				i.setClassName("com.arthackday.killerapp", "com.arthackday.killerapp.OFA");
				startActivity(i);
			}
		});

		return rootView;
	}
}
