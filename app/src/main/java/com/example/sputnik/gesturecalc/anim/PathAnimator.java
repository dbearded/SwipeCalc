package com.example.sputnik.gesturecalc.anim;

import android.graphics.Canvas;

/**
 * Created by Sputnik on 2/16/2018.
 */

public interface PathAnimator {
    public void setStartSize(float size);

    public void setEndSize(float size);

//    public void setCircleCenterSpacing(float spacing);

    public void setOpacity(int opacity);

    public void setAnimationDuration(int duration);

//    public void setAnimationType(boolean type);

    public float getStartSize();

    public float getEndSize();

//    public float getCircleCenterSpacing();

    public int getOpacity();

    public long getAnimationDuration();

//    public boolean getAnimationType();

    public void reDrawTo(int progress);

    public void setCanvasSize(int width, int height);

    public void addSpecialPoint(float x, float y);

    public void addPoint(float x, float y, boolean newContour);

//    public void setAnimType(boolean circle);

    public void reset();

    public boolean isRunning();

    public void updateCanvas(Canvas canvas);

    public void recycle();
}
