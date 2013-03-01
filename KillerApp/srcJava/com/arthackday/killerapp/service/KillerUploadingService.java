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
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.IntentService;
import android.content.Intent;

public class KillerUploadingService extends IntentService {

	public KillerUploadingService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			URI uri = new URI("");

			File file = new File("");
			DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPut httpPut = new HttpPut("http://mydomain.com/some/action");
			MultipartEntity entity = new MultipartEntity();
			entity.addPart("myFile", new FileBody(file));

			httpPut.setEntity(entity);
			try {
				HttpResponse response = httpclient.execute(httpPut);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
