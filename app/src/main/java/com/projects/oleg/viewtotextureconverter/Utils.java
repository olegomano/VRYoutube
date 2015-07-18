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
        //    Log.d(tag, object.toString());
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
        crossProduct(v1,0,v2,0,res,0);
        /*
        res[0] = v1[1]*v2[2] - v1[2]*v2[1];
        res[1] = -(v1[0]*v2[2] - v1[2]*v2[0]);
        res[2] = v1[0]*v2[1] - v1[1]*v2[0];
        res[3] = 0;
        */
    }

    public static void crossProduct(float[] v1, int v1Offset, float[] v2, int v2Offset, float[] res, int resOffset){
        res[0 + resOffset] = v1[1 + v1Offset]*v2[2 + v2Offset] - v1[2 + v1Offset]*v2[1 + v2Offset];
        res[1 + resOffset] = -(v1[0 + v1Offset]*v2[2 + v2Offset] - v1[2 + v1Offset]*v2[0 + v2Offset]);
        res[2 + resOffset] = v1[0 + v1Offset]*v2[1 + v2Offset] - v1[1 + v1Offset]*v2[0 + v2Offset];
        res[3 + resOffset] = 0;
    }

    public static float getMagnitude(float[] v){
        return getMagnitude(v,0);
    }

    public static float getMagnitude(float[] v, int offset){
        float mag = v[0 + offset]*v[0 + offset] + v[1 + offset]*v[1 + offset] + v[2 + offset]*v[2 + offset] + v[3 + offset]*v[3 + offset];
        return (float) Math.sqrt(mag);
    }

    public static void normalize(float[] vec){
        float mag = getMagnitude(vec);
        for(int i = 0; i < vec.length; i++){
            vec[i]/=mag;
        }
    }

    public static float dotProduct(float[] v1, float[] v2){
        return dotProduct(v1,0,v2,0);
    }

    public static float dotProduct(float[] v1, int v1Offset, float[] v2, int v2Offset){
        float dotProduct = 0;
        for(int i = 0; i < 4; i++){
            dotProduct+=( v1[i + v1Offset] * v2[i + v2Offset] );
        }
        return dotProduct;

    }


}
