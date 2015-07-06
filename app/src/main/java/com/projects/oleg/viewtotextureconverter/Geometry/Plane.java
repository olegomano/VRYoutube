package com.projects.oleg.viewtotextureconverter.Geometry;

import android.opengl.Matrix;

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
    protected float[] verts = {
                            -1,-1,0,1, //lb
                            -1,1,0,1, // lt
                             1,1,0,1, //rt
                             1,-1,0,1, //rb
    };
    protected float[] uvCoords = {
                             0,1, //lb
                             0,0, //lt
                             1,0, //rt
                             1,1, //rb
    };

    protected short[] drawOrder = {0,1,2,0,2,3};

    protected float[] bounds = new float[16];

    protected FloatBuffer vertsBuffer;
    protected FloatBuffer uvCoordsBuffer;
    protected ShortBuffer drawOrderBuffer;

    protected HashMap<String,Object> parameters = new HashMap<>();

    protected Shader shader;
    protected TextureManager.Texture texture;

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

    public float[] getBounds(){
        for(int i = 0; i < 4; i++){
            Matrix.multiplyMV(bounds,i*4,modelMatrix,0,verts,i*4);
        }
        return bounds;
    }

    public void setShader(Shader s){
        shader = s;
    }

    public void setTexture(TextureManager.Texture txt){
        Utils.print("Set texture to " + txt.getId());
        texture = txt;
    }

    public void draw(Camera camera, float[] parent){
        if(texture == null){
            texture = TextureManager.getManager().getErrorTexture();
        }
        if(shader!=null){
            shader.draw(camera,modelMatrix,scale,texture,vertsBuffer,uvCoordsBuffer,drawOrderBuffer);
        }
    }


}
