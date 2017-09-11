package com.merlin.transition.model;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * Created by ncm on 2017/6/16.
 */

public class Transit {

    public int statusBarHeight;
    public int titleHeight;
    public int windowWidth;
    public int windowHeight;

    public Bitmap bitmap;

    public int translationX;
    public int translationY;

    public Rect originRect = new Rect();
    public int originWidth;
    public int originHeight;

    public Rect targetRect = new Rect();
    public int targetWidth;
    public int targetHeight;

    public float scaleY;
    public float scaleX;


}
