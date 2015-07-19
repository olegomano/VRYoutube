package com.projects.oleg.viewtotextureconverter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.sensors.MagnetSensor;
import com.projects.oleg.viewtotextureconverter.Views.BrowserView;


public class StereoViewActivity extends CardboardActivity {
    public static int BROWSER_VIEW = 0;
    public static int VIDEO_VIEW = 1;
    public static int SCROLL_VIEW = 2;

    private CardboardView cardboardView;
    private MagnetSensor buttonDetector;
    private OnMagnetButtonPressedListener listener;
    private MyRenderer renderer;
    private BrowserView browser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonDetector = new MagnetSensor(this);
        buttonDetector.setOnCardboardTriggerListener(new MagnetSensor.OnCardboardTriggerListener() {
            @Override
            public void onCardboardTrigger() {
                if (listener != null) {
                    listener.onMagnetButtonPressed(cardboardView);
                }
            }
        });
        buttonDetector.start();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.setContentView(R.layout.activity_stereo_view);
        this.browser = new BrowserView(this);
        View[] content = {this.browser};
        setContentViews(content);
    }

    public void setOnMagnetButtonListener(OnMagnetButtonPressedListener lstnr){
        listener =  lstnr;
    }

    @Override
    public void setContentView(int layoutID){

    }

    public void setContentViews(View[] content){
        cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setNeckModelEnabled(true);
        cardboardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(listener != null){
                        listener.onMagnetButtonPressed(cardboardView);
                    }
                }
                return true;
            }
        });
        renderer = new MyRenderer(this, content);
        setOnMagnetButtonListener(renderer);
        cardboardView.setRenderer(renderer);
        browser.setOnBrowserStatusListener(renderer);
        browser.setZoomListener(renderer);
        setCardboardView(cardboardView);
     }


    public void onPause(){
        super.onPause();
        if(buttonDetector != null){
            if(browser != null){
                browser.clearView();
            }
            buttonDetector.stop();
        }
    }

    public void onResume(){
        super.onResume();
        if(buttonDetector != null){
            buttonDetector.start();
        }
    }

    public interface OnMagnetButtonPressedListener{
        public void onMagnetButtonPressed(CardboardView view);
    }



}
