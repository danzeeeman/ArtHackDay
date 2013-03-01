package com.arthackday.killerapp.camera;

import android.hardware.Camera;

public class HiddenCameraCallBack implements Camera.PreviewCallback {
	public void onPreviewFrame(byte[] data, Camera camera) {
		// Process the camera data here
	}
}