package com.example.sputnik.gesturecalc.anim;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

/**
 * Created by Sputnik on 2/16/2018.
 */

public interface PathAnimator {

    void reDrawTo(int progress);

    void addNoDrawRect(Rect rect);

    void setCanvasSize(int width, int height);

    void addSpecialPoint(float x, float y);

    void addEvent(MotionEvent event);

    void clear();

    void applySettings(Settings settings);

    boolean isRunning();

    void updateCanvas(Canvas canvas);

    void recycle();

    int getCanvasWidth();

    int getCanvasHeight();

    Settings getSettings();
}
