package com.projects.oleg.viewtotextureconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.display.VirtualDisplay;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
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
import com.projects.oleg.viewtotextureconverter.Shader.ShaderManager;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by momo-chan on 7/1/15.
 */
public class MyRenderer implements CardboardView.StereoRenderer {
    private Context mContext;

    private Camera camera = new Camera();
    private Camera eyeCamera = new Camera();
    private float[] headTrackTransform = new float[16];
    private volatile boolean stereoRendering = false;

    private Plane tstPlane = new Plane();
    private volatile View[] content;
    private volatile VirtualDisplayPlane[] contentPlanes;
    private float radius;

    public MyRenderer(Context context, View[] content){
        super();
        mContext = context;
        this.content = content;
        contentPlanes = new VirtualDisplayPlane[content.length];
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i] = new VirtualDisplayPlane();
        }
        positionPlanes(3.45f);
    }

    public void positionPlanes(float rad) {
        radius = rad;
        float mag = -.27f;
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
        float scale = 7.825f;
        float startX = scale*(contentPlanes.length - 1)/2.0f;
        for (int i = 0; i < contentPlanes.length; i++) {
            float dxL =  -startX + scale*i;
            float dzL =  rad;
            float dx = (float) (rad*Math.cos(Math.toRadians(angle*i + sAngle)));
            float dz = (float) (rad*Math.sin(Math.toRadians(angle * i + sAngle)));
            float mx = dx + (dxL - dx)*mag;
            float mz = dz + (dzL - dz)*mag;
            contentPlanes[i].displace(mx, 0, mz);
            //contentPlanes[i].rotateAboutPoint(axis,rotate - rotate*i,contentPlanes[i].getOrigin());
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
               contentPlanes[i].scale(2,2,1);
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
        headTransform.getHeadView(headTrackTransform, 0);
    }

    @Override
    public void onDrawEye(Eye eye) {
        camera.setRatio((float) eye.getViewport().height / (float) eye.getViewport().width);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glClearColor(1, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        eyeCamera.copyFrom(camera);
        //eyeCamera.applyTransform(headTrackTransform);


        if(stereoRendering) {
            float[] eyeMatrix = new float[16];
            Matrix.transposeM(eyeMatrix, 0, eye.getEyeView(), 0);
            eyeCamera.applyTransform(eyeMatrix);
        }
        eyeCamera.applyTransform(headTrackTransform);
        VirtualDisplayPlane hitPlane = (VirtualDisplayPlane) cameraRayTrace(eyeCamera);
        for(int i = 0; i < contentPlanes.length; i++){
            contentPlanes[i].setContentTxt();
        }
        if(hitPlane != null){
            hitPlane.setLoadTxt();
        }
        for(int i = 0; i < contentPlanes.length; i++) {
            float[] bounds = contentPlanes[i].getBounds();
            float[] vecW = new float[4];
            float[] vecH = new float[4];
            for(int c = 0; c < 3; c++){
                vecH[c] = bounds[0 + c] - bounds[4 + c];
                vecW[c] = bounds[4 + c] - bounds[8 + c];
            }
            float width = (Utils.getMagnitude(vecH));
            float height = (Utils.getMagnitude(vecW));
            boolean skip = false;
            for (int b = 0; b < 4; b++) {
                float dotProduct = Utils.dotProduct(bounds, b * 4, eyeCamera.getForward(), 0) / Utils.getMagnitude(bounds, b * 4);
                if (dotProduct <= .05f) {
                    skip = true;
                }
            }
            if (!skip) {
                contentPlanes[i].draw(eyeCamera, null);
            }
        }
    }

    public Plane cameraRayTrace(Camera camera){
        for(int i = 0; i < contentPlanes.length; i++){
            float top = Utils.dotProduct(contentPlanes[i].getForward(),contentPlanes[i].getOrigin()) - Utils.dotProduct(camera.getOrigin(),contentPlanes[i].getForward());
            float bottom = Utils.dotProduct(camera.getForward(),contentPlanes[i].getForward());
            if(bottom == 0){ // vectors are parallel
                continue;
            }
            float scale = top/bottom;
            float[] intersection = {camera.getForward()[0]*scale,camera.getForward()[1]*scale,camera.getForward()[2]*scale,0};
            float[] bounds = contentPlanes[i].getBounds();
            /*
            for (int c = 0; c < 3; c++){
                intersection[c] -= bounds[4 + c];
                if(intersection[c] < 0){
                    continue;
                }
            }
            */
            float[] vecW = new float[4];
            float[] vecH = new float[4];
            for(int c = 0; c < 3; c++){
                vecH[c] = bounds[0 + c] - bounds[4 + c];
                vecW[c] = bounds[4 + c] - bounds[8 + c];
            }
            float width = (Utils.getMagnitude(vecH));
            float height = (Utils.getMagnitude(vecW));

            float[] intersectionPlaneSpace = new float[4];
            float[] inverseMatrix = new float[16];
            contentPlanes[i].transpose(inverseMatrix);
            Matrix.multiplyMV(intersectionPlaneSpace, 0, inverseMatrix, 0, intersection, 0);


            //intersectionPlaneSpace[0] = Utils.dotProduct(intersection,camera.getRight())/Utils.getMagnitude(intersection);
            //intersectionPlaneSpace[1] = Utils.dotProduct(intersection,camera.getDown())/Utils.getMagnitude(intersection);
            //intersectionPlaneSpace[2] = Utils.dotProduct(intersection,camera.getForward())/Utils.getMagnitude(intersection);
            if(intersectionPlaneSpace[0] > -width/2 && intersectionPlaneSpace[0] < width/2){
                if(intersectionPlaneSpace[1] > -height/2 && intersectionPlaneSpace[1] < height/2) {
                    return contentPlanes[i];
                }
            }

        }
        return null;
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
        ShaderManager.initalizeManager();

    }

    @Override
    public void onRendererShutdown() {

    }
}
