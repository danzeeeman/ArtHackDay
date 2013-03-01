package com.arthackday.killerapp;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

import com.arthackday.killerapp.camera.CameraHiddenPreview;
import com.arthackday.killerapp.camera.CameraPreview;
import com.arthackday.killerapp.camera.CaptureVideo;
import com.arthackday.killerapp.camera.HiddenCameraCallBack;
import com.arthackday.killerapp.fragments.MenuFrag;
import com.arthackday.killerapp.fragments.MenuMap;
import com.arthackday.killerapp.fragments.PageMapFragment;
import com.arthackday.killerapp.fragments.ScreenSlidePageFragment;
import com.arthackday.killerapp.gps.PlatformSpecificImplementationFactory;
import com.arthackday.killerapp.sharedpreference.SharedPreferenceSaver;
import com.arthackday.killerapp.util.KillerConstants;
import com.google.android.gcm.GCMRegistrar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class KillerHomeScreen extends FragmentActivity implements
		TextToSpeech.OnInitListener {

	/**
	 * The number of pages (wizard steps) to show in this demo.
	 */
	private static final int NUM_PAGES = 5;

	/**
	 * The pager widget, which handles animation and allows swiping horizontally
	 * to access previous and next wizard steps.
	 */
	private ViewPager mPager;
	private ViewPager mMenu;

	/**
	 * The pager adapter, which provides the pages to the view pager widget.
	 */
	private PagerAdapter mPagerAdapter;
	private PagerAdapter mMenuAdapter;

	Camera camera;
	File file;

	MediaRecorder mediaRecorder;
	FrameLayout cameraPreviewFrame;

	boolean recording;

	CameraPreview cameraPreview;
	private boolean godmode;

	protected SharedPreferences prefs;
	protected Editor prefsEditor;
	protected SharedPreferenceSaver sharedPreferenceSaver;

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.killer_home_screen);

		GCMRegistrar.checkDevice(this);

		if (true) {
			GCMRegistrar.checkManifest(this);
		}

		final String regId = GCMRegistrar.getRegistrationId(this);

		if (regId.length() == 0) {
			GCMRegistrar.register(this, KillerApp.SENDER_ID);
		} else {
			Log.d(getClass().getSimpleName(), "Existing registration: " + regId);
			Toast.makeText(this, regId, Toast.LENGTH_LONG).show();
		}

		// Get a reference to the Shared Preferences and a Shared Preference
		// Editor.
		prefs = getSharedPreferences(KillerConstants.SHARED_PREFERENCE_FILE,
				Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();

		// Instantiate a SharedPreferenceSaver class based on the available
		// platform version.
		// This will be used to save shared preferences
		sharedPreferenceSaver = PlatformSpecificImplementationFactory
				.getSharedPreferenceSaver(this);

		// Save that we've been run once.
		prefsEditor.putBoolean(KillerConstants.SP_KEY_RUN_ONCE, true);
		sharedPreferenceSaver.savePreferences(prefsEditor, false);

		// cameraPreviewFrame = (FrameLayout) findViewById(R.id.fakepreview);

		// Instantiate a ViewPager and a PagerAdapter.
		mPager = (ViewPager) findViewById(R.id.pager);
		mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mPager.setOffscreenPageLimit(5);

		mMenu = (ViewPager) findViewById(R.id.topmenu);
		mMenuAdapter = new MenuPagerAdapter(getSupportFragmentManager());
		mMenu.setAdapter(mMenuAdapter);
		mMenu.setOffscreenPageLimit(5);

		mMenu.setAnimationCacheEnabled(true);
		mMenu.setOnPageChangeListener(new OnPageChangeListener() {
			private int mPosition;

			@Override
			public void onPageSelected(int position) {
				mPosition = position;
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_IDLE) {
					mPager.setCurrentItem(mPosition, true);
				}

			}
		});

		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			private int mPosition;

			@Override
			public void onPageSelected(int position) {
				mPosition = position;
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_IDLE) {
					mMenu.setCurrentItem(mPosition, true);
				}

			}
		});

	}

	@Override
	public void onBackPressed() {
		if (mPager.getCurrentItem() == 0) {
			// If the user is currently looking at the first step, allow the
			// system to handle the
			// Back button. This calls finish() on this activity and pops the
			// back stack.
			super.onBackPressed();
		} else {
			// Otherwise, select the previous step.
			mPager.setCurrentItem(mPager.getCurrentItem() - 1);
		}
	}

	/**
	 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects,
	 * in sequence.
	 */
	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position % 3 == 0) {
				return new PageMapFragment();
			} else if (position % 4 == 0) {
				return new MenuFrag();
			} else {
				return new ScreenSlidePageFragment();
			}

		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	private class MenuPagerAdapter extends FragmentStatePagerAdapter {
		public MenuPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return new ScreenSlidePageFragment();
		}

		@Override
		public int getCount() {
			return NUM_PAGES;
		}
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub

	}

	void initCamera(Camera camera) {

		// this.camera = camera;
		//
		// // create a preview for our camera
		// this.camera = camera;
		// // create a preview for our camera
		// this.cameraPreview = new CameraPreview(KillerHomeScreen.this,
		// this.camera);
		// // add the preview to our preview frame
		// this.cameraPreviewFrame.addView(this.cameraPreview, 0);
		//
		// if (!recording) {
		// startRecording();
		// }
	}

	void releaseCamera() {
		if (this.camera != null) {
			this.camera.lock(); // unnecessary in API >= 14
			this.camera.stopPreview();
			this.camera.release();
			this.camera = null;
		}
	}

	void releaseMediaRecorder() {
		if (this.mediaRecorder != null) {
			this.mediaRecorder.reset(); // clear configuration (optional here)
			this.mediaRecorder.release();
			this.mediaRecorder = null;
		}
	}

	void releaseResources() {
		this.releaseMediaRecorder();
		this.releaseCamera();
	}

	@Override
	public void onPause() {
		super.onPause();
		this.releaseResources();
	}

	// gets called by the button press
	public void startRecording() {
		Log.d("REMOVE BEFORE COMMIT", "startRecording()");
		// we need to unlock the camera so that mediaRecorder can use it
		this.camera.unlock(); // unnecessary in API >= 14
		// now we can initialize the media recorder and set it up with our
		// camera
		this.mediaRecorder = new MediaRecorder();
		this.mediaRecorder.setCamera(this.camera);

		this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		this.mediaRecorder.setProfile(CamcorderProfile
				.get(CamcorderProfile.QUALITY_HIGH));
		this.mediaRecorder.setOutputFile(this.initFile().getAbsolutePath());
		this.mediaRecorder.setPreviewDisplay(cameraPreview.getHolder()
				.getSurface());
		this.mediaRecorder.setOnInfoListener(new OnInfoListener() {

			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
					stopRecording();
				}

			}
		});
		this.mediaRecorder.setMaxDuration(300);
		this.mediaRecorder.setMaxFileSize(5000);
		try {
			recording = true;
			this.mediaRecorder.prepare();
			// start the actual recording
			// throws IllegalStateException if not prepared
			this.mediaRecorder.start();
			Toast.makeText(this, R.string.recording, Toast.LENGTH_SHORT).show();
			// enable the stop button by indicating that we are recordin
		} catch (Exception e) {
			Log.wtf("REMOVE BEFORE COMMIT", "Failed to prepare MediaRecorder",
					e);
			Toast.makeText(this, R.string.cannot_record, Toast.LENGTH_SHORT)
					.show();
			this.releaseMediaRecorder();
		}
	}

	// gets called by the button press
	public void stopRecording() {
		recording = false;
		Log.d("REMOVE BEFORE COMMIT", "stopRecording()");
		assert this.mediaRecorder != null;
		try {
			this.mediaRecorder.stop();
			Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
			// we are no longer recording
		} catch (RuntimeException e) {
			// the recording did not succeed
			Log.w("REMOVE BEFORE COMMIT", "Failed to record", e);
			if (this.file != null && this.file.exists() && this.file.delete()) {
				Log.d("REMOVE BEFORE COMMIT",
						"Deleted " + this.file.getAbsolutePath());
			}
			return;
		} finally {
			this.releaseMediaRecorder();
		}
		if (this.file == null || !this.file.exists()) {
			startRecording();
		} else {
			try {
				Log.i("REMOVE BEFORE COMMIT", String.format(
						"abs path %s \n CanonicalPath %s",
						file.getAbsolutePath(), file.getCanonicalPath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private File initFile() {
		File dir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
				this.getClass().getPackage().getName());
		if (!dir.exists() && !dir.mkdirs()) {
			Log.wtf("REMOVE BEFORE COMMIT",
					"Failed to create storage directory: "
							+ dir.getAbsolutePath());
			Toast.makeText(KillerHomeScreen.this, R.string.cannot_record,
					Toast.LENGTH_SHORT);
			this.file = null;
		} else {
			this.file = new File(dir.getAbsolutePath(), String.format(
					"ArtHackDay-%d.mp4", System.currentTimeMillis()));
		}
		return this.file;
	}
}
