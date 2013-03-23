/***
 * Copyright (c) 2012 CommonsWare, LLC Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * 
 * From _The Busy Coder's Guide to Android Development_
 * http://commonsware.com/Android
 */

package com.arthackday.killerapp.fragments;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PageMapFragment extends SupportMapFragment
{
  GoogleMap mMap;
  
  @Override
  public void onActivityCreated(Bundle savedInstanceState)
  {
    super.onActivityCreated(savedInstanceState);
    
    mMap = getMap();
    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    if (savedInstanceState == null)
    {
      CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(40.709559,
          -73.935490));
      CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

      mMap.moveCamera(center);
      mMap.animateCamera(zoom);
    }

    Log.i("REMOVE BEFORE COMMIT", "I JUST STARTED A PAGE MAP FRAGMENT");

    mMap.addMarker(new MarkerOptions()
        .position(new LatLng(40.709559, -73.935490)).title("@319Scholes")
        .snippet("#ArtHackDay -God-Mode-"));
    
    new GetLocations().execute("");
    
    Log.i("REMOVE BEFORE COMMIT", "I JUST STARTED CALLED AN ASYNCTASK");

  }

  private void addMarker(GoogleMap map, double lat, double lon, String string,
      String string2)
  {
    map.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
        .title(string).snippet(string2));
  }

  private class GetLocations extends AsyncTask<String, Void, String>
  {
    @Override
    protected String doInBackground(String... params)
    {

      HttpClient httpClient = new DefaultHttpClient();
      HttpContext localContext = new BasicHttpContext();
      HttpGet httpGet = new HttpGet("http://makeitdoathing.com:5100/locations");
      String text = "";
      try
      {
        HttpResponse response = httpClient.execute(httpGet, localContext);
        HttpEntity entity = response.getEntity();
        BufferedReader r = new BufferedReader(new InputStreamReader(
            entity.getContent()));
        String x = "";
        x = r.readLine();
        String total = "";
        while (x != null)
        {
          total += x;
          x = r.readLine();
        }
        text = total;
      } catch (Exception e)
      {
        Log.i("ArtHackDay","FAILURES");
      }
      return text;
    }

    @Override
    protected void onPostExecute(String result)
    {
      Log.i("REMOVE BEFORE COMMIT",result);
      try
      {
        JSONArray foo = new JSONArray(result);
        for(int i = 0; i < foo.length(); i++){
          JSONObject bar = (JSONObject) foo.get(i);
          double lat = bar.getDouble("lat");
          double lng = bar.getDouble("lat");
          addMarker(mMap, lat, lng, "drone", "");
        }
      } catch (JSONException e)
      {
        Log.i("REMOVE BEFORE COMMIT","ERROROROROR");
      }
     
    }

    @Override
    protected void onPreExecute()
    {
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
    }
  }
}