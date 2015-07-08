package com.projects.oleg.viewtotextureconverter.Geometry;

import android.app.Presentation;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;

import com.projects.oleg.viewtotextureconverter.Rendering.Camera;
import com.projects.oleg.viewtotextureconverter.Shader.Bitmap3DShader;
import com.projects.oleg.viewtotextureconverter.Shader.OES3DShader;
import com.projects.oleg.viewtotextureconverter.Shader.ShaderManager;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;

import static android.content.Context.DISPLAY_SERVICE;

/**
 * Created by momo-chan on 7/3/15.
 */
public class VirtualDisplayPlane extends Plane implements SurfaceTexture.OnFrameAvailableListener {
    private static int virtualDisplayCount = 0;

    private Surface surface;
    private SurfaceTexture surfaceTexture;
    private VirtualDisplay display;
    private TextureManager.Texture displayTexture;
    private Presentation presentation;
    private String textureName = "VirtualDisplay";

    private volatile boolean displayCreated = false;
    private volatile boolean textureUpdated = false;

    public void createDisplay(final Context context, final View content, int w, int h){
        textureName+=++virtualDisplayCount;

        DisplayManager displayManager = (DisplayManager) context.getSystemService(DISPLAY_SERVICE);
        displayTexture = TextureManager.getManager().createOESSTexture(textureName,w,h);
        setTexture(displayTexture);

        surfaceTexture = new SurfaceTexture(displayTexture.getId());
        surfaceTexture.setDefaultBufferSize(w,h);
        surfaceTexture.setOnFrameAvailableListener(this);
        surface = new Surface(surfaceTexture);
        display = displayManager.createVirtualDisplay(textureName,w,h, DisplayMetrics.DENSITY_DEFAULT,surface,DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY);

        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                presentation = new Presentation(context, display.getDisplay());
                presentation.setContentView(content);
                presentation.show();
                synchronized (this) {
                    displayCreated = true;
                }
            }
        } , 15000);


    }

    public void setLoadTxt(){
        setShader(ShaderManager.getManager().getShader(Bitmap3DShader.SHADER_KEY));
        setTexture(TextureManager.getManager().getErrorTexture());
    }

    public void setContentTxt(){
        setShader(ShaderManager.getManager().getShader(OES3DShader.SHADER_KEY));
        setTexture(displayTexture);
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
