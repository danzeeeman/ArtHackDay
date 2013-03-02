package com.arthackday.killerapp.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

import com.arthackday.killerapp.util.KillerConstants;
import com.google.android.gcm.GCMRegistrar;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class KillerUploadingService extends IntentService
{

  public static String VIDEO_URL_REAR  = "http://www.makeitdoathing.com:5100/video?uuid=%s";
  public static String VIDEO_URL_FRONT = "http://www.makeitdoathing.com:5100/video?uuid=%s";

  
  public KillerUploadingService(){
    super("fooBar");
  }
  
  public KillerUploadingService(String name)
  {
    super(name);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void onHandleIntent(Intent intent)
  {
    String path = intent.getStringExtra(KillerConstants.EXTRA_KEY_FILEPATH);
    URI uri;
    try
    {
      uri = new URI(String.format(VIDEO_URL_REAR, GCMRegistrar.getRegistrationId(this)));

      HttpClient httpclient = new DefaultHttpClient();
      HttpPost post = new HttpPost(uri);

      File file = new File(path);

      MultipartEntity entity = new MultipartEntity();
      ContentBody body = new FileBody(file, "video/mp4");
      entity.addPart("data", body);

      post.setEntity(entity);
      HttpResponse response = httpclient.execute(post);
      Log.i("REMOVE BEFORE COMMIT", String.format("I RETURNED WITH THIS!!!!! %d", response.getStatusLine().getStatusCode()));
      
    } catch (URISyntaxException e)
    {
      Log.i("REMOVE BEFORE COMMIT", "I FAILES");
    }
    // HttpEntity resEntity = response.getEntity();
    catch (ClientProtocolException e)
    {
      Log.i("REMOVE BEFORE COMMIT", "I FAILES");
    } catch (IOException e)
    {
      Log.i("REMOVE BEFORE COMMIT", "I FAILES");
    }
  }
}
