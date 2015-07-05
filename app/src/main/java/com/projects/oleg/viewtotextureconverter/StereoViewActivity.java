package com.projects.oleg.viewtotextureconverter;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;


public class StereoViewActivity extends CardboardActivity {
    private CardboardView cardboardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.setContentView(R.layout.activity_stereo_view);
    }

    @Override
    public void setContentView(int layoutID){

    }

    public void setConentViews(View[] content){
        cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(new MyRenderer(this,content));
        setCardboardView(cardboardView);

    }

}
