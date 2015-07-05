package com.projects.oleg.viewtotextureconverter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;


public class StereoViewActivity extends CardboardActivity {
    private CardboardView cardboardView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.setContentView(R.layout.activity_stereo_view);
        View[] content = new View[1];
        for(int i = 0; i < content.length; i++){
            content[i] = createWebView(this);
        }
        setConentViews(content);
    }

    @Override
    public void setContentView(int layoutID){

    }

    public void setConentViews(View[] content){
        cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(new MyRenderer(this,content));
        setCardboardView(cardboardView);

    }

    private WebView createWebView(Context context){
        WebView wbView = new WebView(context);
        wbView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        wbView.loadUrl("https://www.google.com/?gws_rd=ssl");
        return  wbView;
    }

}
