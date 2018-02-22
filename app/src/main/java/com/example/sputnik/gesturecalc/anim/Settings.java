package com.example.sputnik.gesturecalc.anim;

/**
 * Created by Sputnik on 2/22/2018.
 */

public class Settings implements Customizable {
    private float startSize = 16f;
    private float endSize = 0f;
    private float spacing = 20f;
    private int opacity = 164;
    private long duration = 900l;
    private FactoryAnimator.Type type;

    public FactoryAnimator.Type getType() {
        return type;
    }

    public void setType(FactoryAnimator.Type type) {
        this.type = type;
    }

    public void setStartSize(float size) {
        startSize = size;
    }

    @Override
    public void setEndSize(float size) {
        endSize = size;
    }

    @Override
    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    @Override
    public void setAnimationDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public void setSpacting(float spacing) {
        this.spacing = spacing;
    }

    @Override
    public float getStartSize() {
        return startSize;
    }

    @Override
    public float getEndSize() {
        return endSize;
    }

    @Override
    public int getOpacity() {
        return opacity;
    }

    @Override
    public long getAnimationDuration() {
        return duration;
    }

    @Override
    public float getSpacing() {
        return spacing;
    }
}
