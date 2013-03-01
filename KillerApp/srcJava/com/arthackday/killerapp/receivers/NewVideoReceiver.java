package com.arthackday.killerapp.receivers;

import com.arthackday.killerapp.service.KillerUploadingService;
import com.arthackday.killerapp.util.KillerConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NewVideoReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		 
		String path = intent.getStringExtra(KillerConstants.EXTRA_KEY_FILEPATH);
		Log.i("REMOVE BEFORE COMMIT",String.format("Blah new Intent()! %s", path));
		if (path != null) {
			Intent serviceIntent = new Intent(context,
					KillerUploadingService.class);
			serviceIntent.putExtra(KillerConstants.EXTRA_KEY_FILEPATH, path);
			Log.i("REMOVE BEFORE COMMIT",String.format("Blah new serviceIntent()? %s", path));
			context.startService(serviceIntent);
		}
	}
}
