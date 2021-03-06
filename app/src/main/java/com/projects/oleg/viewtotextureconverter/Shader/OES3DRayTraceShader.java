package com.projects.oleg.viewtotextureconverter.Shader;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.projects.oleg.viewtotextureconverter.Rendering.Camera;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;
import com.projects.oleg.viewtotextureconverter.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

/**
 * Created by momo-chan on 7/8/15.
 */
public class OES3DRayTraceShader extends Shader {
    // (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax - OldMin)) + NewMin
    public static String SHADER_KEY = "OES3DShaderRAYTRACE";
    public static String RAY_COORD_KEY = "RAYCOORDKEY";

    private final String mVertexShader =
                    "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 cameraMatrix;" +
                    "uniform vec4 perspective;"+  // near far ratio constant
                    "uniform vec4 scale; " +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "varying vec2 vTextureCoordInv;"+
                    "void main() {\n" +
                    "  vec4 scaledPos = vec4(1,1,1,1); "+
                    "  scaledPos.x = scale.x * aPosition.x;"+
                    "  scaledPos.y = scale.y * aPosition.y;"+
                    "  scaledPos.z = scale.z * aPosition.z;"+
                    "  gl_Position =  uMVPMatrix * scaledPos;\n " +
                    "  gl_Position =  cameraMatrix * gl_Position; " +

                    "  vTextureCoord = vec2( aTextureCoord.x / (gl_Position.z + perspective.w), aTextureCoord.y / (gl_Position.z + perspective.w) );"+
                    "  vTextureCoordInv = vec2(1.0f/ ( gl_Position.z + perspective.w), 1.0f / (gl_Position.z + perspective.w));"+

                    "  gl_Position.x = (perspective.w * perspective.z * gl_Position.x) / (perspective.w + gl_Position.z);"+
                    "  gl_Position.y = (perspective.w * gl_Position.y) / (perspective.w + gl_Position.z);"+
                    "  gl_Position.z = ( (2.0f*(gl_Position.z - perspective.x))/(perspective.y - perspective.x) ) - 1.0f;"+
                    "}\n";

    private final String mFragmentShader =
                    "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "varying vec2 vTextureCoordInv;"+
                    "uniform samplerExternalOES sTexture;\n" +
                    "uniform vec2 rayTrace;"+
                    "void main() {\n" +
                    "  vec2 reintTxt = vec2(vTextureCoord.x / vTextureCoordInv.x ,vTextureCoord.y / vTextureCoordInv.y );"+
                    "  gl_FragColor = texture2D(sTexture, reintTxt);\n" +
                    "  if( ( (reintTxt.x - rayTrace.x)*(reintTxt.x - rayTrace.x) + (reintTxt.y - rayTrace.y)*(reintTxt.y - rayTrace.y) ) < 0.0025f  ){"+
                    "       gl_FragColor = vec4( (gl_FragColor.x - .2f)/2.0f,(gl_FragColor.y - .5f)/2.0f,(gl_FragColor.z - .2f)/2.0f,(gl_FragColor.w - .5f)/2.0f);"+
                    "  }"+
                    "}\n";


    private int programHandle;
    private int modelMatrixHandle;
    private int vertexHandle;
    private int uvHandle;
    private int samplerHandle;
    private int scaleHandle;
    private int cameraMatrixHandle;
    private int perspectiveHandle;
    private int rayTraceHandle;


    @Override
    public void initShader() {
        programHandle = createProgram(mVertexShader,mFragmentShader);
        checkGlError("got program handle");
        modelMatrixHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");
        checkGlError("got model matrix handle");
        scaleHandle = GLES20.glGetUniformLocation(programHandle,"scale");
        checkGlError("got scale handle");
        vertexHandle = GLES20.glGetAttribLocation(programHandle, "aPosition");
        checkGlError("got vertex handle");
        uvHandle = GLES20.glGetAttribLocation(programHandle,"aTextureCoord");
        checkGlError("got uv handle");
        samplerHandle = GLES20.glGetUniformLocation(programHandle,"sTexture");
        checkGlError("Got sampler");
        cameraMatrixHandle = GLES20.glGetUniformLocation(programHandle,"cameraMatrix");
        perspectiveHandle = GLES20.glGetUniformLocation(programHandle,"perspective");
        rayTraceHandle = GLES20.glGetUniformLocation(programHandle,"rayTrace");

    }

    @Override
    public String getKey() {
        return SHADER_KEY;
    }

    @Override
    public void draw(Camera camera, float[] modelMatrix, float[] scale, TextureManager.Texture texture, FloatBuffer verts, FloatBuffer uv, ShortBuffer drawOrder, HashMap params) {
        GLES20.glUseProgram(programHandle);
        checkGlError("use program");
        if(texture.getType() != GLES11Ext.GL_TEXTURE_EXTERNAL_OES){
            Utils.printError("Texture type mismatch, skipping draw");
            return;
        }

        float[] rayTrace = (float[]) params.get(RAY_COORD_KEY);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture.getId());

        checkGlError("Bind Texture " + texture.getId());
        GLES20.glUniform1i(samplerHandle, 0);
        checkGlError("Passed texture");
        GLES20.glEnableVertexAttribArray(vertexHandle);
        checkGlError("Enabled vertex handle");
        GLES20.glEnableVertexAttribArray(uvHandle);
        checkGlError("Enabled uv handle");

        GLES20.glVertexAttribPointer(vertexHandle, 4, GLES20.GL_FLOAT, false, 16, verts);
        checkGlError("Passed vertecies");

        GLES20.glVertexAttribPointer(uvHandle, 2, GLES20.GL_FLOAT, false, 8, uv);
        checkGlError("Passed uv");

        GLES20.glEnableVertexAttribArray(scaleHandle);
        GLES20.glUniform4fv(scaleHandle, 1, scale, 0);

        GLES20.glEnableVertexAttribArray(rayTraceHandle);
        GLES20.glUniform2fv(rayTraceHandle,1,rayTrace,0);

        GLES20.glEnableVertexAttribArray(perspectiveHandle);
        GLES20.glUniform4fv(perspectiveHandle, 1, camera.getPerspective(), 0);

        checkGlError("Passed perspective");
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0);
        checkGlError("Passed model mat");

        GLES20.glUniformMatrix4fv(cameraMatrixHandle, 1, true, camera.getTransform(), 0);
        checkGlError("Passed camera matrix");



        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.capacity(), GLES20.GL_UNSIGNED_SHORT, drawOrder);
        checkGlError("Draw");

        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(uvHandle);
        GLES20.glDisableVertexAttribArray(scaleHandle);
        GLES20.glDisableVertexAttribArray(perspectiveHandle);
        GLES20.glDisableVertexAttribArray(rayTraceHandle);

    }


}