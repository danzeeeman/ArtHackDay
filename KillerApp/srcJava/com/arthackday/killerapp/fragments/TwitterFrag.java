package com.arthackday.killerapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebView.FindListener;
import android.widget.ListView;

import com.arthackday.killerapp.R;

public class TwitterFrag extends Fragment
{
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState)
  {
    ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.twitter_frag,
        container, false);

    WebView view = (WebView) rootView.findViewById(R.id.webview);
    
    view.getSettings().setJavaScriptEnabled(true);
    view.getSettings().setDomStorageEnabled(true);
    
    view.loadUrl("https://mobile.twitter.com/search?q=%23ArtHackDay");

    return rootView;
  }
}
