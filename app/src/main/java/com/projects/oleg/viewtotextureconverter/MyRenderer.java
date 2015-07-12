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

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by momo-chan on 7/1/15.
 */
public class MyRenderer implements CardboardView.StereoRenderer, StereoViewActivity.OnMagnetButtonPressedListener {
    public static final String CURSOR_TEXTURE = "cursor";
    private Context mContext;
    private Vibrator vibrator;

    private Camera camera = new Camera();
    private Camera eyeCamera = new Camera();
    private float[] headTrackTransform = new float[16];
    private float[] eyeTransform = new float[16];
    private volatile boolean stereoRendering = true;

    private Plane cursor = new Plane();

    private volatile View[] content;
    private volatile VirtualDisplayPlane[] contentPlanes;
    private float radius;

    private RayTraceResults rayResults = new RayTraceResults();


    private class RayTraceResults{
        VirtualDisplayPlane retPlane;
        float[] coordsWorldSpace = new float[4];
        float[] coordsPlaneSpace = new float[2];
    }


    public MyRenderer(Context context, View[] content){
        super();
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mContext = context;
        this.content = content;
        contentPlanes = new VirtualDisplayPlane[content.length];
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i] = new VirtualDisplayPlane();
        }
        positionTabs(9.5f);
    }

    public void positionTabs(float distance){
        float centerScale = 4.885f;
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i].displace(0,0,distance);
            contentPlanes[i].scale(9.0f/9.0f,9.0f/16.0f,1);
        }
        contentPlanes[1].scale(centerScale,centerScale,1);
        contentPlanes[0].displace(-contentPlanes[1].getWidth()/2,contentPlanes[1].getHeight()/4,-distance/9.0f);
        contentPlanes[2].displace(contentPlanes[1].getWidth() / 2, contentPlanes[1].getHeight() / 4, -distance / 9.0f);
    }


    @Override
    public void onMagnetButtonPressed() {
        vibrator.vibrate(200);
        synchronized (rayResults) {
            if (rayResults.retPlane != null) {
                rayResults.retPlane.dispatchTouchEvent(rayResults.coordsPlaneSpace[0], rayResults.coordsPlaneSpace[1]);
            }
        }
    }

    public void positionPlanes(float rad) {
        radius = rad;
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

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        cursor.setDraw(false);
        headTransform.getHeadView(headTrackTransform, 0);
        eyeCamera.copyFrom(camera);
        eyeCamera.applyTransform(headTrackTransform);
        synchronized (rayResults) {
            rayResults.retPlane = (VirtualDisplayPlane) cameraRayTrace(eyeCamera, rayResults.coordsPlaneSpace);
        }
        if(rayResults.retPlane!=null){
            cursor.setOrigin(rayResults.coordsWorldSpace);
            cursor.displace(cursor.getScale()[0], -cursor.getScale()[1], 0);
            cursor.setDraw(true);
            cursor.setParallel(rayResults.retPlane);
        }
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
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        cursor.draw(eyeCamera, null);
    }



    public Plane cameraRayTrace(Camera rayCamera, float[] intrs){
        for(int i = 0; i < contentPlanes.length; i++){
            float top = Utils.dotProduct(contentPlanes[i].getForward(),contentPlanes[i].getOrigin()) - Utils.dotProduct(rayCamera.getOrigin(),contentPlanes[i].getForward());
            float bottom = Utils.dotProduct(rayCamera.getForward(),contentPlanes[i].getForward());
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
            Matrix.invertM(inverseMatrix,0,contentPlanes[i].getTransform(),0);
            Matrix.multiplyMV(intersectionPlaneSpace, 0, inverseMatrix, 0, intersection, 0);
            float[] topLeftPlane = {-contentPlanes[i].getScale()[0], contentPlanes[i].getScale()[1]};
            intersectionPlaneSpace[0] = intersectionPlaneSpace[0] - topLeftPlane[0];
            intersectionPlaneSpace[1] = -intersectionPlaneSpace[1] + topLeftPlane[1];

            intersectionPlaneSpace[0]/= (contentPlanes[i].getScale()[0]*2);
            intersectionPlaneSpace[1]/= (contentPlanes[i].getScale()[1]*2);
            if(intersectionPlaneSpace[0] < 1 && intersectionPlaneSpace[0] > 0){
                if(intersectionPlaneSpace[1] < 1 && intersectionPlaneSpace[1] > 0) {
                    rayResults.coordsWorldSpace = intersection;
                    intrs[0] = intersectionPlaneSpace[0];
                    intrs[1] = intersectionPlaneSpace[1];
                    return contentPlanes[i];
                }
            }

        }
        return null;
    }

    private float convertRange(float number, float oMin, float oMax, float nMin, float nMax){
        /*
        OldRange = (OldMax - OldMin)
        NewRange = (NewMax - NewMin)
        NewValue = (((OldValue - OldMin) * NewRange) / OldRange) + NewMin
         */
        float oldRange = oMax - oMin;
        float newRange = nMax - nMin;
        float newValue = (((number - oMin)*newRange)/oldRange) + nMin;
        return newValue;

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
            contentPlanes[b].createDisplay(mContext,content[b],960,740);
        }
        cursor.setShader(ShaderManager.getManager().getShader(Bitmap3DShader.SHADER_KEY));
        cursor.setTexture(TextureManager.getManager().getTexture(CURSOR_TEXTURE));
        cursor.scale(.125f, .125f, 1);
        Utils.print("Cam origin: ");
        Utils.printVec(camera.getOrigin());
        Utils.print("Cam forward");
        Utils.printVec(camera.getForward());
     }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        TextureManager.createSingleton(mContext);
        ShaderManager.initalizeManager();
        TextureManager.getManager().createTextureFromReasource(R.drawable.cursor,CURSOR_TEXTURE);

    }

    @Override
    public void onRendererShutdown() {

    }

}
