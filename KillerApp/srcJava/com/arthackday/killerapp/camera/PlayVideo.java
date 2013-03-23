package com.arthackday.killerapp.camera;

import java.io.File;

import com.arthackday.killerapp.R;
import com.arthackday.killerapp.receivers.NewVideoReceiver;
import com.arthackday.killerapp.util.KillerConstants;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

public class PlayVideo extends Activity implements OnPreparedListener,
    OnCompletionListener
{
  private static final String TAG = "VideoPlaybackActivity";

  private VideoView           videoView;

  private ImageButton         backButton;

  private ImageButton         playButton;

  private ImageButton         stopButton;

  private ImageButton         deleteButton;

  private Uri                 uri;

  @Override
  public void onCreate(Bundle bundle)
  {
    super.onCreate(bundle);
    super.setContentView(R.layout.video_playback);
    this.videoView = (VideoView) super.findViewById(R.id.video);
    this.uri = super.getIntent().getData();
    this.backButton = (ImageButton) super.findViewById(R.id.backButton);
    this.playButton = (ImageButton) super.findViewById(R.id.playButton);
  }

  private void toggleButtons(boolean playing)
  {
    this.backButton.setEnabled(!playing);
    this.playButton.setVisibility(playing ? View.GONE : View.VISIBLE);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    this.videoView.setVideoURI(this.uri);
    this.videoView.setOnPreparedListener(this);
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    this.videoView.stopPlayback();
    this.playButton.setVisibility(View.GONE);
  }

  public void onPrepared(MediaPlayer mp)
  {
    Log.d(TAG, "Prepared. Subscribing for completion callback.");
    this.videoView.setOnCompletionListener(this);
    Log.d(TAG, "Starting plackback");
    this.toggleButtons(true);
    this.videoView.start();
    Toast.makeText(this, R.string.playing, Toast.LENGTH_SHORT).show();

  }

  public void onCompletion(MediaPlayer mp)
  {
    Log.d(TAG, "Completed playback. Go to beginning.");
    this.toggleButtons(false);
    this.videoView.seekTo(0);
    this.notifyUser(R.string.completed_playback);
    Intent intent = new Intent();
    intent.setAction("com.arthackday.killerapp.newvideo");
    intent.putExtra(KillerConstants.EXTRA_KEY_FILEPATH, uri.getPath());
    sendBroadcast(intent);
  }

  // gets called by the button press
  public void back(View v)
  {
    Log.d(TAG, "Going back");
    super.finish();
  }

  // gets called by the button press
  public void play(View v)
  {
    Log.d(TAG, "Playing");
    this.videoView.start();
    this.toggleButtons(true);
  }

  public void stop(View v)
  {
    Log.d(TAG, "Stopping");
    this.videoView.pause();
    this.videoView.seekTo(0);
    this.toggleButtons(false);
  }

  // gets called by the button press
  public void delete(View v)
  {
    if (new File(this.uri.getPath()).delete())
    {
      Log.d(TAG, "Deleted: " + this.uri);
      this.notifyUser(R.string.deleted);
    } else
    {
      Log.d(TAG, "Failed to delete: " + this.uri);
      this.notifyUser(R.string.cannot_delete);
    }
    Log.d(TAG, "Going back");
    super.finish();
  }

  private void notifyUser(int messageResource)
  {
    Toast.makeText(this, messageResource, Toast.LENGTH_SHORT).show();
  }
}
