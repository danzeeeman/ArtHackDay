package com.arthackday.killerapp;

import java.io.File;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.FrameLayout;

import com.arthackday.killerapp.camera.CameraPreview;
import com.arthackday.killerapp.fragments.MenuFrag;
import com.arthackday.killerapp.fragments.PageMapFragment;
import com.arthackday.killerapp.fragments.ScreenSlidePageFragment;
import com.arthackday.killerapp.fragments.TwitterFrag;
import com.arthackday.killerapp.gps.PlatformSpecificImplementationFactory;
import com.arthackday.killerapp.sharedpreference.SharedPreferenceSaver;
import com.arthackday.killerapp.util.KillerConstants;
import com.google.android.gcm.GCMRegistrar;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class KillerHomeScreen extends FragmentActivity implements
    TextToSpeech.OnInitListener
{

  /**
   * The number of pages (wizard steps) to show in this demo.
   */
  private static final int        NUM_PAGES = 5;

  /**
   * The pager widget, which handles animation and allows swiping horizontally
   * to access previous and next wizard steps.
   */
  private ViewPager               mPager;
  private ViewPager               mMenu;

  /**
   * The pager adapter, which provides the pages to the view pager widget.
   */
  private PagerAdapter            mPagerAdapter;
  private PagerAdapter            mMenuAdapter;

  Camera                          camera;
  File                            file;

  MediaRecorder                   mediaRecorder;
  FrameLayout                     cameraPreviewFrame;

  boolean                         recording;

  CameraPreview                   cameraPreview;
  private boolean                 godmode;

  protected SharedPreferences     prefs;
  protected Editor                prefsEditor;
  protected SharedPreferenceSaver sharedPreferenceSaver;

  @Override
  protected void onResume()
  {
    super.onResume();

  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.killer_home_screen);

    GCMRegistrar.checkDevice(this);

    if (true)
    {
      GCMRegistrar.checkManifest(this);
    }

    final String regId = GCMRegistrar.getRegistrationId(this);

    if (regId.length() == 0)
    {
      GCMRegistrar.register(this, KillerApp.SENDER_ID);
    } else
    {
      Log.d(getClass().getSimpleName(), "Existing registration: " + regId);
    }

    // Get a reference to the Shared Preferences and a Shared Preference
    // Editor.
    prefs = getSharedPreferences(KillerConstants.SHARED_PREFERENCE_FILE,
        Context.MODE_PRIVATE);
    prefsEditor = prefs.edit();

    // Instantiate a SharedPreferenceSaver class based on the available
    // platform version.
    // This will be used to save shared preferences
    sharedPreferenceSaver = PlatformSpecificImplementationFactory
        .getSharedPreferenceSaver(this);

    // Save that we've been run once.
    prefsEditor.putBoolean(KillerConstants.SP_KEY_RUN_ONCE, true);
    sharedPreferenceSaver.savePreferences(prefsEditor, false);

    // cameraPreviewFrame = (FrameLayout) findViewById(R.id.fakepreview);

    // Instantiate a ViewPager and a PagerAdapter.
    mPager = (ViewPager) findViewById(R.id.pager);
    mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
    mPager.setAdapter(mPagerAdapter);
    mPager.setOffscreenPageLimit(5);
  }

  @Override
  public void onBackPressed()
  {
    if (mPager.getCurrentItem() == 0)
    {
      // If the user is currently looking at the first step, allow the
      // system to handle the
      // Back button. This calls finish() on this activity and pops the
      // back stack.
      super.onBackPressed();
    } else
    {
      // Otherwise, select the previous step.
      mPager.setCurrentItem(mPager.getCurrentItem() - 1);
    }
  }

  /**
   * A simple pager adapter that represents 5 ScreenSlidePageFragment objects,
   * in sequence.
   */
  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
  {
    public ScreenSlidePagerAdapter(FragmentManager fm)
    {
      super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
      switch (position)
      {
        case 0:
          return new MenuFrag();
        case 1:
          return new TwitterFrag();
        case 2:
          return new PageMapFragment();
        case 3:
          return new ScreenSlidePageFragment();
        case 4:
          return new MenuFrag();
      }
      return new MenuFrag();
    }

    @Override
    public int getCount()
    {
      return NUM_PAGES;
    }
  }

  @Override
  public void onInit(int status)
  {
    // TODO Auto-generated method stub

  }
}
