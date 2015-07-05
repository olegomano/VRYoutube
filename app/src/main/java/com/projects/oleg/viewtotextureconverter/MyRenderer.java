package com.projects.oleg.viewtotextureconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.display.VirtualDisplay;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.projects.oleg.viewtotextureconverter.Geometry.Plane;
import com.projects.oleg.viewtotextureconverter.Geometry.VirtualDisplayPlane;
import com.projects.oleg.viewtotextureconverter.Rendering.Camera;
import com.projects.oleg.viewtotextureconverter.Shader.Bitmap3DShader;
import com.projects.oleg.viewtotextureconverter.Shader.BitmapSpriteShader;
import com.projects.oleg.viewtotextureconverter.Shader.OES3DShader;
import com.projects.oleg.viewtotextureconverter.Shader.Shader;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by momo-chan on 7/1/15.
 */
public class MyRenderer implements CardboardView.StereoRenderer {
    private Context mContext;

    private Camera camera = new Camera();
    private Camera eyeCamera = new Camera();

    private Bitmap3DShader shader = new Bitmap3DShader();
    private OES3DShader oesShader= new OES3DShader();

    private Plane tstPlane = new Plane();
    //private VirtualDisplayPlane farPlane = new VirtualDisplayPlane();
    private volatile View[] content;
    private volatile VirtualDisplayPlane[] contentPlanes;

    public MyRenderer(Context context, View[] content){
        super();
        mContext = context;
        this.content = content;
        contentPlanes = new VirtualDisplayPlane[content.length];
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i] = new VirtualDisplayPlane();
        }
    }

    public void positionPlanes(){
        
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }
    private float dx = 0;
    private float increment = .03f;
    @Override
    public void onDrawEye(Eye eye) {
        camera.setRatio((float)eye.getViewport().height / (float)eye.getViewport().width);
        GLES20.glClearColor(1, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        dx+=increment;
        if(dx < -1 || dx > 1){
            increment*=-1;
        }
        float[] rotMat = new float[16];
        Matrix.setRotateM(rotMat, 0, .5f, 0, 1, 0);
      //  tstPlane.applyTransform(rotMat);
      //  camera.applyTransform(rotMat);
        float[] axis = {1,0,0};
        tstPlane.rotateAboutPoint(axis,1,tstPlane.getOrigin());
        eyeCamera.copyFrom(camera);
        eyeCamera.applyTransform(eye.getEyeView());
        tstPlane.draw(eyeCamera, null);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        TextureManager.createSingleton(mContext);
        shader.initShader();
        oesShader.initShader();
        tstPlane.setShader(shader);
        tstPlane.setTexture(TextureManager.getManager().createTextureFromReasource(R.drawable.errorloadingpng, "loading"));
        tstPlane.scale(1.0f / 9.0f, 1.0f / 16.0f, 1);
        tstPlane.scale(5,5,1);
        tstPlane.displace(0, 0, 1);

        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i].setShader(oesShader);
            contentPlanes[i].createDisplay(mContext,content[i],720,720);
        }


        Utils.print("Cam origin: ");
        Utils.printVec(camera.getOrigin());
        Utils.print("Cam forward");
        Utils.printVec(camera.getForward());
        Utils.print("Plane Origin: ");
        Utils.printVec(tstPlane.getOrigin());


    }

    @Override
    public void onRendererShutdown() {

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
