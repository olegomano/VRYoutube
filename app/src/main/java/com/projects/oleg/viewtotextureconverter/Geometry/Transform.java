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
        float[] newNorm = new float[4];
        float[] origin = getOrigin();
        for(int i = 0; i < 4;i++){
            newNorm[i] = point[i] - origin[i];
        }
        Utils.print("Looking at " );
        Utils.printVec(point);
        Utils.print("Down ");
        Utils.printVec(down);
        Utils.normalize(newNorm);
        createBase(newNorm, down);
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
        //Matrix.setIdentityM(modelMatrix,0);
        Utils.normalize(forward);
        Utils.normalize(right);
        Utils.normalize(down);
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
        float[] right = {0,0,0,0};
        Utils.print("Creating base: ");
        Utils.printVec(forward);
        Utils.printVec(down);
        Utils.crossProduct(forward,down,right);
        createBase(forward,right,down);
    }

    public void setOrigin(float[] newOrigin){
        System.arraycopy(newOrigin,0,modelMatrix,12,4);
    }

    public void setScale(float x, float y, float z){
        scale[0] = x;
        scale[1] = y;
        scale[2] = z;
    }

    public void rotateAboutPoint(float[] axis,  float angle, float[] point) {
        Matrix.setIdentityM(lBuffMat,0);
        Matrix.translateM(lBuffMat, 0, -point[0], -point[1], -point[2]);
        applyTransform(lBuffMat);
        Matrix.setRotateM(lBuffMat, 0, angle, axis[0], axis[1], axis[2]);
        applyTransform(lBuffMat);
        Matrix.setIdentityM(lBuffMat, 0);
        Matrix.translateM(lBuffMat, 0, point[0], point[1], point[2]);
        applyTransform(lBuffMat);
    }

    public float[] getTransform(){
        return modelMatrix;
    }

    public float[] getScale(){
        return scale;
    }

    private float[] origin = new float[4];
    public float[] getOrigin() {
        System.arraycopy(modelMatrix,12,origin,0,4);
        return origin;
    }

    private float[] forward = new float[4];
    public float[] getForward() {
        System.arraycopy(modelMatrix,8,forward,0,4);
        return forward;
    }


    public float[] down  = new float[4];
    public float[] getDown() {
        System.arraycopy(modelMatrix,4,down,0,4);
        return down;
    }


    private float[] right = new float[4];
    public float[] getRight() {
        System.arraycopy(modelMatrix,0,right,0,4);
        return right;
    }


    public void transpose(float[] transposed){
        Matrix.transposeM(transposed,0,modelMatrix,0);
    }


    public void copyFrom(Transform other){
        System.arraycopy(other.modelMatrix,0,modelMatrix,0,modelMatrix.length);
        System.arraycopy(other.scale,0,scale,0,scale.length);
    }
}

