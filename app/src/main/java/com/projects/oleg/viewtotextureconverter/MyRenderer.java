package com.projects.oleg.viewtotextureconverter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Vibrator;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.projects.oleg.viewtotextureconverter.Geometry.Plane;
import com.projects.oleg.viewtotextureconverter.Geometry.VirtualDisplayPlane;
import com.projects.oleg.viewtotextureconverter.Rendering.Camera;
import com.projects.oleg.viewtotextureconverter.Shader.Bitmap3DShader;
import com.projects.oleg.viewtotextureconverter.Shader.OES3DShader;
import com.projects.oleg.viewtotextureconverter.Shader.ShaderManager;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;
import com.projects.oleg.viewtotextureconverter.Views.BrowserView;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by momo-chan on 7/1/15.
 */
public class MyRenderer implements CardboardView.StereoRenderer, StereoViewActivity.OnMagnetButtonPressedListener, BrowserView.BrowserStatusListener, BrowserView.ZoomListener {
    public static final String CURSOR_TEXTURE = "cursor";
    public static final String MIC_TEXTURE = "mic";
    public static final String MIC_TALK_TEXTURE = "mic_talk";
    public static final String SCROLL_UP_TEXTURE = "scroll_up_texture";
    public static final String SCROLL_DOWN_TEXTURE = "scroll_down_texture";
    public static final String SCROLL_DOWN_HIGHLIGHT_TEXTURE = "scroll_down_highlight_texture";
    public static final String SCROLL_UP_HIGHLIGHT_TEXTURE = "scroll_up_highlight_texture";

    private Context mContext;
    private Vibrator vibrator;

    private Camera camera = new Camera();
    private Camera eyeCamera = new Camera();
    private float[] headTrackTransform = new float[16];
    private volatile boolean stereoRendering = true;

    private Plane cursor = new Plane();
    private Plane voiceButton = new Plane();
    private Plane scrollUp = new Plane();
    private Plane scrollDown = new Plane();

    private Plane[] regPlanes = {voiceButton,scrollUp,scrollDown};

    private volatile View[] content;
    private volatile VirtualDisplayPlane[] contentPlanes;

    private VirtualDisplayPlane centerPlane;
    private VirtualDisplayPlane[] tabs;

    private RayTraceResults contentPlaneResults = new RayTraceResults();
    private RayTraceResults regularPlaneResults = new RayTraceResults();

    private float distance = 16;

    public MyRenderer(Context context, View[] content){
        super();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mContext = context;
        this.content =  content;
        contentPlanes = new VirtualDisplayPlane[content.length];
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i] = new VirtualDisplayPlane();
        }
        centerPlane = contentPlanes[0];
        scrollDown.setRayTraceStatusListener(new Plane.OnRayTraceStatusListener() {
            @Override
            public void onOver(RayTraceResults results) {
                if(centerPlane.getContent() != null) {
                    centerPlane.getContent().scrollBy(0, 10);
                    scrollDown.setTexture(TextureManager.getManager().getTexture(SCROLL_DOWN_HIGHLIGHT_TEXTURE));
                }
            }

            @Override
            public void onClick(RayTraceResults results) {

            }
        });


        scrollUp.setRayTraceStatusListener(new Plane.OnRayTraceStatusListener() {
            @Override
            public void onOver(RayTraceResults results) {
                if(centerPlane.getContent() != null) {
                    centerPlane.getContent().scrollBy(0, -10);
                    scrollUp.setTexture(TextureManager.getManager().getTexture(SCROLL_UP_HIGHLIGHT_TEXTURE));
                }
            }

            @Override
            public void onClick(RayTraceResults results) {

            }
        });
    }

    @Override
    public void onZoomChanged(float dz) {
        if(voiceButton.getOrigin()[3] <= camera.getPerspective()[0] + 4f){
            return;
        }
        distance+=(.35f*-dz);
        positionTabs(distance);
    }


    public void positionTabs(float distance){
        float[] origin = {0,0,0,1};

        centerPlane.setOrigin(origin);
        centerPlane.setScale(1,1,1);

        voiceButton.setOrigin(origin);
        voiceButton.setScale(1,1,1);

        scrollUp.setOrigin(origin);
        scrollUp.setScale(1,1,1);

        scrollDown.setOrigin(origin);
        scrollDown.setScale(1,1,1);

        voiceButton.setOrigin(origin);
        voiceButton.setScale(1,1,1);

        float centerScale = 5.785f;
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i].displace(0, 0, distance);
         //   contentPlanes[i].scale(9.0f/9.0f,9.0f/16.0f,1);
        }
        centerPlane.scale(centerScale,centerScale,1);
        voiceButton.scale(.45f,.45f,1);
        voiceButton.displace(0, 0, distance / 5.0f);
        scrollUp.displace(0, centerPlane.getHeight() / 1.9f, distance * .85f);
        scrollDown.displace(0, -centerPlane.getHeight() / 1.9f, distance * .85f);
        scrollUp.scale(.6f,.6f,1);
        scrollDown.scale(.6f,.6f,1);
        float[] lookAt = {0,0,-3.5f,0};
        scrollDown.lookAt(lookAt, camera.getDown());
        scrollUp.lookAt(lookAt, camera.getDown());

        voiceButton.setDraw(false);
    }


    @Override
    public void onMagnetButtonPressed() {
        vibrator.vibrate(200);
        synchronized (contentPlaneResults) {
            if (contentPlaneResults.retPlane != null) {
                ( ( VirtualDisplayPlane ) (contentPlaneResults.retPlane) ).dispatchTouchEvent(contentPlaneResults.coordsPlaneSpace[0], contentPlaneResults.coordsPlaneSpace[1]);
                contentPlaneResults.retPlane.onClick(contentPlaneResults);
            }
        }
        synchronized (regularPlaneResults){
            if(regularPlaneResults.retPlane != null){
                regularPlaneResults.retPlane.onClick(regularPlaneResults);
            }
        }
    }


    @Override
    public void onNewFrame(HeadTransform headTransform) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resetFrame();
        headTransform.getHeadView(headTrackTransform, 0);
        eyeCamera.copyFrom(camera);
        eyeCamera.applyTransform(headTrackTransform);
        synchronized (contentPlaneResults) {
             if(cameraRayTrace(eyeCamera, contentPlanes, contentPlaneResults)){
                 cursor.setOrigin(contentPlaneResults.coordsWorldSpace);
                 cursor.displace(cursor.getScale()[0], -cursor.getScale()[1], 0);
                 cursor.setDraw(true);
                 cursor.setParallel(contentPlaneResults.retPlane);
                 contentPlaneResults.retPlane.onOver(contentPlaneResults);
             }
        }
        synchronized (regularPlaneResults) {
            if (cameraRayTrace(eyeCamera, regPlanes, regularPlaneResults)) {
                regularPlaneResults.retPlane.onOver(regularPlaneResults);
            }
        }

    }

    private void resetFrame(){
        cursor.setDraw(false);
        scrollDown.setTexture(TextureManager.getManager().getTexture(SCROLL_DOWN_TEXTURE));
        scrollUp.setTexture(TextureManager.getManager().getTexture(SCROLL_UP_TEXTURE));
    }

    @Override
    public void onDrawEye(Eye eye) {
        eye.setProjectionChanged();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        eyeCamera.copyFrom(camera);
        eyeCamera.setRatio((float) eye.getViewport().height / (float) eye.getViewport().width);
        if(stereoRendering) {
            eyeCamera.applyTransform(eye.getEyeView());
        }else{
            eyeCamera.applyTransform(headTrackTransform);
        }
        /*
        for(int i = 0; i < contentPlanes.length; i++) {
            float[] bounds = contentPlanes[i].getBounds();
            boolean skip = false;
            for (int b = 0; b < 4 && !skip; b++) {
                float dotProduct = Utils.dotProduct(bounds, b * 4, eyeCamera.getForward(), 0) / Utils.getMagnitude(bounds, b * 4);
                if (dotProduct <= .00005f) {
                    skip = true;
                }
            }
            if (!skip) {
                contentPlanes[i].draw(eyeCamera, null);
            }
        }
        */
        for(int i = 0; i < contentPlanes.length; i++){
            if(!contentPlanes[i].shouldCull(eyeCamera)){
                contentPlanes[i].draw(eyeCamera,null);
            }
        }

        synchronized (voiceButton) {
            voiceButton.draw(eyeCamera, null);
        }
        if(!scrollDown.shouldCull(eyeCamera)) {
            scrollDown.draw(eyeCamera, null);
        }
        if(!scrollUp.shouldCull(eyeCamera)){
           scrollUp.draw(eyeCamera,null);
        }
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        cursor.draw(eyeCamera, null);
    }


    @Override
    public void onRecognitionStarted() {
        synchronized (voiceButton){
            voiceButton.setDraw(true);
        }
    }

    @Override
    public void onRecognitoinEnded() {
        synchronized (voiceButton){
            voiceButton.setDraw(false);
        }
    }

    @Override
    public void onWordStarted() {
        synchronized (voiceButton){
            voiceButton.setTexture(TextureManager.getManager().getTexture(MIC_TALK_TEXTURE));
        }
    }

    @Override
    public void onWordEnded() {
        synchronized (voiceButton){
            voiceButton.setTexture(TextureManager.getManager().getTexture(MIC_TEXTURE));
        }
    }


    public boolean cameraRayTrace(Camera rayCamera, Plane[] planeSet, RayTraceResults results){
        for(int i = 0; i < planeSet.length; i++){
            float top = Utils.dotProduct(planeSet[i].getForward(),planeSet[i].getOrigin()) - Utils.dotProduct(rayCamera.getOrigin(),planeSet[i].getForward());
            float bottom = Utils.dotProduct(rayCamera.getForward(),planeSet[i].getForward());
            if(bottom == 0){ // vectors are parallel
                continue;
            }
            float scale = top/bottom;
            if(scale < 0){
                continue;
            }
            float[] intersection = {rayCamera.getForward()[0]*scale,rayCamera.getForward()[1]*scale,rayCamera.getForward()[2]*scale,1};
            float[] intersectionPlaneSpace = new float[4];
            float[] inverseMatrix = new float[16];
            Matrix.invertM(inverseMatrix,0,planeSet[i].getTransform(),0);
            Matrix.multiplyMV(intersectionPlaneSpace, 0, inverseMatrix, 0, intersection, 0);
            float[] topLeftPlane = {-planeSet[i].getScale()[0], planeSet[i].getScale()[1]};
            intersectionPlaneSpace[0] = intersectionPlaneSpace[0] - topLeftPlane[0];
            intersectionPlaneSpace[1] = -intersectionPlaneSpace[1] + topLeftPlane[1];

            intersectionPlaneSpace[0]/= (planeSet[i].getScale()[0]*2);
            intersectionPlaneSpace[1]/= (planeSet[i].getScale()[1]*2);
            if(intersectionPlaneSpace[0] < 1 && intersectionPlaneSpace[0] > 0){
                if(intersectionPlaneSpace[1] < 1 && intersectionPlaneSpace[1] > 0) {
                    results.coordsWorldSpace = intersection;
                    results.retPlane = planeSet[i];
                    results.coordsWorldSpace = intersection;
                    results.coordsPlaneSpace = intersectionPlaneSpace;
                    return true;
                }
            }

        }
        results.retPlane = null;
        return false;
    }



    public void setStereo(boolean status){
        stereoRendering = status;
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        for(int b = 0; b < contentPlanes.length; b++){
            contentPlanes[b].setShader(ShaderManager.getManager().getShader(OES3DShader.SHADER_KEY));
            contentPlanes[b].createDisplay(mContext,content[b],i1,i );
            float ratio = (float) i1 / ( (float) i);
            contentPlanes[b].scale(ratio,1,1);
        }
        cursor.setShader(ShaderManager.getManager().getShader(Bitmap3DShader.SHADER_KEY));
        cursor.setTexture(TextureManager.getManager().getTexture(CURSOR_TEXTURE));
        voiceButton.setShader(ShaderManager.getManager().getShader(Bitmap3DShader.SHADER_KEY));
        voiceButton.setTexture(TextureManager.getManager().getTexture(MIC_TEXTURE));
        scrollDown.setShader(ShaderManager.getManager().getShader(Bitmap3DShader.SHADER_KEY));
        scrollUp.setShader(ShaderManager.getManager().getShader(Bitmap3DShader.SHADER_KEY));
        scrollDown.setTexture(TextureManager.getManager().getTexture(SCROLL_DOWN_TEXTURE));
        scrollUp.setTexture(TextureManager.getManager().getTexture(SCROLL_UP_TEXTURE));
        cursor.scale(.225f, .225f, 1);
        positionTabs(distance);
        Utils.print("Cam origin: ");
        Utils.printVec(camera.getOrigin());
        Utils.print("Cam forward");
        Utils.printVec(camera.getForward());
     }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        TextureManager.createSingleton(mContext);
        ShaderManager.initalizeManager();
        TextureManager.getManager().createTextureFromReasource(R.drawable.cursor, CURSOR_TEXTURE);
        TextureManager.getManager().createTextureFromReasource(R.drawable.mic,MIC_TEXTURE);
        TextureManager.getManager().createTextureFromReasource(R.drawable.scrolldown,SCROLL_DOWN_TEXTURE);
        TextureManager.getManager().createTextureFromReasource(R.drawable.scrollup,SCROLL_UP_TEXTURE);
        TextureManager.getManager().createTextureFromReasource(R.drawable.scrolldownhighlight,SCROLL_DOWN_HIGHLIGHT_TEXTURE);
        TextureManager.getManager().createTextureFromReasource(R.drawable.scrolluphighlight,SCROLL_UP_HIGHLIGHT_TEXTURE);
        TextureManager.getManager().createTextureFromReasource(R.drawable.mictalk, MIC_TALK_TEXTURE);
    }

    @Override
    public void onRendererShutdown() {

    }


    public class RayTraceResults{
        Plane retPlane;
        float[] coordsWorldSpace = new float[4];
        float[] coordsPlaneSpace = new float[2];
    }

    /////////////////////////

    private float convertRange(float number, float oMin, float oMax, float nMin, float nMax){
        float oldRange = oMax - oMin;
        float newRange = nMax - nMin;
        float newValue = (((number - oMin)*newRange)/oldRange) + nMin;
        return newValue;

    }

    public void positionPlanes(float rad) {
        float mag = -.35f;
        float angle;
        if(contentPlanes.length == 1){
            float dx = (float) (rad*Math.cos(Math.toRadians(90)));
            float dz = (float) (rad*Math.sin(Math.toRadians(90)));
            contentPlanes[0].displace(dx, 0, dz);
            contentPlanes[0].lookAt(camera.getOrigin(), camera.getDown());
            contentPlanes[0].scale(3.7f, 3.7f, 1);
            return;
        }
        float sum = 90;
        angle = sum / (contentPlanes.length - 1);
        float sAngle = (180 - sum) / 2.0f;
        float scale = 5.825f;
        float startX = scale*(contentPlanes.length - 1)/2.0f;
        for (int i = 0; i < contentPlanes.length; i++) {
            float dxL =  -startX + scale*i;
            float dzL =  rad;
            float dx = (float) (rad*Math.cos(Math.toRadians(angle*i + sAngle)));
            float dz = (float) (rad*Math.sin(Math.toRadians(angle * i + sAngle)));
            float mx = dx + (dxL - dx)*mag;
            float mz = dz + (dzL - dz)*mag;
            contentPlanes[i].displace(mx, 0, mz);
            float[] lookAtPoint = {0,0,-3f,1};
            for(int b = 0; b < lookAtPoint.length; b++){
                lookAtPoint[b] += camera.getOrigin()[b];
            }
            contentPlanes[i].lookAt(lookAtPoint,camera.getDown());
            Utils.print("Plane Origin " + i);
            Utils.printVec(contentPlanes[i].getOrigin());
            contentPlanes[i].scale(16.0f, 9.0f, 1);
            contentPlanes[i].scale( (1.0f ) /scale, (1.0f ) / scale, 1);
            if(i == contentPlanes.length/2){
                contentPlanes[i].scale(1.81f,1.81f,1);
            }
            Utils.print("Plane bounds are");
            Utils.printMat(contentPlanes[i].getBounds());
        }
    }


}
