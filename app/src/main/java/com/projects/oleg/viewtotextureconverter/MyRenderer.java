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

    private OES3DShader oesShader= new OES3DShader();

    private Plane tstPlane = new Plane();
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
        positionPlanes(5.0f);
    }

    public void positionPlanes(float rad) {
        float mag = .5f;
        float angle;
        if(contentPlanes.length == 1){
            float dx = (float) (rad*Math.cos(Math.toRadians(90)));
            float dz = (float) (rad*Math.sin(Math.toRadians(90)));
            contentPlanes[0].displace(dx, 0, dz);
            contentPlanes[0].lookAt(camera.getOrigin(), camera.getDown());
            contentPlanes[0].scale(3.7f, 3.7f, 1);
            return;
        }
        angle = 180 / (contentPlanes.length - 1);
        float distance =8f;
        float startX = distance*(contentPlanes.length - 1)/2.0f;
        for (int i = 0; i < contentPlanes.length; i++) {
            float dxL =  -startX + distance*i;
            float dzL =rad;
            float dx = (float) (rad*Math.cos(Math.toRadians(angle*i)));
            float dz = (float) (rad*Math.sin(Math.toRadians(angle * i)));
            float mx = dx + (dxL - dx)*mag;
            float mz = dz + (dzL - dz)*mag;

            contentPlanes[i].displace(dxL, 0, dzL);
            //contentPlanes[i].lookAt(camera.getOrigin(), camera.getDown());
            Utils.print("Plane Origin " + i);
            Utils.printVec(contentPlanes[i].getOrigin());
            contentPlanes[i].scale(16.0f,9.0f, 1);
            contentPlanes[i].scale(1.0f/3.7f, 1.0f / 3.7f,1);
        }
    }
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawEye(Eye eye) {
        camera.setRatio((float) eye.getViewport().height / (float) eye.getViewport().width);
        GLES20.glClearColor(1, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        eyeCamera.copyFrom(camera);
        eyeCamera.applyTransform(eye.getEyeView());
        //eyeCamera.setFov(eye.getFov().getLeft() + eye.getFov().getRight());
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i].draw(eyeCamera,null);
        }
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        for(int b = 0; b < contentPlanes.length; b++){
            contentPlanes[b].setShader(oesShader);
            contentPlanes[b].createDisplay(mContext,content[b],960,740);
        }
        Utils.print("Cam origin: ");
        Utils.printVec(camera.getOrigin());
        Utils.print("Cam forward");
        Utils.printVec(camera.getForward());
        Utils.print("Plane Origin: ");
        Utils.printVec(tstPlane.getOrigin());
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        TextureManager.createSingleton(mContext);
        oesShader.initShader();

    }

    @Override
    public void onRendererShutdown() {

    }
}
