package com.arthackday.killerapp;

import java.util.ArrayList;

import com.arthackday.killerapp.camera.CaptureVideo;
import com.arthackday.killerapp.util.DeveloperKey;
import com.arthackday.killerapp.util.SystemUiHider;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubePlayer.ErrorReason;
import com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStateChangeListener;
import com.google.android.youtube.player.YouTubePlayer.PlayerStyle;
import com.google.android.youtube.player.YouTubePlayer.PlaylistEventListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;

import android.annotation.TargetApi;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class HarlemShake extends YouTubeFailureRecoveryActivity {

	private static final String KEY_CURRENTLY_SELECTED_ID = "currentlySelectedId";
	private YouTubePlayerView youTubePlayerView;
	private YouTubePlayer player;
	private MyPlaylistEventListener playlistEventListener;
	private MyPlayerStateChangeListener playerStateChangeListener;
	private MyPlaybackEventListener playbackEventListener;
	private String currentlySelectedId;

	private float yZero;
	private float yMax;
	private float yMin;
	private boolean doTheHarlemShake;
	private TranslateAnimation ta;
	private TranslateAnimation at;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_killer_app_splash);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		youTubePlayerView = (YouTubePlayerView) findViewById(R.id.player);
		youTubePlayerView.initialize(DeveloperKey.DEVELOPER_KEY, this);

		yZero = youTubePlayerView.getY();
		yMax = metrics.heightPixels - youTubePlayerView.getHeight() - 150;
		yMin = 0;
		ta = new TranslateAnimation(youTubePlayerView.getX(),
				youTubePlayerView.getX(), 0, -150f);
		at = new TranslateAnimation(youTubePlayerView.getX(),
				youTubePlayerView.getX(), 0, 150f);
		ta.setDuration(50);
		at.setDuration(50);
		ta.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				if (doTheHarlemShake) {
					youTubePlayerView.startAnimation(at);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

		});

		at.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				if (doTheHarlemShake) {
					youTubePlayerView.startAnimation(ta);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationStart(Animation animation) {

			}

		});

		playlistEventListener = new MyPlaylistEventListener();
		playerStateChangeListener = new MyPlayerStateChangeListener();
		playbackEventListener = new MyPlaybackEventListener();

		setControlsEnabled(false);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		findViewById(R.id.dummy_button).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent i = new Intent();
						i.setClass(getApplicationContext(), CaptureVideo.class);
						startActivity(i);

					}
				});
		findViewById(R.id.dummy_button1).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!doTheHarlemShake) {
							doTheHarlemShake = true;
							youTubePlayerView.startAnimation(at);
						}else{
							doTheHarlemShake = false;
						}
					}
				});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

	}

	@Override
	public void onInitializationSuccess(YouTubePlayer.Provider provider,
			final YouTubePlayer player, boolean wasRestored) {
		this.player = player;
		player.setPlaylistEventListener(playlistEventListener);
		player.setPlayerStateChangeListener(playerStateChangeListener);
		player.setPlaybackEventListener(playbackEventListener);
		player.setPlayerStyle(PlayerStyle.MINIMAL);
		if (!wasRestored) {
			playVideoAtSelection();
		}
		setControlsEnabled(true);
	}

	@Override
	protected YouTubePlayer.Provider getYouTubePlayerProvider() {
		return youTubePlayerView;
	}

	private void playVideoAtSelection() {
		ListEntry selectedEntry = new ListEntry("HarlemShake",
				"PLr15H0y-sbvjLWr90VGi0Sw6y3MozqmPu", true);
		if (selectedEntry.id != currentlySelectedId && player != null) {
			currentlySelectedId = selectedEntry.id;
			if (selectedEntry.isPlaylist) {
				player.cuePlaylist(selectedEntry.id);
			} else {
				player.cueVideo(selectedEntry.id);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putString(KEY_CURRENTLY_SELECTED_ID, currentlySelectedId);
	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		currentlySelectedId = state.getString(KEY_CURRENTLY_SELECTED_ID);
	}

	private void setControlsEnabled(boolean enabled) {

	}

	private final class MyPlaylistEventListener implements
			PlaylistEventListener {
		@Override
		public void onNext() {
			doTheHarlemShake = false;
		}

		@Override
		public void onPrevious() {
			doTheHarlemShake = false;
		}

		@Override
		public void onPlaylistEnded() {

		}
	}

	private final class MyPlaybackEventListener implements
			PlaybackEventListener {
		String playbackState = "NOT_PLAYING";
		String bufferingState = "";

		@Override
		public void onPlaying() {
			playbackState = "PLAYING";

		}

		@Override
		public void onBuffering(boolean isBuffering) {
			bufferingState = isBuffering ? "(BUFFERING)" : "";
		}

		@Override
		public void onStopped() {
			playbackState = "STOPPED";
			doTheHarlemShake = false;

		}

		@Override
		public void onPaused() {
			playbackState = "PAUSED";
			doTheHarlemShake = false;

		}

		@Override
		public void onSeekTo(int endPositionMillis) {

		}
	}

	private final class MyPlayerStateChangeListener implements
			PlayerStateChangeListener {
		String playerState = "UNINITIALIZED";

		@Override
		public void onLoading() {
			playerState = "LOADING";

		}

		@Override
		public void onLoaded(String videoId) {
			playerState = String.format("LOADED %s", videoId);

		}

		@Override
		public void onAdStarted() {
			playerState = "AD_STARTED";
			player.pause();

		}

		@Override
		public void onVideoStarted() {
			playerState = "VIDEO_STARTED";
			doTheHarlemShake = false;

		}

		@Override
		public void onVideoEnded() {
			playerState = "VIDEO_ENDED";
			doTheHarlemShake = false;

		}

		@Override
		public void onError(ErrorReason reason) {
			playerState = "ERROR (" + reason + ")";
			if (reason == ErrorReason.UNEXPECTED_SERVICE_DISCONNECTION) {
				// When this error occurs the player is released and can no
				// longer be used.
				player = null;
				setControlsEnabled(false);
			}

		}

	}

	private static final class ListEntry {

		public final String title;
		public final String id;
		public final boolean isPlaylist;

		public ListEntry(String title, String videoId, boolean isPlaylist) {
			this.title = title;
			this.id = videoId;
			this.isPlaylist = isPlaylist;
		}

		@Override
		public String toString() {
			return title;
		}

	}
}