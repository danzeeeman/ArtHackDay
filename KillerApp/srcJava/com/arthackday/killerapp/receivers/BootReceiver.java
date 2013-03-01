package com.arthackday.killerapp.receivers;

import com.arthackday.killerapp.camera.CaptureVideo;
import com.arthackday.killerapp.gps.LocationUpdateRequester;
import com.arthackday.killerapp.gps.PlatformSpecificImplementationFactory;
import com.arthackday.killerapp.util.KillerConstants;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.sax.StartElementListener;
import android.util.Log;


public class BootReceiver extends BroadcastReceiver {
	  @Override
	  public void onReceive(Context context, Intent intent) {
	    SharedPreferences prefs = context.getSharedPreferences(KillerConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
	  	boolean runOnce = prefs.getBoolean(KillerConstants.SP_KEY_RUN_ONCE, false);
	  	
	  	if (runOnce) {
	  	  LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	  		
	        // Instantiate a Location Update Requester class based on the available platform version.
	        // This will be used to request location updates.
	  	  LocationUpdateRequester locationUpdateRequester = PlatformSpecificImplementationFactory.getLocationUpdateRequester(locationManager); 
	   
	      // Check the Shared Preferences to see if we are updating location changes.
	      boolean followLocationChanges = prefs.getBoolean(KillerConstants.SP_KEY_FOLLOW_LOCATION_CHANGES, true);
	      
	      if (followLocationChanges) { 	  
	        // Passive location updates from 3rd party apps when the Activity isn't visible.
	        Intent passiveIntent = new Intent(context, PassiveLocationChangedReceiver.class);
	        PendingIntent locationListenerPassivePendingIntent = PendingIntent.getActivity(context, 0, passiveIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	        locationUpdateRequester.requestPassiveLocationUpdates(KillerConstants.PASSIVE_MAX_TIME, KillerConstants.PASSIVE_MAX_DISTANCE, locationListenerPassivePendingIntent);
	      }
	  	}
	  	
	  	Log.i("REMOVE BEFORE COMMIT","blahhh onBoot Receiver");
	  	
	  	Intent i = new Intent();
	  	i.setClassName("com.arthackday.killerapp", "com.arthackday.killerapp.GodMode");
	  	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	  	context.startActivity(i);
	  }
	}