package com.merlin.transition.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;

import com.merlin.transition.model.Transit;

/**
 * Created by ncm on 2017/6/16.
 */

public class CircleAnimView extends AnimView {

    private Canvas mCanvas;
    private Bitmap originalBitmap; // 画布bitmap
    private Xfermode xfermode;
    //画布宽度
    private int canvasWidth = 1080;
    //画布高度
    private int canvasHeight = 1920;
    //画圆
    private int cX = 1080 / 2;
    private int cY = 1920 / 2;
    private int radius = 5;
    private int maxRadius = 1920;
    private int increaseSpeed = 5;
    private boolean startCircleAnim = false;

    public CircleAnimView(Context context) {
        this(context, 0xfff2f2f2);
        init(context);
    }

    public CircleAnimView(Context context, int backgroundColor) {
        super(context, backgroundColor);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);

        setWillNotDraw(false);
        setBackgroundColor(backgroundColor);
    }

    @Override
    public void startAnim(Transit transit) {
        originalBitmap = Bitmap.createBitmap(transit.windowWidth < 1 ? canvasWidth : transit.windowWidth,
                transit.windowHeight < 1 ? canvasHeight : transit.windowHeight,
                Bitmap.Config.ARGB_4444);
        canvasWidth = transit.windowWidth;
        canvasHeight = transit.windowHeight;
        mCanvas = new Canvas(originalBitmap);
        startCircleAnim = true;
        cX = transit.targetWidth / 2 + transit.targetRect.left;
        if (cX < 1) {
            cX = transit.originWidth / 2 + transit.originRect.left;
        }
        cY = transit.targetHeight / 2 + transit.targetRect.top - transit.statusBarHeight - transit.titleHeight;
        if (cY < 1) {
            cY = transit.originHeight / 2 + transit.originRect.top - transit.statusBarHeight - transit.titleHeight;
        }
        radius = (int) Math.hypot(transit.targetWidth / 2, transit.targetHeight / 2);
        if (radius < 1) {
            radius = (int) Math.hypot(transit.originWidth / 2, transit.originHeight / 2);
        }
        maxRadius = (int) Math.hypot(canvasWidth, canvasHeight);

        if (canvasWidth < 1) {
            canvasWidth = 1080;
        }
        if (canvasHeight < 1) {
            canvasHeight = 1920;
        }
        if (cX < 1) {
            cX = canvasWidth / 2;
        }
        if (cY < 1) {
            cY = canvasHeight / 2;
        }
        if (radius < 1) {
            radius = 5;
        }
        if (maxRadius < 1) {
            maxRadius = (int) Math.hypot(canvasWidth, canvasHeight);
        }
        setBackgroundColor(0x00000000);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (startCircleAnim) {
            zoomDraw(canvas);
        }
    }

    private void notifyEnd() {
        if (listener != null) {
            listener.onAnimEnd();
        }
    }

    private void zoomDraw(Canvas canvas) {
        if (radius < maxRadius) {
            mCanvas.drawRect(0, 0, canvasWidth, canvasHeight, paint);
            paint.setXfermode(xfermode);
            mCanvas.drawCircle(cX, cY, radius, paint);
            radius += increaseSpeed;
            increaseSpeed += 6;
            paint.setXfermode(null);
            if (!originalBitmap.isRecycled()) {
                canvas.drawBitmap(originalBitmap, 0, 0, null);
            }
            invalidate();
        } else {
            startCircleAnim = false;
            setVisibility(GONE);
            //recycle
            if (originalBitmap != null) {
                originalBitmap.recycle();
                originalBitmap = null;
            }
            notifyEnd();
        }
    }

}
