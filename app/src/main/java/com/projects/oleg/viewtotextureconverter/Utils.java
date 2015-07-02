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
            Log.e("Utils",errCode);
        }
    }

    public static void print(String tag, Object object){
        if(object != null){
            Log.d(tag,object.toString());
        }
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
}
