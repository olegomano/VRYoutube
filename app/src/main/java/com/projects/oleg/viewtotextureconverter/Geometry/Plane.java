package com.projects.oleg.viewtotextureconverter.Geometry;

import android.opengl.Matrix;

import com.projects.oleg.viewtotextureconverter.MyRenderer;
import com.projects.oleg.viewtotextureconverter.Rendering.Camera;
import com.projects.oleg.viewtotextureconverter.Shader.Shader;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;
import com.projects.oleg.viewtotextureconverter.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

/**
 * Created by momo-chan on 7/1/15.
 */
public class Plane extends Transform {
    protected final float[] verts = {
                            -1,-1,0,1, //lb
                            -1,1,0,1, // lt
                             1,1,0,1, //rt
                             1,-1,0,1, //rb
    };
    protected final float[] uvCoords = {
                             0,1, //lb
                             0,0, //lt
                             1,0, //rt
                             1,1, //rb
    };

    protected final short[] drawOrder = {0,1,2,0,2,3};

    protected float[] bounds = new float[16];

    protected FloatBuffer vertsBuffer;
    protected FloatBuffer uvCoordsBuffer;
    protected ShortBuffer drawOrderBuffer;

    protected boolean drawing = true;

    protected HashMap<String,Object> parameters = new HashMap<>();

    protected Shader shader;
    protected TextureManager.Texture texture;

    protected volatile OnRayTraceStatusListener listener;

    public Plane(){
        super();
        vertsBuffer = Utils.allocFloatBuffer(verts.length);
        uvCoordsBuffer = Utils.allocFloatBuffer(uvCoords.length);
        drawOrderBuffer = Utils.allocShortBuffer(drawOrder.length);
        vertsBuffer.put(verts);
        uvCoordsBuffer.put(uvCoords);
        drawOrderBuffer.put(drawOrder);
        vertsBuffer.position(0);
        uvCoordsBuffer.position(0);
        drawOrderBuffer.position(0);
    }

    public float getWidth(){
        return scale[0]*2;
    }

    public float getHeight(){
        return scale[1]*2;
    }

    public void setDraw(boolean status){
        drawing = status;
    }

    private float[] scaledVerts = new float[16];
    public float[] getBounds(){
        for(int i = 0; i < 16; i+=4){
            scaledVerts[i + 0] = scale[0]*verts[i + 0];
            scaledVerts[i + 1] = scale[1]*verts[i + 1];
            scaledVerts[i + 2] = scale[2]*verts[i + 2];
            scaledVerts[i + 3] =          verts[i + 3];
        }
        for(int i = 0; i < 4; i++){
            Matrix.multiplyMV(bounds,i*4,modelMatrix,0,scaledVerts,i*4);
        }
        return bounds;
    }

    public boolean shouldCull(Camera camera){
        float[] bounds = getBounds();
        for(int i = 0; i < 4; i++){
            float angle = Utils.dotProduct(camera.getForward(),0,bounds,i*4) / Utils.getMagnitude(bounds,i*4);
            if(angle <= 0){
                return true;
            }
        }
        return false;
    }

    public void setRayTraceStatusListener(OnRayTraceStatusListener listener){
        this.listener = listener;
    }

    public void setShader(Shader s){
        shader = s;
    }

    public void setTexture(TextureManager.Texture txt){
        texture = txt;
    }

    public void onClick(MyRenderer.RayTraceResults results){
        if(listener != null){
            listener.onClick(results);
        }
    }

    public void onOver(MyRenderer.RayTraceResults results){
        if(listener != null){
            listener.onOver(results);
        }
    }

    public void draw(Camera camera, float[] parent){
        if(!drawing) return;

        if(texture == null){
            texture = TextureManager.getManager().getErrorTexture();
        }
        if(shader!=null){
            shader.draw(camera,modelMatrix,scale,texture,vertsBuffer,uvCoordsBuffer,drawOrderBuffer,parameters);
        }
    }

    public void putParam(String key, Object data){
        parameters.put(key,data);
    }

    public interface OnRayTraceStatusListener{
        public void onOver(MyRenderer.RayTraceResults results);
        public void onClick(MyRenderer.RayTraceResults results);
    }

}
