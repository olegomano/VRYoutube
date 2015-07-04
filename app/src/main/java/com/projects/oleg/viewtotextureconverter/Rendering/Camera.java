package com.projects.oleg.viewtotextureconverter.Rendering;

import com.projects.oleg.viewtotextureconverter.Geometry.Transform;

/**
 * Created by momo-chan on 7/1/15.
 */
public class Camera extends Transform {
    private float[] nfrc = {0,10,1,2}; // near far ratio constant

    public float[] getPerspective(){
        return nfrc;
    }

    public void setRatio(float ratio){
        nfrc[2] = ratio;
    }

    public void setFov(float angle){

    }

    public void setNearFar(float near, float far){
        nfrc[0] = near;
        nfrc[1] = far;
    }

    public float getFov(){
        return nfrc[3];
    }

    public float getFovAsAngle(){
        return 0;
    }

    public void copyFrom(Camera other){
        super.copyFrom(other);
        System.arraycopy(other.nfrc,0,nfrc,0,nfrc.length);
    }


}
