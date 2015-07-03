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
import com.projects.oleg.viewtotextureconverter.Rendering.Camera;
import com.projects.oleg.viewtotextureconverter.Shader.BitmapSpriteShader;
import com.projects.oleg.viewtotextureconverter.Shader.Shader;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by momo-chan on 7/1/15.
 */
public class MyRenderer implements CardboardView.StereoRenderer {
    private Context mContext;

    private Camera camera = new Camera();

    private BitmapSpriteShader shader = new BitmapSpriteShader();
    private Plane tstPlane = new Plane();


    public MyRenderer(Context context){
        super();
        mContext = context;
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }
    private float dx = 0;
    private float increment = .03f;
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClearColor(1, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        dx+=increment;
        if(dx < -1 || dx > 1){
            increment*=-1;
        }
        tstPlane.displace(increment,0,0);
        tstPlane.draw(camera,null);
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
        tstPlane.setShader(shader);
        tstPlane.setTexture(TextureManager.getManager().createTextureFromReasource(R.drawable.errorloadingpng, "loading"));
        tstPlane.scale(1.0f / 9.0f, 1.0f / 16.0f, 1);
        tstPlane.scale(5,5,0);

    }

    @Override
    public void onRendererShutdown() {

    }
}
