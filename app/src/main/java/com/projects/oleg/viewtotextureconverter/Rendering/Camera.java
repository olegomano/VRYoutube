package com.projects.oleg.viewtotextureconverter.Rendering;

import com.projects.oleg.viewtotextureconverter.Geometry.Transform;

/**
 * Created by momo-chan on 7/1/15.
 */
public class Camera extends Transform {
    private float[] nfrc = {0,10,1,2};

    public float[] getPerspective(){
        return nfrc;
    }

    public void setRatio(float ratio){
        nfrc[2] = ratio;
    }

    public void setFov(float angle){

    }


}
