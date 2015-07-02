package com.projects.oleg.viewtotextureconverter.Geometry;

import android.opengl.Matrix;

/**
 * Created by momo-chan on 7/1/15.
 */
public class Transform {
    protected float[] modelMatrix = new float[16];
    protected float[] scale = {1,1,1};
    private float[] lBuffMat = new float[16];
    private float[] rBuffmat = new float[16];

    public Transform() {
        Matrix.setIdentityM(modelMatrix, 0);
    }

    public void lookAt(float[] point, float[] down) {

    }

    public void applyTransform(float[] transform) {
        System.arraycopy(modelMatrix, 0, rBuffmat, 0, modelMatrix.length);
        Matrix.multiplyMM(modelMatrix, 0, transform, 0, rBuffmat, 0);
    }

    public void scale(float x, float y, float z){
        scale[0]*=x;
        scale[1]*=y;
        scale[2]*=z;
    }

    public void createBase(float[] forward, float[] right, float[] down) {

    }

    public void setOrigin(float[] newOrigin){

    }

    public void rotateAboutPoint(float[] axis, float[] point) {

    }

    public float[] getTransform(){
        return modelMatrix;
    }

    public float[] getScale(){
        return scale;
    }

    public float[] getOrigin() {
        return null;
    }

    public float[] getForward() {
        return null;
    }

    public float[] getRight() {
        return null;
    }

    public float[] getDown() {
        return null;
    }
}

