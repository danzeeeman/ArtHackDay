package com.arthackday.killerapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.google.android.gcm.GCMRegistrar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.provider.MediaStore.Files.FileColumns;
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
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

@SuppressLint("ShowToast")
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class GodMode extends FragmentActivity implements
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
	private Uri fileUri;

	@Override
	protected void onResume() {
		super.onResume();
		GCMRegistrar.checkDevice(this);

		if (true) {
			GCMRegistrar.checkManifest(this);
		}

		final String regId = GCMRegistrar.getRegistrationId(this);

		if (regId.length() == 0) {
			GCMRegistrar.register(this, KillerApp.SENDER_ID);
		} else {
			Log.d(getClass().getSimpleName(), "Existing registration: " + regId);
		}

		godmode = true;

		// initialize the camera in background, as this may take a while
		new AsyncTask<Void, Void, Camera>() {

			@Override
			protected Camera doInBackground(Void... params) {
				try {
					Camera camera = Camera.open(1);
					return camera == null ? Camera.open(0) : camera;
				} catch (RuntimeException e) {
					Log.wtf("-GOD-MODE-", "Failed to get camera", e);
					return null;
				}

			}

			@Override
			protected void onPostExecute(Camera camera) {
				if (camera == null) {
					Log.e("-God-Mode-", "Camera Failed");
				} else {
					initCamera(camera);
				}
			}
		}.execute();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.godmode);

		fileUri = getOutputMediaFileUri(FileColumns.MEDIA_TYPE_IMAGE);

		cameraPreviewFrame = (FrameLayout) findViewById(R.id.video);

		cameraPreviewFrame.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				camera.takePicture(null, null, mPicture);
				return false;
			}
		});

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
			if (position % 2 == 0) {
				return new PageMapFragment();
			} else {
				return new MenuMap();
			}

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

		this.camera = camera;

		// create a preview for our camera
		this.camera = camera;
		// create a preview for our camera
		this.camera.setDisplayOrientation(90);
		this.cameraPreview = new CameraPreview(GodMode.this, this.camera);
		// add the preview to our preview frame
		this.cameraPreviewFrame.addView(this.cameraPreview, 0);

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
		this.mediaRecorder.setPreviewDisplay(this.cameraPreview.getHolder()
				.getSurface());
		this.mediaRecorder.setOnInfoListener(new OnInfoListener() {

			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
					stopRecording();
					recording = false;
				}

			}
		});
		this.mediaRecorder.setMaxDuration(60);
		this.mediaRecorder.setMaxFileSize(500);
		try {
			recording = true;
			mediaRecorder.prepare();
			// start the actual recording
			// throws IllegalStateException if not prepared
			mediaRecorder.start();
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
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
				this.getClass().getPackage().getName());
		if (!dir.exists() && !dir.mkdirs()) {
			Log.wtf("REMOVE BEFORE COMMIT",
					"Failed to create storage directory: "
							+ dir.getAbsolutePath());
			Toast.makeText(GodMode.this, R.string.cannot_record,
					Toast.LENGTH_SHORT);
			this.file = null;
		} else {
			this.file = new File(dir.getAbsolutePath(), String.format(
					"ArtHackDay-%d.mp4", System.currentTimeMillis()));
		}
		return this.file;
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null) {
				Log.d("ARTHACKDAY",
						"Error creating media file, check storage permissions: ");
				return;
			}
			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d("ARTHACKDAY", "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d("ARTHACKDAY", "Error accessing file: " + e.getMessage());
			}

			Intent i = new Intent();
			i.setClassName("com.arthackday.killerapp",
					"com.arthackday.killerapp.KillerHomeScreen");
			startActivity(i);
		}
	};

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"ArtHackDay");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("ArtHackDay", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

}
