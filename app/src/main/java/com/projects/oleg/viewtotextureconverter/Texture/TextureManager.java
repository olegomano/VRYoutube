package com.projects.oleg.viewtotextureconverter.Texture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.projects.oleg.viewtotextureconverter.R;
import com.projects.oleg.viewtotextureconverter.Utils;

import java.util.HashMap;

/**
 * Created by momo-chan on 7/2/15.
 */
public class TextureManager {
    private static TextureManager singleton;

    public static void createSingleton(Context context){
        singleton = new TextureManager(context);
        singleton.createTextureFromReasource(R.drawable.errorloadingpng,ERROR_TEXTURE);
    }

    public static TextureManager getManager(){
        if(singleton == null){
            return null;
        }
        return singleton;
    }

    public class Texture{
        private int id;
        private int width;
        private int height;
        private int type;

        public Texture(int id, int width, int height, int type){
            this.id = id;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        public Texture(Texture other){
            id = other.getId();
            width = other.getWidth();
            height = other.getHeight();
            type = other.getType();
        }

        public int getId(){
            return id;
        }

        public int getWidth(){
            return width;
        }

        public int getHeight(){
            return height;
        }

        public int getType(){
            return type;
        }

    }
    public static String ERROR_TEXTURE = "ERROR_TEXTURE";
    private HashMap<String,Texture> textures = new HashMap<>();
    private Context context;
    public TextureManager(Context context){
        this.context = context;
    }

    public Texture getTexture(String name){
        return textures.get(name);
    }

    public Texture createTextureFromBitmap(Bitmap bmp, String name){
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        Texture newTexture = new Texture(texture[0],bmp.getWidth(),bmp.getHeight(),GLES20.GL_TEXTURE_2D);
        textures.put(name,newTexture);

        Utils.print("Created texture " + newTexture.getId());

        return newTexture;
    }

    public Texture createTextureFromReasource(int resource, String name){
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),resource);
        return createTextureFromBitmap(bmp, name);
    }

    public Texture getErrorTexture(){
        return textures.get(ERROR_TEXTURE);
    }

    public Texture createOESSTexture(String name, int w, int h) {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        Texture newTexture = new Texture(texture[0],w,h, GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        textures.put(name,newTexture);

        Utils.print("Created new OES texture " + newTexture.getId());

        return newTexture;
    }



}
