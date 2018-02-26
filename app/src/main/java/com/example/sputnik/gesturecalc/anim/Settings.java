package com.example.sputnik.gesturecalc.anim;

/**
 * Created by Sputnik on 2/22/2018.
 */

public class Settings {
    private float startSize = 22f;
    private float endSize = 0f;
    private float spacing = 26f;
    private int opacity = 164;
    private long duration = 900l;
    private Animator.Type type = Animator.Type.Circle;
    private float touchSlop = 16f;

    public Animator.Type getType() {
        return type;
    }

    public void setType(Animator.Type type) {
        this.type = type;
    }

    public void setStartSize(float size) {
        startSize = size;
    }

    public void setEndSize(float size) {
        endSize = size;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public void setAnimationDuration(long duration) {
        this.duration = duration;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    public float getStartSize() {
        return startSize;
    }

    public float getEndSize() {
        return endSize;
    }

    public int getOpacity() {
        return opacity;
    }

    public long getAnimationDuration() {
        return duration;
    }

    public float getSpacing() {
        return spacing;
    }

    public float getTouchSlop() {
        return touchSlop;
    }

    public void setTouchSlop(float touchSlop){
        this.touchSlop = touchSlop;
    }
}
