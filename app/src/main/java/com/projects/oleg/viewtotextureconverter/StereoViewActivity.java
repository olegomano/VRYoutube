package com.projects.oleg.viewtotextureconverter;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;


public class StereoViewActivity extends CardboardActivity {
    private CardboardView cardboardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_stereo_view);
        cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(new MyRenderer());
        setCardboardView(cardboardView);
    }

    @Override
    public void setContentView(int layoutID){

    }

}
