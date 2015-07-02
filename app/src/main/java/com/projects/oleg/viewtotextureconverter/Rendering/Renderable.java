package com.projects.oleg.viewtotextureconverter.Rendering;

import com.projects.oleg.viewtotextureconverter.Geometry.Transform;

import java.nio.FloatBuffer;

/**
 * Created by momo-chan on 7/1/15.
 */
public interface Renderable {
    public void draw(Transform camera, Transform parent);
}
