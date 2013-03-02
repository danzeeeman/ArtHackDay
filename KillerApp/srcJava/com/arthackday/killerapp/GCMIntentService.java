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

package com.arthackday.killerapp;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.arthackday.killerapp.KillerApp;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

public class GCMIntentService extends GCMBaseIntentService
{

  public static String GCM_URL = "http://www.makeitdoathing.com:5100/gcm?appname=%s&uuid=%s";

  public GCMIntentService()
  {
    super(KillerApp.SENDER_ID);
  }

  @Override
  protected void onRegistered(Context ctxt, String regId)
  {
    Log.i(getClass().getSimpleName(), "onRegistered: " + regId);

    URL url;
    try{
      url = new URL(String.format(GCM_URL, this.getPackageName(), regId));

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
      Log.wtf("ArtHackDay","failure");
    } catch (ClientProtocolException e)
    {
      Log.wtf("ArtHackDay","failure");
    } catch (IOException e)
    {
      Log.wtf("ArtHackDay","failure");
    }
  }

  @Override
  protected void onUnregistered(Context ctxt, String regId)
  {
    Log.i(getClass().getSimpleName(), "onUnregistered: " + regId);
  }

  @Override
  protected void onMessage(Context ctxt, Intent message)
  {
    Bundle extras = message.getExtras();

    /*
     * TO DO! CHANGE WHAT THE MESSAGES DO!!
     */

    for (String key : extras.keySet())
    {
      Log.i(getClass().getSimpleName(),
          String.format("onMessage: %s=%s", key, extras.getString(key)));
    }
  }

  @Override
  protected void onError(Context ctxt, String errorMsg)
  {
    Log.i(getClass().getSimpleName(), "onError: " + errorMsg);
  }

  @Override
  protected boolean onRecoverableError(Context ctxt, String errorMsg)
  {
    Log.i(getClass().getSimpleName(), "onRecoverableError: " + errorMsg);

    return (true);
  }
}
