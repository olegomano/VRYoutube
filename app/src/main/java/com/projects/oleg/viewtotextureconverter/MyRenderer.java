package com.projects.oleg.viewtotextureconverter;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
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
public class MyRenderer implements CardboardView.StereoRenderer {
    public static final String CURSOR_TEXTURE = "cursor";

    private Context mContext;

    private Camera camera = new Camera();
    private Camera eyeCamera = new Camera();
    private float[] headTrackTransform = new float[16];
    private float[] eyeTransform = new float[16];
    private volatile boolean stereoRendering = false;

    private Plane cursor = new Plane();

    private volatile View[] content;
    private volatile VirtualDisplayPlane[] contentPlanes;
    private float radius;

    private float[] rayTraceCoords = new float[2];
    private VirtualDisplayPlane rayTracedPlane;

    public MyRenderer(Context context, View[] content){
        super();
        mContext = context;
        this.content = content;
        contentPlanes = new VirtualDisplayPlane[content.length];
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i] = new VirtualDisplayPlane();
        }
        positionPlanes(5.45f);
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
               contentPlanes[i].scale(1.85f,1.85f,1);
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
        rayTracedPlane = (VirtualDisplayPlane) cameraRayTrace(eyeCamera, rayTraceCoords);
    }

    @Override
    public void onDrawEye(Eye eye) {
        camera.setRatio((float) eye.getViewport().height / (float) eye.getViewport().width);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glClearColor(1, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc (GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        eyeCamera.copyFrom(camera);

        if(stereoRendering) {
            Matrix.transposeM(eyeTransform, 0, eye.getEyeView(), 0);
            eyeCamera.applyTransform(eye.getEyeView());
        }else{
            eyeCamera.applyTransform(headTrackTransform);
        }

        for(int i = 0; i < contentPlanes.length; i++) {
            float[] bounds = contentPlanes[i].getBounds();
            boolean skip = false;
            for (int b = 0; b < 4 && !skip; b++) {
                float dotProduct = Utils.dotProduct(bounds, b * 4, eyeCamera.getForward(), 0) / Utils.getMagnitude(bounds, b * 4);
                if (dotProduct <= .05f) {
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
                    intrs[0] = intersectionPlaneSpace[0];
                    intrs[1] = intersectionPlaneSpace[1];
                    Utils.print("Looking at plane at pos: " + intrs[0] + ", " + intrs[1]);
                    cursor.setOrigin(intersection);
                    cursor.lookAt(rayCamera.getOrigin(), rayCamera.getDown());
                    cursor.setDraw(true);
                    cursor.setParallel(contentPlanes[i]);
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
        Utils.print("Plane Origin: ");
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
