package com.projects.oleg.viewtotextureconverter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buttonDetector = new MagnetSensor(this);
        buttonDetector.setOnCardboardTriggerListener(new MagnetSensor.OnCardboardTriggerListener() {
            @Override
            public void onCardboardTrigger() {
                if(listener != null){
                    listener.onMagnetButtonPressed();
                }
            }
        });
        buttonDetector.start();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.setContentView(R.layout.activity_stereo_view);
        View[] content = new View[3];
        for(int i = 0; i < content.length; i++){
            content[i] = new BrowserView(this);
        }
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
        renderer = new MyRenderer(this, content);
        setOnMagnetButtonListener(renderer);
        cardboardView.setRenderer(renderer);
        setCardboardView(cardboardView);
     }

    private WebView createWebView(Context context){
        WebView wbView = new WebView(context);
        wbView.setWebChromeClient(new WebChromeClient() {
        });
        wbView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        WebSettings ws = wbView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
        ws.setUseWideViewPort(true);
        //ws.setLoadWithOverviewMode(true);
        ws.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        wbView.loadUrl("https://www.youtube.com");
        wbView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Utils.print("View focus has changed " + hasFocus);
            }
        });

        wbView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                Utils.print("System Ui visibility has changed " + visibility);
            }
        });
        return  wbView;
    }

    public void onPause(){
        super.onPause();
        if(buttonDetector != null){
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
        public void onMagnetButtonPressed();
    }



}
