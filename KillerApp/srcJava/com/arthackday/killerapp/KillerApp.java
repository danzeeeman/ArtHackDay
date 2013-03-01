package com.arthackday.killerapp;

import com.arthackday.killerapp.gps.IStrictMode;
import com.arthackday.killerapp.gps.PlatformSpecificImplementationFactory;
import com.arthackday.killerapp.util.KillerConstants;
import com.google.android.gcm.GCMRegistrar;

import android.app.Application;
import android.view.View;

public class KillerApp extends Application {

	public static String SENDER_ID = "1031540704045";

	public static final String SHARED_PREF_NAME = "com.arthackday.killerapp.PREF";

	@Override
	public final void onCreate() {
		super.onCreate();
		if (KillerConstants.DEVELOPER_MODE) {
			IStrictMode strictMode = PlatformSpecificImplementationFactory
					.getStrictMode();
			if (strictMode != null){
				strictMode.enableStrictMode();
			}
		}
		
	}
}
