package com.projects.oleg.viewtotextureconverter.Rendering;

import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.projects.oleg.viewtotextureconverter.Geometry.Plane;
import com.projects.oleg.viewtotextureconverter.Geometry.VirtualDisplayPlane;

/**
 * Created by momo-chan on 7/12/15.
 */
public class Scene {
    private VirtualDisplayPlane[] contentPlane;
    private Camera sceneCamera;
    private Plane cursor;

    public void createScene(Context context, View[] content, int w, int h){
        sceneCamera = new Camera();
        cursor = new Plane();

    }

}
