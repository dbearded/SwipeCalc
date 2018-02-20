package com.example.sputnik.gesturecalc.anim;

import android.graphics.Canvas;

/**
 * Created by Sputnik on 2/16/2018.
 */

public interface PathAnimator {
    void setStartSize(float size);

    void setEndSize(float size);

    void setOpacity(int opacity);

    void setAnimationDuration(int duration);

    float getStartSize();

    float getEndSize();

    int getOpacity();

    long getAnimationDuration();

    void reDrawTo(int progress);

    void setCanvasSize(int width, int height);

    void addSpecialPoint(float x, float y);

    void addPoint(float x, float y, boolean newContour);

    void reset();

    boolean isRunning();

    void updateCanvas(Canvas canvas);

    void recycle();

    int getCanvasWidth();

    int getCanvasHeight();
}
