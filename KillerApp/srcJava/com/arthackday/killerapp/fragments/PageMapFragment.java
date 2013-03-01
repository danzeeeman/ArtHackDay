/***
  Copyright (c) 2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.
  
  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */

package com.arthackday.killerapp.fragments;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PageMapFragment extends SupportMapFragment {
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		GoogleMap map = getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		if (savedInstanceState == null) {
			CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(
					40.709559, -73.935490));
			CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

			map.moveCamera(center);
			map.animateCamera(zoom);
		}

		Log.i("REMOVE BEFORE COMMIT", "I JUST STARTED A PAGE MAP FRAGMENT");

		map.addMarker(new MarkerOptions()
				.position(new LatLng(40.709559, -73.935490))
				.title("@319Scholes").snippet("#ArtHackDay -God-Mode-"));

	}

	private void addMarker(GoogleMap map, double lat, double lon, int title,
			int snippet) {
		map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
				.title(getString(title)).snippet(getString(snippet)));
	}
}