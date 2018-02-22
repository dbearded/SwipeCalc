package com.example.sputnik.gesturecalc.anim;

/**
 * Created by Sputnik on 2/22/2018.
 */

public interface Customizable {
    void setStartSize(float size);

    void setEndSize(float size);

    void setOpacity(int opacity);

    void setAnimationDuration(long duration);

    void setSpacting(float spacing);

    float getStartSize();

    float getEndSize();

    int getOpacity();

    long getAnimationDuration();

    float getSpacing();
}
