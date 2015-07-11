package com.projects.oleg.viewtotextureconverter.Shader;

import java.util.HashMap;

/**
 * Created by momo-chan on 7/8/15.
 */
public class ShaderManager {
    private Shader[] initList = {
            new Bitmap3DShader(),
            new BitmapSpriteShader(),
            new OES3DShader(),
            new OES3DRayTraceShader()
    };

    private static ShaderManager mthis;
    private HashMap<String,Shader> shaderTable = new HashMap<>();
    public static void initalizeManager(){
        mthis = new ShaderManager();
    }

    public static ShaderManager getManager(){
        return mthis;
    }

    private ShaderManager(){
        for(int i = 0; i < initList.length; i++){
            initList[i].initShader();
            shaderTable.put(initList[i].getKey(),initList[i]);
        }
    }

    public Shader getShader(String key){
        return shaderTable.get(key);
    }
}
