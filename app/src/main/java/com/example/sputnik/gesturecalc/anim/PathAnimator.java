package com.example.sputnik.gesturecalc.anim;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

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

    void setNoDrawRects(Rect... rects);

    void setCanvasSize(int width, int height);

    void addSpecialPoint(float x, float y);

    void addEvent(MotionEvent event);

    void clear();

    boolean isRunning();

    void updateCanvas(Canvas canvas);

    void recycle();

    int getCanvasWidth();

    int getCanvasHeight();
}
