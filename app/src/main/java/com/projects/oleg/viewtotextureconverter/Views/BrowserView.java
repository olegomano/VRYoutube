package com.projects.oleg.viewtotextureconverter.Views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ZoomButtonsController;

/**
 * Created by momo-chan on 7/13/15.
 */
public class BrowserView extends RelativeLayout implements VoiceEditText.SpeechStatusListener {
    private LinearLayout topBarParent;
    private Button back;
    private Button forward;

    private LinearLayout zoomCntrlParent;
    private Button zoomIn;
    private Button zoomOut;

    private VoiceEditText searchBar;
    private String youtubeSearch = "https://www.youtube.com/results?search_query=";


    private BrowserStatusListener listener;
    private ZoomListener zoomListener;

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

    public void setOnBrowserStatusListener(BrowserStatusListener ls){
        listener = ls;
    }

    public void setZoomListener(ZoomListener zls){
        zoomListener = zls;
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

        backParams.weight = .25f;
        backParams.width = 0;
        backParams.height = LayoutParams.MATCH_PARENT;

        forwardParams.weight = .15f;
        forwardParams.width = 0;
        forwardParams.height = LayoutParams.MATCH_PARENT;

        searchParams.weight = .65f;
        searchParams.width = 0;
        searchParams.height = LayoutParams.MATCH_PARENT;

        back.setLayoutParams(backParams);
        forward.setLayoutParams(forwardParams);
        searchBar.setLayoutParams(searchParams);



        topBarParent.addView(back);
        topBarParent.addView(searchBar);
        createZoomControlls(.10f);
        topBarParent.addView(zoomCntrlParent);

        searchBar.setBackgroundColor(Color.argb(255, 120, 120, 217));
        topBarParent.setBackgroundColor(Color.argb(255, 23, 23, 170));

        searchBar.setHint("Click here to voice search");
        searchBar.setHintTextColor(Color.argb(255,17,17,131));


        searchBar.requestFocus();
        searchBar.setHighlightColor(Color.argb(255,17,17,131));
        searchBar.setResultListener(this);


        back.setText("back");
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

    private void createZoomControlls(float weight){
        zoomCntrlParent = new LinearLayout(getContext());
        LinearLayout.LayoutParams zoomCntlrParentParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        zoomCntlrParentParams.weight = weight;
        zoomCntrlParent.setLayoutParams(zoomCntlrParentParams);
        zoomCntrlParent.setOrientation(LinearLayout.VERTICAL);
        zoomCntrlParent.setPadding(0,0,0,0);

        zoomIn = new Button(getContext());
        zoomOut = new Button(getContext());
        zoomIn.setPadding(0,0,0,0);
        zoomOut.setPadding(0,0,0,0);
        zoomIn.setText("+");
        zoomOut.setText("-");

        LinearLayout.LayoutParams inParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0);
        LinearLayout.LayoutParams outParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0);
        inParams.weight = .5f;
        inParams.setMargins(0,0,0,0);
        outParams.weight = .5f;
        outParams.setMargins(0,0,0,0);

        zoomIn.setLayoutParams(inParams);
        zoomOut.setLayoutParams(outParams);

        zoomCntrlParent.addView(zoomIn);
        zoomCntrlParent.addView(zoomOut);


        zoomIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zoomListener != null){
                    zoomListener.onZoomChanged(1);
                }
            }
        });

        zoomOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zoomListener != null){
                    zoomListener.onZoomChanged(-1);
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
        /*
        wbView.setWebChromeClient(new WebChromeClient() {
        });
        */
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
    public void onRecognitionStarted() {
        if(listener != null){
            listener.onRecognitionStarted();
        }
    }

    @Override
    public void onRecognitionEnded() {
        if(listener != null){
            listener.onRecognitoinEnded();
        }
    }

    @Override
    public void onWordStarted() {
        if(listener != null){
            listener.onWordStarted();
        }
    }

    @Override
    public void onWordEnded() {
        if(listener != null){
            listener.onWordEnded();
        }
    }

    @Override
    public void onSpeechResult(String result) {
        wbView.loadUrl(youtubeSearch+result);
    }

    public interface BrowserStatusListener {
        public void onRecognitionStarted();
        public void onRecognitoinEnded();
        public void onWordStarted();
        public void onWordEnded();
    }

    public interface ZoomListener{
        public void onZoomChanged(float dz);
    }
}