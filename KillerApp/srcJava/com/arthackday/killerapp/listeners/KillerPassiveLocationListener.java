package com.arthackday.killerapp.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class KillerPassiveLocationListener implements LocationListener
{

	@Override
	public void onLocationChanged(Location location) {
		Log.d("I AM GOWD", "I know where you are...and what you are doing");
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
 
}
