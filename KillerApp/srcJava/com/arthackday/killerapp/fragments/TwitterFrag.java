package com.arthackday.killerapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


    

    return rootView;
  }
}
