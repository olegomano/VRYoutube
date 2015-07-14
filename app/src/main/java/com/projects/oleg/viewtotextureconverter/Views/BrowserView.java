package com.projects.oleg.viewtotextureconverter.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projects.oleg.viewtotextureconverter.R;
import com.projects.oleg.viewtotextureconverter.Utils;

/**
 * Created by momo-chan on 7/13/15.
 */
public class BrowserView extends RelativeLayout implements VoiceEditText.SpeechStatusListener {
    private LinearLayout topBarParent;
    private Button back;
    private Button forward;
    private VoiceEditText searchBar;
    private String youtubeSearch = "https://www.youtube.com/results?search_query=";

    private BrowserStatusListener listener;

    private LinearLayout wbViewParent;
    private WebView wbView;


    public BrowserView(Context context) {
        super(context);
        createHierarchy();
    }

    public BrowserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        createHierarchy();
    }

    public BrowserView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createHierarchy();
    }

    public BrowserView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        createHierarchy();
    }

    private void createHierarchy() {
        //this.setWeightSum(1);
        //this.setOrientation(VERTICAL);
        createTopBar(.14f);
        createWebView(.86f);
        addView(wbViewParent);
        addView(topBarParent);

    }

    private void createTopBar(float weight) {
        topBarParent = new LinearLayout(getContext());
        topBarParent.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams topBarParentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        topBarParentParams.height = dpToPix(44);
        topBarParentParams.weight = weight;
        topBarParent.setLayoutParams(topBarParentParams);

        back = new Button(getContext());
        forward = new Button(getContext());
        searchBar = new VoiceEditText(getContext());

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(0, 0);
        LinearLayout.LayoutParams forwardParams = new LinearLayout.LayoutParams(0, 0);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(0, 0);

        backParams.weight = .15f;
        backParams.width = 0;
        backParams.height = LayoutParams.MATCH_PARENT;

        forwardParams.weight = .15f;
        forwardParams.width = 0;
        forwardParams.height = LayoutParams.MATCH_PARENT;

        searchParams.weight = .7f;
        searchParams.width = 0;
        searchParams.height = LayoutParams.MATCH_PARENT;

        back.setLayoutParams(backParams);
        forward.setLayoutParams(forwardParams);
        searchBar.setLayoutParams(searchParams);

        topBarParent.addView(back);
        topBarParent.addView(forward);
        topBarParent.addView(searchBar);
        searchBar.setBackgroundColor(Color.WHITE);
        topBarParent.setBackgroundColor(Color.LTGRAY);
        searchBar.requestFocus();
        searchBar.setHighlightColor(Color.BLUE);
        searchBar.setResultListener(this);

        back.setText("B");
        forward.setText("F");

        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wbView.canGoBack()) {
                    wbView.goBack();
                }
            }
        });

        forward.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wbView.canGoForward()){
                    wbView.goForward();
                }
            }
        });
    }

    private void createWebView(float weight) {
        wbView = createWebView(getContext());
        wbViewParent = new LinearLayout(getContext());
        LinearLayout.LayoutParams wbVParParams =  new LinearLayout.LayoutParams(0, 0);
        wbVParParams.weight = weight;
        wbVParParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        wbVParParams.width = LayoutParams.MATCH_PARENT;
        wbViewParent.setLayoutParams(wbVParParams);
        wbViewParent.addView(wbView);
    }

    @Override
    public void scrollBy(int x, int y) {
        if (wbView != null) {
            wbView.scrollBy(x, y);
        }
    }

    private int dpToPix(int dp){
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }

    private WebView createWebView(Context context) {
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
        return wbView;
    }

    @Override
    public void onSpeechStarted() {
        if(listener != null){
            listener.onVoiceStarted();
        }
    }

    @Override
    public void onSpeechEnded() {
        if(listener != null){
            listener.onVoiceEnded();
        }
    }

    @Override
    public void onSpeechResult(String result) {
        wbView.loadUrl(youtubeSearch+result);
    }

    public interface BrowserStatusListener {
        public void onVoiceStarted();

        public void onVoiceEnded();
    }
}