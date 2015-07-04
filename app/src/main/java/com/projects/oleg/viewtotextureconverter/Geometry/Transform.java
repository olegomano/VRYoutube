package com.projects.oleg.viewtotextureconverter.Geometry;

import android.opengl.Matrix;

import com.projects.oleg.viewtotextureconverter.Utils;

import java.sql.RowId;

/**
 * Created by momo-chan on 7/1/15.
 */
public class Transform {
    protected float[] modelMatrix = new float[16];
    protected float[] scale = {1,1,1,1};
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

    public void displace(float x, float y, float z){
        Matrix.setIdentityM(lBuffMat,0);
        Matrix.translateM(lBuffMat, 0, x, y, z);
        applyTransform(lBuffMat);
    }

    public void scale(float x, float y, float z){
        scale[0]*=x;
        scale[1]*=y;
        scale[2]*=z;
    }

    public void createBase(float[] forward, float[] right, float[] down) {
        Matrix.setIdentityM(modelMatrix,0);
        for(int i = 0; i < 4; i++){
            modelMatrix[i + 0] = right[i];
        }
        for(int i = 0; i < 4; i++){
            modelMatrix[i + 4] = down[i];
        }
        for(int i = 0; i < 4; i++){
            modelMatrix[i + 8] = forward[i];
        }
    }

    public void createBase(float[] forward, float[] down){
        float[] right = new float[4];
        Utils.crossProduct(forward,down,right);
        createBase(forward,right,down);
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

    private float[] origin = new float[4];
    public float[] getOrigin() {
        for(int i = 0; i < 4;i++){
            origin[i] = modelMatrix[12 + i];
        }
        return origin;
    }

    private float[] forward = new float[4];
    public float[] getForward() {
        for(int i = 0; i < 4;i++){
            forward[i] = modelMatrix[8 + i];
        }
        return forward;
    }

    private float[] right = new float[4];
    public float[] getRight() {
        for(int i = 0; i < 4;i++){
            right[i] = modelMatrix[0 + i];
        }
        return right;
    }

    public float[] getDown() {
        return null;
    }

    public void copyFrom(Transform other){
        System.arraycopy(other.modelMatrix,0,modelMatrix,0,modelMatrix.length);
        System.arraycopy(other.scale,0,scale,0,scale.length);
    }
}

