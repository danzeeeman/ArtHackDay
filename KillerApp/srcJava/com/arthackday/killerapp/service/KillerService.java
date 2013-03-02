package com.arthackday.killerapp.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONStringer;

import com.arthackday.killerapp.provider.LocationContentProvider;
import com.arthackday.killerapp.receivers.ConnectivityChangedReceiver;
import com.arthackday.killerapp.receivers.LocationChangedReceiver;
import com.arthackday.killerapp.receivers.PassiveLocationChangedReceiver;
import com.arthackday.killerapp.util.KillerConstants;
import com.google.android.gcm.GCMRegistrar;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class KillerService extends IntentService
{
  protected static String       TAG           = "PlacesUpdateService";

  protected ContentResolver     contentResolver;
  protected SharedPreferences   prefs;
  protected Editor              prefsEditor;
  protected ConnectivityManager cm;
  protected boolean             lowBattery    = false;
  protected boolean             mobileData    = false;
  protected int                 prefetchCount = 0;

  public static String          LOCATION_URL  = "http://www.makeitdoathing.com:5100/location?appname=%s&uuid=%s&lat=%s&lng=%s&distance=%s&currentTime=%s";

  public KillerService()
  {
    super(TAG);
    setIntentRedeliveryMode(false);
  }

  /**
   * Set the Intent Redelivery mode to true to ensure the Service starts
   * "Sticky" Defaults to "true" on legacy devices.
   */
  protected void setIntentRedeliveryMode(boolean enable)
  {
  }

  /**
   * Returns battery status. True if less than 10% remaining.
   * 
   * @param battery
   *          Battery Intent
   * @return Battery is low
   */
  protected boolean getIsLowBattery(Intent battery)
  {
    float pctLevel = (float) battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 1)
        / battery.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
    return pctLevel < 0.15;
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    contentResolver = getContentResolver();
    prefs = getSharedPreferences(KillerConstants.SHARED_PREFERENCE_FILE,
        Context.MODE_PRIVATE);
    prefsEditor = prefs.edit();
  }

  /**
   * {@inheritDoc} Checks the battery and connectivity state before removing
   * stale venues and initiating a server poll for new venues around the
   * specified location within the given radius.
   */
  @Override
  protected void onHandleIntent(Intent intent)
  {
    // Check if we're running in the foreground, if not, check if
    // we have permission to do background updates.
    boolean backgroundAllowed = cm.getBackgroundDataSetting();
    boolean inBackground = prefs.getBoolean(
        KillerConstants.EXTRA_KEY_IN_BACKGROUND, true);

    if (!backgroundAllowed && inBackground)
      return;

    // Extract the location and radius around which to conduct our search.
    Location location = new Location(
        KillerConstants.CONSTRUCTED_LOCATION_PROVIDER);
    int radius = KillerConstants.DEFAULT_RADIUS;

    Bundle extras = intent.getExtras();
    if (intent.hasExtra(KillerConstants.EXTRA_KEY_LOCATION))
    {
      location = (Location) (extras.get(KillerConstants.EXTRA_KEY_LOCATION));
      radius = extras.getInt(KillerConstants.EXTRA_KEY_RADIUS,
          KillerConstants.DEFAULT_RADIUS);
    }

    // Check if we're in a low battery situation.
    IntentFilter batIntentFilter = new IntentFilter(
        Intent.ACTION_BATTERY_CHANGED);
    Intent battery = registerReceiver(null, batIntentFilter);
    lowBattery = getIsLowBattery(battery);

    // Check if we're connected to a data network, and if so - if it's a
    // mobile network.
    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null
        && activeNetwork.isConnectedOrConnecting();
    mobileData = activeNetwork != null
        && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

    // If we're not connected, enable the connectivity receiver and disable
    // the location receiver.
    // There's no point trying to poll the server for updates if we're not
    // connected, and the
    // connectivity receiver will turn the location-based updates back on
    // once we have a connection.
    if (!isConnected)
    {
      PackageManager pm = getPackageManager();

      ComponentName connectivityReceiver = new ComponentName(this,
          ConnectivityChangedReceiver.class);
      ComponentName locationReceiver = new ComponentName(this,
          LocationChangedReceiver.class);
      ComponentName passiveLocationReceiver = new ComponentName(this,
          PassiveLocationChangedReceiver.class);

      pm.setComponentEnabledSetting(connectivityReceiver,
          PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
          PackageManager.DONT_KILL_APP);

      pm.setComponentEnabledSetting(locationReceiver,
          PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
          PackageManager.DONT_KILL_APP);

      pm.setComponentEnabledSetting(passiveLocationReceiver,
          PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
          PackageManager.DONT_KILL_APP);
    } else
    {
      // If we are connected check to see if this is a forced update
      // (typically triggered
      // when the location has changed).

      // If it's not a forced update (for example from the Activity being
      // restarted) then
      // check to see if we've moved far enough, or there's been a long
      // enough delay since
      // the last update and if so, enforce a new update.

      // Retrieve the last update time and place.
      long lastTime = prefs.getLong(
          KillerConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);
      long lastLat = prefs.getLong(KillerConstants.SP_KEY_LAST_LIST_UPDATE_LAT,
          Long.MIN_VALUE);
      long lastLng = prefs.getLong(KillerConstants.SP_KEY_LAST_LIST_UPDATE_LNG,
          Long.MIN_VALUE);
      Location lastLocation = new Location(
          KillerConstants.CONSTRUCTED_LOCATION_PROVIDER);
      lastLocation.setLatitude(lastLat);
      lastLocation.setLongitude(lastLng);

      // If update time and distance bounds have been passed, do an
      // update.
      if ((lastTime < System.currentTimeMillis() - KillerConstants.MAX_TIME)
          || (lastLocation.distanceTo(location) > KillerConstants.MAX_DISTANCE))
      {
        Log.i("I AM GOD",
            "why hello there did you just enjoy your trip underground?");
      }

      long currentTime = System.currentTimeMillis();

      String id = String.format("%d", currentTime);
      String name = Build.FINGERPRINT;

      addPlace(location, id, name, currentTime);

      // Save the last update time and place to the Shared Preferences.
      prefsEditor.putLong(KillerConstants.SP_KEY_LAST_LIST_UPDATE_LAT,
          (long) location.getLatitude());
      prefsEditor.putLong(KillerConstants.SP_KEY_LAST_LIST_UPDATE_LNG,
          (long) location.getLongitude());
      prefsEditor.putLong(KillerConstants.SP_KEY_LAST_LIST_UPDATE_TIME,
          System.currentTimeMillis());
      prefsEditor.commit();

    }
  }

  /**
   * Adds the new place to the {@link LocationContentProvider} using the values
   * passed in. TODO Update this method to accept and persist the place
   * information your service provides.
   * 
   * @param currentLocation
   *          Current location
   * @param id
   *          Unique identifier
   * @param name
   *          Name
   * @param vicinity
   *          Vicinity
   * @param types
   *          Types
   * @param location
   *          Location
   * @param viewport
   *          Viewport
   * @param icon
   *          Icon
   * @param reference
   *          Reference
   * @param currentTime
   *          Current time
   * @return Successfully added
   */
  protected boolean addPlace(Location currentLocation, String id, String name,
      long currentTime)
  {
    // Contruct the Content Values
    Location location = new Location(
        KillerConstants.CONSTRUCTED_LOCATION_PROVIDER);
    location.setLatitude(40.709559);
    location.setLongitude(-73.935490);

    ContentValues values = new ContentValues();
    values.put(LocationContentProvider.KEY_ID, id);
    values.put(LocationContentProvider.KEY_NAME, name);
    double lat = currentLocation.getLatitude();
    double lng = currentLocation.getLongitude();
    values.put(LocationContentProvider.KEY_LOCATION_LAT, lat);
    values.put(LocationContentProvider.KEY_LOCATION_LNG, lng);
    values.put(LocationContentProvider.KEY_LAST_UPDATE_TIME, currentTime);

    // Calculate the distance between the current location and the venue's
    // location
    float distance = 0;
    if (currentLocation != null && currentLocation != null)
      distance = currentLocation.distanceTo(location);
    values.put(LocationContentProvider.KEY_DISTANCE, distance);

    // Update or add the new place to the LocationContentProvider
    String where = LocationContentProvider.KEY_ID + " = '" + id + "'";
    boolean result = false;
    try
    {
      if (contentResolver.update(LocationContentProvider.CONTENT_URI, values,
          where, null) == 0)
      {
        if (contentResolver.insert(LocationContentProvider.CONTENT_URI, values) != null)
          result = true;
      } else
        result = true;
    } catch (Exception ex)
    {
      Log.e("PLACES", "Adding " + name + " failed.");
    }

    URL url;
    try
    {
      url = new URL(String.format(LOCATION_URL, getPackageName(),
          GCMRegistrar.getRegistrationId(this), String.valueOf(lat),
          String.valueOf(lng), distance,
          String.valueOf(System.currentTimeMillis())));

      // --This code works for updating a record from the feed--
      HttpPut httpPut = new HttpPut(url.toString());

      // Send request to WCF service
      DefaultHttpClient httpClient = new DefaultHttpClient();

      HttpResponse response = httpClient.execute(httpPut);
      HttpEntity entity1 = response.getEntity();

      if (entity1 != null
          && (response.getStatusLine().getStatusCode() == 201 || response
              .getStatusLine().getStatusCode() == 200))
      {
        // --just so that you can view the response, this is optional--
        int sc = response.getStatusLine().getStatusCode();
        String sl = response.getStatusLine().getReasonPhrase();
      } else
      {
        int sc = response.getStatusLine().getStatusCode();
        String sl = response.getStatusLine().getReasonPhrase();
      }

    } catch (MalformedURLException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ClientProtocolException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return result;
  }

}