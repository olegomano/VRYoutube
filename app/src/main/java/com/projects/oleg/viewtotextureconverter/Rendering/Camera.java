package com.projects.oleg.viewtotextureconverter.Rendering;

import com.projects.oleg.viewtotextureconverter.Geometry.Transform;

/**
 * Created by momo-chan on 7/1/15.
 */
public class Camera extends Transform {
    private float[] nfrc = {.05f,50,1,4.1f}; // near far ratio constant
    private float[] nfrcRet = new float[4];
    public synchronized float[] getPerspective(){
        System.arraycopy(nfrc,0,nfrcRet,0,nfrc.length);
        return nfrcRet;
    }

    public synchronized void setRatio(float ratio){
        nfrc[2] = ratio;
    }

    public synchronized void setFov(float angle){
        float hAngle = angle/2;
        nfrc[3] = (float) (1.0f / (Math.tan(Math.toRadians(hAngle))));
    }

    public synchronized void setNearFar(float near, float far){
      //  nfrc[0] = near;
      //  nfrc[1] = far;
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
