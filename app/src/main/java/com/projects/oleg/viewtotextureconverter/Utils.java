package com.projects.oleg.viewtotextureconverter;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by momo-chan on 7/1/15.
 */
public class Utils {
    public static void print(String object){
        if(object != null){
            Log.d("Utils",object.toString());
        }
    }

    public static void printError(String errCode){
        if(errCode != null){
            Log.e("Utils", errCode);
        }
    }

    public static void print(String tag, Object object){
        if(object != null){
            Log.d(tag, object.toString());
        }
    }

    public static void printMat(float[] mat){
        for(int i = 0; i < mat.length; i+=4){
            String line = "[" + mat[i + 0] + "," + mat[i + 1] + "," + mat[i + 2] + "," + mat[i + 3] + "]";
            Utils.print(line);
        }
    }

    public static void printVec(float[] vec){
        String line = "{" + vec[0] + "," + vec[1] + "," + vec[2] + "," + vec[3] + "}";
        Utils.print(line);
    }

    public static FloatBuffer allocFloatBuffer(int size){
        ByteBuffer bb = ByteBuffer.allocateDirect(size * 4);
        bb.order(ByteOrder.nativeOrder());
        return bb.asFloatBuffer();
    }

    public static ShortBuffer allocShortBuffer(int size){
        ByteBuffer bb = ByteBuffer.allocateDirect(size * 2);
        bb.order(ByteOrder.nativeOrder());
        return bb.asShortBuffer();
    }

    public static void crossProduct(float[] v1, float[] v2, float[] res){
        res[0] = v1[1]*v2[2] - v1[2]*v2[1];
        res[1] = -(v1[0]*v2[2] - v1[2]*v2[0]);
        res[2] = v1[0]*v2[1] - v1[1]*v2[0];
        res[3] = 0;
    }

    public static float dotProduct(float[] v1, float[] v2){
        return 0;
    }
}
