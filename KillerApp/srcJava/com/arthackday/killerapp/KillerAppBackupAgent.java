package com.arthackday.killerapp;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

import com.arthackday.killerapp.util.KillerConstants;

public class KillerAppBackupAgent extends BackupAgentHelper {
	@Override
	public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, KillerConstants.SHARED_PREFERENCE_FILE);
		addHelper(KillerConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, helper);
		
	}
}
