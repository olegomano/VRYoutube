package com.projects.oleg.viewtotextureconverter.Shader;

import android.opengl.GLES20;

import com.projects.oleg.viewtotextureconverter.Rendering.Camera;
import com.projects.oleg.viewtotextureconverter.Texture.TextureManager;
import com.projects.oleg.viewtotextureconverter.Utils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

/**
 * Created by momo-chan on 7/3/15.
 */
public class Bitmap3DShader extends Shader {
    // (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax - OldMin)) + NewMin
    public static String SHADER_KEY = "BMP3DSHADER";

    private final String mVertexShader =
                    "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 cameraMatrix;" +
                    "uniform vec4 perspective;"+  // near far ratio constant
                    "uniform vec4 scale; " +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec3 vTextureCoord;\n" +
                    "varying vec2 vTextureCoordInv;"+
                    "void main() {\n" +
                    "  vec4 scaledPos = vec4(1,1,1,1); "+
                    "  scaledPos.x = scale.x * aPosition.x;"+
                    "  scaledPos.y = scale.y * aPosition.y;"+
                    "  scaledPos.z = scale.z * aPosition.z;"+
                    "  gl_Position =  uMVPMatrix * scaledPos;\n " +
                    "  gl_Position =  cameraMatrix * gl_Position; " +

                  //  "  vTextureCoord = vec2( (aTextureCoord.x) / (gl_Position.z + perspective.w) , (aTextureCoord.y) / (gl_Position.z + perspective.w));\n" +
                    "    vTextureCoord = vec3(aTextureCoord.x / (perspective.w + gl_Position.z),aTextureCoord.y / (perspective.w + gl_Position.z), gl_Position.z);"+
                    "  vTextureCoordInv = vec2(1.0f / (perspective.w + gl_Position.z), 1.0f / (perspective.w + gl_Position.z) );"+

                    "  gl_Position.x = (perspective.w * perspective.z * gl_Position.x) / (perspective.w + gl_Position.z);"+
                    "  gl_Position.y = (perspective.w * gl_Position.y) / (perspective.w + gl_Position.z);"+
                    "  gl_Position.z = ( (2.0f*(gl_Position.z - perspective.x))/(perspective.y - perspective.x) ) - 1.0f;"+
                    "}\n";

    private final String mFragmentShader =
                    "precision mediump float;\n" +
                    "varying vec3 vTextureCoord;\n" +
                    "varying vec2 vTextureCoordInv;"+
                    "uniform sampler2D sTexture;\n" +
                    "void main() {\n" +
                    "  vec2 txtCoords = vec2(vTextureCoord.x / vTextureCoordInv.x, vTextureCoord.y / vTextureCoordInv.y); "+
                    "  gl_FragColor = texture2D(sTexture, txtCoords);\n" +
                    "}\n";


    private int programHandle;
    private int modelMatrixHandle;
    private int vertexHandle;
    private int uvHandle;
    private int samplerHandle;
    private int scaleHandle;
    private int cameraMatrixHandle;
    private int perspectiveHandle;

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

    }

    @Override
    public String getKey() {
        return SHADER_KEY;
    }

    @Override
    public void draw(Camera camera, float[] modelMatrix, float[] scale, TextureManager.Texture texture, FloatBuffer verts, FloatBuffer uv, ShortBuffer drawOrder, HashMap params) {
        GLES20.glUseProgram(programHandle);
        checkGlError("use program");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if(texture.getType() != GLES20.GL_TEXTURE_2D){
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TextureManager.getManager().getErrorTexture().getId());
            Utils.printError("EROR TEXTURE TYPE MISMATCH");
        }else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getId());
        }

        checkGlError("Bind Texture " + texture);
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


    }


}
