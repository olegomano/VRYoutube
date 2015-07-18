package com.projects.oleg.viewtotextureconverter.Geometry;

import android.app.Presentation;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.webkit.WebView;

import com.projects.oleg.viewtotextureconverter.MyRenderer;
import com.projects.oleg.viewtotextureconverter.Rendering.Camera;
import com.projects.oleg.viewtotextureconverter.Shader.Bitmap3DShader;
import com.projects.oleg.viewtotextureconverter.Shader.OES3DShader;
import com.projects.oleg.viewtotextureconverter.Shader.ShaderManager;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;
import com.projects.oleg.viewtotextureconverter.Utils;

import java.util.FormatFlagsConversionMismatchException;

import static android.content.Context.DISPLAY_SERVICE;

/**
 * Created by momo-chan on 7/3/15.
 */
public class VirtualDisplayPlane extends Plane implements SurfaceTexture.OnFrameAvailableListener {
    private static int virtualDisplayCount = 0;
    private Handler uiThread;

    private Surface surface;
    private SurfaceTexture surfaceTexture;
    private VirtualDisplay display;
    private TextureManager.Texture displayTexture;
    private Presentation presentation;
    private View contentView;
    private String textureName = "VirtualDisplay";

    private volatile boolean displayCreated = false;
    private volatile boolean textureUpdated = false;

    public void createDisplay(final Context context, final View content, int w, int h){
        textureName+=++virtualDisplayCount;

        DisplayManager displayManager = (DisplayManager) context.getSystemService(DISPLAY_SERVICE);
        displayTexture = TextureManager.getManager().createOESSTexture(textureName,w,h);
        setTexture(displayTexture);

        contentView = content;
        surfaceTexture = new SurfaceTexture(displayTexture.getId());
        surfaceTexture.setDefaultBufferSize(w,h);
        surfaceTexture.setOnFrameAvailableListener(this);
        surface = new Surface(surfaceTexture);
        display = displayManager.createVirtualDisplay(textureName,w,h, DisplayMetrics.DENSITY_DEFAULT,surface,DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY);

        uiThread = new Handler(context.getMainLooper());

        uiThread.postDelayed(new Runnable() {
            @Override
            public void run() {
                presentation = new Presentation(context, display.getDisplay());
                presentation.setContentView(content);
                presentation.show();
                synchronized (this) {
                    displayCreated = true;
                }
                setContentTxt();
            }
        } , 15000);
    }

    public void dispatchTouchEvent(final float x, final float y){
        if(contentView != null){
            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    int xPixels = (int) (x*contentView.getWidth());
                    int yPixels = (int) (y*contentView.getHeight());
                    MotionEvent down = MotionEvent.obtain( SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN,
                            xPixels, yPixels, 0);

                    MotionEvent up = MotionEvent.obtain( SystemClock.uptimeMillis(),
                            SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP,
                            xPixels, yPixels, 0);
                    contentView.dispatchTouchEvent(down);
                    contentView.dispatchTouchEvent(up);

                }
            });
        }
    }

    public void setLoadTxt(){
        setShader(ShaderManager.getManager().getShader(Bitmap3DShader.SHADER_KEY));
        setTexture(TextureManager.getManager().getTexture(MyRenderer.LOADING_TEXTURE));
    }

    public void setContentTxt(){
        setShader(ShaderManager.getManager().getShader(OES3DShader.SHADER_KEY));
        setTexture(displayTexture);
    }

    public TextureManager.Texture getDisplayTexture(){
        return displayTexture;
    }

    public View getContent(){
        return contentView;
    }

    public void swapContent(VirtualDisplayPlane other){
        if(!displayCreated) return;

        TextureManager.Texture mTexture = displayTexture;
        Surface mSurface = surface;
        SurfaceTexture mSurfaceTexture = surfaceTexture;
        VirtualDisplay mDisplay = display;
        Presentation mPresentation = presentation;
        View mView = contentView;
        String mTextureName = textureName;

        textureName = other.textureName;
        surface = other.surface;
        surfaceTexture = other.surfaceTexture;
        display = other.display;
        presentation = other.presentation;
        contentView = other.contentView;
        textureName = other.textureName;
        surfaceTexture.setOnFrameAvailableListener(this);

        other.displayTexture = mTexture;
        other.surface = mSurface;
        other.surfaceTexture = mSurfaceTexture;
        other.display = mDisplay;
        other.presentation = mPresentation;
        other.contentView = mView;
        other.textureName = mTextureName;
        other.surfaceTexture.setOnFrameAvailableListener(other);





    }

    public void draw(Camera camera, float[] parent){
        synchronized (this){
            if(textureUpdated && displayCreated){
                surfaceTexture.updateTexImage();
                textureUpdated = false;
            }
        }
        super.draw(camera,parent);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this){
            textureUpdated = true;
        }
    }

    public void setContent(View content, int w, int h){

    }

    public String getTextureName(){
        return textureName;
    }

}
