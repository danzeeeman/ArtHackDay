/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arthackday.killerapp.gps;

import com.arthackday.killerapp.sharedpreference.FroyoSharedPreferenceSaver;
import com.arthackday.killerapp.sharedpreference.GingerbreadSharedPreferenceSaver;
import com.arthackday.killerapp.sharedpreference.SharedPreferenceSaver;
import com.arthackday.killerapp.util.KillerConstants;

import android.content.Context;
import android.location.LocationManager;



/**
 * Factory class to create the correct instances
 * of a variety of classes with platform specific
 * implementations.
 * 
 */
public class PlatformSpecificImplementationFactory {    
	
  /**
   * Create a new LastLocationFinder instance
   * @param context Context
   * @return LastLocationFinder
   */
  public static ILastLocationFinder getLastLocationFinder(Context context) {
    return KillerConstants.SUPPORTS_GINGERBREAD ? new GingerbreadLastLocationFinder(context) : new LegacyLastLocationFinder(context);
  }
  
  /**
   * Create a new StrictMode instance.
   * @return StrictMode
   */
  public static IStrictMode getStrictMode() {
	if (KillerConstants.SUPPORTS_HONEYCOMB)
      return new HoneycombStrictMode();
	else if (KillerConstants.SUPPORTS_GINGERBREAD)
      return new LegacyStrictMode(); 
	else
      return null;
  }
  
  /**
   * Create a new LocationUpdateRequester
   * @param locationManager Location Manager
   * @return LocationUpdateRequester
   */
  public static LocationUpdateRequester getLocationUpdateRequester(LocationManager locationManager) {
    return KillerConstants.SUPPORTS_GINGERBREAD ? new GingerbreadLocationUpdateRequester(locationManager) : new FroyoLocationUpdateRequester(locationManager);    
  }
  
  /**
   * Create a new SharedPreferenceSaver
   * @param context Context
   * @return SharedPreferenceSaver
   */
  public static SharedPreferenceSaver getSharedPreferenceSaver(Context context) {
    return  KillerConstants.SUPPORTS_GINGERBREAD ? 
       new GingerbreadSharedPreferenceSaver(context) : 
       KillerConstants.SUPPORTS_FROYO ? 
           new FroyoSharedPreferenceSaver(context) :
           new LegacySharedPreferenceSaver(context);
  }
}
