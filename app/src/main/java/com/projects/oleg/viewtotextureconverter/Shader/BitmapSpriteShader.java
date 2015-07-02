package com.projects.oleg.viewtotextureconverter.Shader;

import android.graphics.drawable.shapes.Shape;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.projects.oleg.viewtotextureconverter.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by momo-chan on 7/1/15.
 */
public class BitmapSpriteShader extends Shader {
    private final String mVertexShader =
                    "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position =   aPosition;\n" +
                    "  vTextureCoord = aTextureCoord;\n" +
                    "}\n";

    private final String mFragmentShader =
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "  vec4 tstColor = vec4(.5f,.5f,.5f,.5f);"+
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
              //      "  gl_FragColor = tstColor; "+
                    "}\n";


    private int programHandle;
    private int modelMatrixHandle;
    private int vertexHandle;
    private int uvHandle;
    private int samplerHandle;


    @Override
    public void initShader() {
        programHandle = createProgram(mVertexShader,mFragmentShader);
        checkGlError("got program handle");
        modelMatrixHandle = GLES20.glGetAttribLocation(programHandle,"uMVPMatrix");
        checkGlError("got model matrix handle");
        vertexHandle = GLES20.glGetAttribLocation(programHandle,"aPosition");
        checkGlError("got vertex handle");
        uvHandle = GLES20.glGetAttribLocation(programHandle,"aTextureCoord");
        checkGlError("got uv handle");
        samplerHandle = GLES20.glGetUniformLocation(programHandle,"sTexture");
        checkGlError("Got sampler");

    }
    private float[] mMatrix = new float[16];
    @Override
    public void draw(float[] camera, float[] modelMatrix, int texture, FloatBuffer verts, FloatBuffer uv, ShortBuffer drawOrder) {
        if(modelMatrix == null){
            Matrix.setIdentityM(mMatrix,0);
        }else{
            System.arraycopy(modelMatrix,0,mMatrix,0,mMatrix.length);
        }

        GLES20.glUseProgram(programHandle);
        checkGlError("use program");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        checkGlError("Bind Texture " + texture);
        GLES20.glUniform1i(samplerHandle,0);
        checkGlError("Passed texture");
        GLES20.glEnableVertexAttribArray(vertexHandle);
        checkGlError("Enabled vertex handle");
        GLES20.glEnableVertexAttribArray(uvHandle);
        checkGlError("Enabled uv handle");

        GLES20.glVertexAttribPointer(vertexHandle, 4, GLES20.GL_FLOAT, false, 16, verts);
        checkGlError("Passed vertecies");

        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 8, uv);
        checkGlError("Passed uv");

        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, mMatrix, 0);
        checkGlError("Passed model mat");

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.capacity(), GLES20.GL_UNSIGNED_SHORT, drawOrder);
        checkGlError("Draw");

        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);


    }


}
