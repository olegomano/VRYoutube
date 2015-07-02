package com.projects.oleg.viewtotextureconverter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import com.projects.oleg.viewtotextureconverter.Geometry.Plane;
import com.projects.oleg.viewtotextureconverter.Shader.BitmapSpriteShader;
import com.projects.oleg.viewtotextureconverter.Shader.Shader;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by momo-chan on 7/1/15.
 */
public class MyRenderer implements CardboardView.StereoRenderer {
    private Context mContext;

    private int errorTexture;
    private BitmapSpriteShader shader = new BitmapSpriteShader();
    private Plane tstPlane = new Plane();


    public MyRenderer(Context context){
        super();
        mContext = context;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClearColor(1,0,0,0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        tstPlane.draw(null,null);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        shader.initShader();
        tstPlane.setShader(shader);
        tstPlane.setTexture(TextureManager.getManager().createTextureFromReasource(mContext,R.drawable.errorloadingpng,"ERROR").getId());
    }

    @Override
    public void onRendererShutdown() {

    }

    private int genTexture(int dwb){
        int[] txture = new int[1];
        GLES20.glGenTextures(1, txture, 0);
        if(txture[0] ==-1){
            Utils.printError("Error creating Texture");
        }
        Shader.checkGlError("Created texture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, txture[0]);
        Shader.checkGlError("Bound Texture");
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_REPEAT);

        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.errorloadingpng);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,bmp,0);
        bmp.recycle();
        return  txture[0];
    }
}
