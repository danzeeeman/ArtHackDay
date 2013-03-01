package com.arthackday.killerapp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.arthackday.killerapp.KillerApp;
import com.arthackday.killerapp.R;
import com.arthackday.killerapp.util.Authorizer.AuthorizationListener;
import com.arthackday.killerapp.util.ClientLoginAuthorizer;
import com.arthackday.killerapp.util.KillerConstants;
import com.arthackday.killerapp.util.Util;

import com.arthackday.killerapp.util.Authorizer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class YouTubeService extends IntentService {

	private String DEV_KEY = "AI39si7Yl70U3VirXNdmsNHkvKJYWWW-qto51r_DBfo4YwNehfBor_8CBwQgI6VRtNHOWigXAKauiZVDLnfwWEZx-WVFHp4Hsg";

	private static final String LOG_TAG = KillerUploadingService.class
			.getSimpleName();

	private static final String INITIAL_UPLOAD_URL = "http://uploads.gdata.youtube.com/resumable/feeds/api/users/default/uploads";
	private static final String DEFAULT_VIDEO_CATEGORY = "News";
	private static final String DEFAULT_VIDEO_TAGS = "#ArtHackDay";

	private static final int DIALOG_LEGAL = 0;

	private static final int MAX_RETRIES = 5;
	private static final int BACKOFF = 4; // base of exponential backoff

	

	private String clientLoginToken = null;
	private String youTubeName = null;
	
	private Authorizer authorizer = null;
	
	
	private String path;
	private double currentFileSize = 0;
	private double totalBytesUploaded = 0;
	
	public static final String YT_ACCOUNT = "yt_account";
	private SharedPreferences preferences = null;
	private AccountManager accountManager = null;

	static class YouTubeAccountException extends Exception {
		public YouTubeAccountException(String msg) {
			super(msg);
		}
	}

	public YouTubeService() {
		super("foobar");

	}

	public YouTubeService(String name) {
		super(name);

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		path = intent.getExtras().getString(KillerConstants.EXTRA_KEY_FILEPATH);

		Log.i("REMOVE BEFORE COMMIT",
				String.format("Blah uploading file! %s", path));

		accountManager = AccountManager.get(getApplicationContext());

		this.authorizer = new ClientLoginAuthorizer.ClientLoginAuthorizerFactory()
				.getAuthorizer(this,
						ClientLoginAuthorizer.YOUTUBE_AUTH_TOKEN_TYPE);
		this.preferences = this.getSharedPreferences(
				KillerApp.SHARED_PREF_NAME, Activity.MODE_PRIVATE);
		this.youTubeName = preferences.getString(YT_ACCOUNT, null);
		if (youTubeName == null) {
			Account[] accts = accountManager.getAccountsByType("com.google");
			youTubeName = accts[0].name;

		}

		clientLoginToken = authorizer.getFreshAuthToken(youTubeName,
				clientLoginToken);
	}

	private void lulzWatchThis() {
		URL url;
		try {
			url = new URL(
					"https://gdata.youtube.com/feeds/api/users/default?v=2.1");
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestMethod("PUT");
			connection.setRequestProperty("Content-Type",
					"application/atom+xml");
			connection.setRequestProperty("Authorization",
					String.format("Bearer \"%s\"", clientLoginToken));
			connection.setRequestProperty("GData-Version", "2.1");
			connection.setRequestProperty("X-GData-Key",
					String.format("key=%s", DEV_KEY));

			String template = Util.readFile(this, R.raw.autolink).toString();
			String name = String.format("ArtHackDayDrone %d",
					System.currentTimeMillis());
			String atomData = String.format(template, youTubeName);

			OutputStreamWriter outStreamWriter = new OutputStreamWriter(
					connection.getOutputStream());
			outStreamWriter.write(atomData);
			outStreamWriter.close();
			int responseCode = connection.getResponseCode();
			if (responseCode < 200 || responseCode >= 300) {
				// The response code is 40X

				throw new IOException(String.format(
						"response code='%s' (code %d)" + " for %s %s",
						connection.getResponseMessage(), responseCode,
						connection.getURL(), connection.getRequestMethod()));

			} else if (responseCode == 200) {
				youTubeName = name;
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void upload(String path) {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String videoId = msg.getData().getString("videoId");

				if (!Util.isNullOrEmpty(videoId)) {
					currentFileSize = 0;
					totalBytesUploaded = 0;
				} else {
					String error = msg.getData().getString("error");
					if (!Util.isNullOrEmpty(error)) {
						currentFileSize = 0;
						totalBytesUploaded = 0;
					}
				}
			}
		};

	}

	public class UploadTask extends AsyncTask {

		private String ytdDomain = null;
		private String assignmentId = null;
		private Date dateTaken = null;
		private Location videoLocation = null;
		private int numberOfRetries = 0;
		private String tags = null;
		
		
		@Override
		protected String doInBackground(Object... params) {

			String uri = (String) params[0];
			String videoId = null;
			int submitCount = 0;
			try {
				while (submitCount <= MAX_RETRIES && videoId == null) {
					try {
						submitCount++;
						videoId = startUpload(uri);
						assert videoId != null;
					} catch (Internal500ResumeException e500) { // TODO -
																// this
																// should
																// not
																// really
																// happen
						if (submitCount < MAX_RETRIES) {
							Log.w(LOG_TAG, e500.getMessage());
							Log.d(LOG_TAG, String.format("Upload retry :%d.",
									submitCount));
						} else {
							Log.d(LOG_TAG, "Giving up");
							Log.e(LOG_TAG, e500.getMessage());
							throw new IOException(e500.getMessage());
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();

			} catch (YouTubeAccountException e) {
				e.printStackTrace();

			} catch (SAXException e) {
				e.printStackTrace();

			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			return videoId;
		}

		private String startUpload(String path) throws IOException,
				YouTubeAccountException, SAXException,
				ParserConfigurationException, Internal500ResumeException {
			File file = new File(path);

			if (clientLoginToken == null) {
				// The stored gmail account is not linked to YouTube
				throw new YouTubeAccountException(youTubeName
						+ " is not linked to a YouTube account.");
			}

			String uploadUrl = uploadMetaData(file.getAbsolutePath(), true);

			Log.d(LOG_TAG, "uploadUrl=" + uploadUrl);
			Log.d(LOG_TAG,
					String.format("Client token : %s ", clientLoginToken));

			currentFileSize = file.length();
			totalBytesUploaded = 0;
			numberOfRetries = 0;

			int uploadChunk = 1024 * 1024 * 3; // 3MB

			int start = 0;
			int end = -1;

			String videoId = null;
			double fileSize = currentFileSize;
			while (fileSize > 0) {
				if (fileSize - uploadChunk > 0) {
					end = start + uploadChunk - 1;
				} else {
					end = start + (int) fileSize - 1;
				}
				Log.d(LOG_TAG, String.format("start=%s end=%s total=%s", start,
						end, file.length()));
				try {
					videoId = gdataUpload(file, uploadUrl, start, end);
					fileSize -= uploadChunk;
					start = end + 1;
					this.numberOfRetries = 0; // clear this counter as we had a
												// succesfull upload
				} catch (IOException e) {
					Log.d(LOG_TAG, "Error during upload : " + e.getMessage());
					ResumeInfo resumeInfo = null;
					do {
						if (!shouldResume()) {
							Log.d(LOG_TAG, String.format(
									"Giving up uploading '%s'.", uploadUrl));
							throw e;
						}
						try {
							resumeInfo = resumeFileUpload(uploadUrl);
						} catch (IOException re) {
							// ignore
							Log.d(LOG_TAG,
									String.format(
											"Failed retry attempt of : %s due to: '%s'.",
											uploadUrl, re.getMessage()));
						}
					} while (resumeInfo == null);
					Log.d(LOG_TAG, String.format(
							"Resuming stalled upload to: %s.", uploadUrl));
					if (resumeInfo.videoId != null) { // upload actually
														// complted
														// despite the exception
						videoId = resumeInfo.videoId;
						Log.d(LOG_TAG, String.format(
								"No need to resume video ID '%s'.", videoId));
						break;
					} else {
						int nextByteToUpload = resumeInfo.nextByteToUpload;
						Log.d(LOG_TAG, String.format(
								"Next byte to upload is '%d'.",
								nextByteToUpload));
						totalBytesUploaded = nextByteToUpload; // possibly
																	// rolling
																	// back
																	// the
																	// previosuly
																	// saved
																	// value
						fileSize = currentFileSize - nextByteToUpload;
						start = nextByteToUpload;
					}
				}
			}

			if (videoId != null) {
				return videoId;
			}

			return null;
		}

		private String uploadMetaData(String filePath, boolean retry)
				throws IOException {
			String uploadUrl = INITIAL_UPLOAD_URL;
			HttpURLConnection urlConnection = getGDataUrlConnection(uploadUrl);
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoOutput(true);
			// urlConnection.setRequestProperty("Content-Length",
			// String.format("%d", new File(filePath).length()));
			urlConnection.setRequestProperty("Content-Type",
					"application/atom+xml");
			urlConnection.setRequestProperty("Slug", filePath);
			String atomData;

			String title = String.format("Art Hack Day -God-Mode-%s &s &s",
					Build.DEVICE, Build.FINGERPRINT, Build.HOST);
			String description = "This is Video Shot by A Drone of the piece Killer App Bro during ArtHackDay -God-Mode- this App was written by @theDANtheMAN";
			String category = DEFAULT_VIDEO_CATEGORY;
			tags = DEFAULT_VIDEO_TAGS;

			String template = Util.readFile(YouTubeService.this, R.raw.gdata).toString();
			atomData = String.format(template, title, description, category,
					this.tags);

			OutputStreamWriter outStreamWriter = new OutputStreamWriter(
					urlConnection.getOutputStream());
			outStreamWriter.write(atomData);
			outStreamWriter.close();

			int responseCode = urlConnection.getResponseCode();
			if (responseCode < 200 || responseCode >= 300) {
				// The response code is 40X
				if ((responseCode + "").startsWith("4") && retry) {
					Log.d(LOG_TAG, "retrying to fetch auth token for "
							+ youTubeName);
					clientLoginToken = authorizer.getFreshAuthToken(
							youTubeName, clientLoginToken);
					return uploadMetaData(filePath, false);
				} else {
					throw new IOException(String.format(
							"response code='%s' (code %d)" + " for %s %s",
							urlConnection.getResponseMessage(), responseCode,
							urlConnection.getURL(),
							urlConnection.getRequestMethod()));
				}
			}

			return urlConnection.getHeaderField("Location");
		}

		private String gdataUpload(File file, String uploadUrl, int start,
				int end) throws IOException {
			int chunk = end - start + 1;
			int bufferSize = 1024;
			byte[] buffer = new byte[bufferSize];
			FileInputStream fileStream = new FileInputStream(file);

			HttpURLConnection urlConnection = getGDataUrlConnection(uploadUrl);
			// some mobile proxies do not support PUT, using
			// X-HTTP-Method-Override
			// to get around this problem
			if (isFirstRequest()) {
				Log.d(LOG_TAG, String.format(
						"Uploaded %d bytes so far, using POST method.",
						(int) totalBytesUploaded));
				urlConnection.setRequestMethod("POST");
			} else {
				urlConnection.setRequestMethod("POST");
				urlConnection.setRequestProperty("X-HTTP-Method-Override",
						"PUT");
				Log.d(LOG_TAG,
						String.format(
								"Uploaded %d bytes so far, using POST with X-HTTP-Method-Override PUT method.",
								(int) totalBytesUploaded));
			}
			urlConnection.setDoOutput(true);
			urlConnection.setFixedLengthStreamingMode(chunk);
			urlConnection.setRequestProperty("Content-Type", "video/mp4");
			urlConnection.setRequestProperty("Content-Range",
					String.format("bytes %d-%d/%d", start, end, file.length()));
			Log.d(LOG_TAG, urlConnection.getRequestProperty("Content-Range"));

			OutputStream outStreamWriter = urlConnection.getOutputStream();

			fileStream.skip(start);

			int bytesRead;
			int totalRead = 0;
			while ((bytesRead = fileStream.read(buffer, 0, bufferSize)) != -1) {
				outStreamWriter.write(buffer, 0, bytesRead);
				totalRead += bytesRead;
				totalBytesUploaded += bytesRead;

				double percent = (totalBytesUploaded / currentFileSize) * 99;

				/*
				 * Log.d(LOG_TAG, String.format(
				 * "fileSize=%f totalBytesUploaded=%f percent=%f",
				 * currentFileSize, totalBytesUploaded, percent));
				 */

				if (totalRead == (end - start + 1)) {
					break;
				}
			}

			outStreamWriter.close();

			int responseCode = urlConnection.getResponseCode();

			Log.d(LOG_TAG, "responseCode=" + responseCode);
			Log.d(LOG_TAG,
					"responseMessage=" + urlConnection.getResponseMessage());

			try {
				if (responseCode == 201) {
					String videoId = parseVideoId(urlConnection
							.getInputStream());

					String latLng = null;
					if (this.videoLocation != null) {
						latLng = String.format("lat=%f lng=%f",
								this.videoLocation.getLatitude(),
								this.videoLocation.getLongitude());
					}

					submitToYtdDomain(this.ytdDomain, this.assignmentId,
							videoId, youTubeName, clientLoginToken,
							getTitleText(), getDescriptionText(),
							this.dateTaken, latLng, this.tags);
					return videoId;
				} else if (responseCode == 200) {
					Set<String> keySet = urlConnection.getHeaderFields()
							.keySet();
					String keys = urlConnection.getHeaderFields().keySet()
							.toString();
					Log.d(LOG_TAG, String.format("Headers keys %s.", keys));
					for (String key : keySet) {
						Log.d(LOG_TAG, String.format("Header key %s value %s.",
								key, urlConnection.getHeaderField(key)));
					}
					Log.w(LOG_TAG,
							"Received 200 response during resumable uploading");
					throw new IOException(
							String.format(
									"Unexpected response code : responseCode=%d responseMessage=%s",
									responseCode,
									urlConnection.getResponseMessage()));
				} else {
					if ((responseCode + "").startsWith("5")) {
						String error = String.format(
								"responseCode=%d responseMessage=%s",
								responseCode,
								urlConnection.getResponseMessage());
						Log.w(LOG_TAG, error);
						// TODO - this exception will trigger retry mechanism to
						// kick in
						// TODO - even though it should not, consider
						// introducing a
						// new type so
						// TODO - resume does not kick in upon 5xx
						throw new IOException(error);
					} else if (responseCode == 308) {
						// OK, the chunk completed succesfully
						Log.d(LOG_TAG, String.format(
								"responseCode=%d responseMessage=%s",
								responseCode,
								urlConnection.getResponseMessage()));
					} else {
						// TODO - this case is not handled properly yet
						Log.w(LOG_TAG,
								String.format(
										"Unexpected return code : %d %s while uploading :%s",
										responseCode,
										urlConnection.getResponseMessage(),
										uploadUrl));
					}
				}
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}

			return null;
		}

		private String parseVideoId(InputStream atomDataStream)
				throws ParserConfigurationException, SAXException, IOException {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(atomDataStream);

			NodeList nodes = doc.getElementsByTagNameNS("*", "*");
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				String nodeName = node.getNodeName();
				if (nodeName != null && nodeName.equals("yt:videoid")) {
					return node.getFirstChild().getNodeValue();
				}
			}
			return null;
		}

		private String getDescriptionText() {
			// TODO Auto-generated method stub
			return null;
		}

		private String getTitleText() {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean isFirstRequest() {
			return totalBytesUploaded == 0;
		}

		private ResumeInfo resumeFileUpload(String uploadUrl)
				throws IOException, ParserConfigurationException, SAXException,
				Internal500ResumeException {
			HttpURLConnection urlConnection = getGDataUrlConnection(uploadUrl);
			urlConnection.setRequestProperty("Content-Range", "bytes */*");
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("X-HTTP-Method-Override", "PUT");
			urlConnection.setFixedLengthStreamingMode(0);

			HttpURLConnection.setFollowRedirects(false);

			urlConnection.connect();
			int responseCode = urlConnection.getResponseCode();

			if (responseCode >= 300 && responseCode < 400) {
				int nextByteToUpload;
				String range = urlConnection.getHeaderField("Range");
				if (range == null) {
					Log.d(LOG_TAG, String.format(
							"PUT to %s did not return 'Range' header.",
							uploadUrl));
					nextByteToUpload = 0;
				} else {
					Log.d(LOG_TAG,
							String.format("Range header is '%s'.", range));
					String[] parts = range.split("-");
					if (parts.length > 1) {
						nextByteToUpload = Integer.parseInt(parts[1]) + 1;
					} else {
						nextByteToUpload = 0;
					}
				}
				return new ResumeInfo(nextByteToUpload);
			} else if (responseCode >= 200 && responseCode < 300) {
				return new ResumeInfo(
						parseVideoId(urlConnection.getInputStream()));
			} else if (responseCode == 500) {
				// TODO this is a workaround for current problems with resuming
				// uploads while switching transport (Wifi->EDGE)
				throw new Internal500ResumeException(String.format(
						"Unexpected response for PUT to %s: %s " + "(code %d)",
						uploadUrl, urlConnection.getResponseMessage(),
						responseCode));
			} else {
				throw new IOException(String.format(
						"Unexpected response for PUT to %s: %s " + "(code %d)",
						uploadUrl, urlConnection.getResponseMessage(),
						responseCode));
			}
		}

		private boolean shouldResume() {
			this.numberOfRetries++;
			if (this.numberOfRetries > MAX_RETRIES) {
				return false;
			}
			try {
				int sleepSeconds = (int) Math
						.pow(BACKOFF, this.numberOfRetries);
				Log.d(LOG_TAG,
						String.format("Zzzzz for : %d sec.", sleepSeconds));
				Thread.currentThread().sleep(sleepSeconds * 1000);
				Log.d(LOG_TAG,
						String.format("Zzzzz for : %d sec done.", sleepSeconds));
			} catch (InterruptedException se) {
				se.printStackTrace();
				return false;
			}
			return true;
		}

		private HttpURLConnection getGDataUrlConnection(String urlString)
				throws IOException {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestProperty("Authorization",
					String.format("Bearer \"%s\"", clientLoginToken));
			connection.setRequestProperty("GData-Version", "2");
			connection.setRequestProperty("X-GData-Key",
					String.format("key=%s", DEV_KEY));
			return connection;
		}

		public void submitToYtdDomain(String ytdDomain, String assignmentId,
				String videoId, String youTubeName, String clientLoginToken,
				String title, String description, Date dateTaken,
				String videoLocation, String tags) {

			JSONObject payload = new JSONObject();
			try {
				payload.put("method", "NEW_MOBILE_VIDEO_SUBMISSION");
				JSONObject params = new JSONObject();

				params.put("videoId", videoId);
				params.put("youTubeName", youTubeName);
				params.put("clientLoginToken", clientLoginToken);
				params.put("title", title);
				params.put("description", description);
				params.put("videoDate", dateTaken.toString());
				params.put("tags", tags);

				if (videoLocation != null) {
					params.put("videoLocation", videoLocation);
				}

				if (assignmentId != null) {
					params.put("assignmentId", assignmentId);
				}

				payload.put("params", params);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			String jsonRpcUrl = "http://" + ytdDomain + "/jsonrpc";
			String json = Util.makeJsonRpcCall(jsonRpcUrl, payload);

			if (json != null) {
				try {
					JSONObject jsonObj = new JSONObject(json);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

		class ResumeInfo {
			int nextByteToUpload;
			String videoId;

			ResumeInfo(int nextByteToUpload) {
				this.nextByteToUpload = nextByteToUpload;
			}

			ResumeInfo(String videoId) {
				this.videoId = videoId;
			}
		}

		/**
		 * Need this for now to trigger entire upload transaction retry
		 */
		class Internal500ResumeException extends Exception {
			Internal500ResumeException(String message) {
				super(message);
			}
		}

	}
}
