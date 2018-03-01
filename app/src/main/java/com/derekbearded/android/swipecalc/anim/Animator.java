package com.derekbearded.android.swipecalc.anim;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sputnik on 2/22/2018.
 */

public abstract class Animator {

    public enum Type{
        Line("line"), Circle("circle");
        private final String id;

        Type(String id){
            this.id = id;
        }

        public static Type fromString(String symbol) {
            for (Type s : Type.values()) {
                if (s.id.equalsIgnoreCase(symbol)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("No Type with text: " + symbol);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    private Settings settings;
    private Type type;
    private ArrayList<Rect> noDrawRects = new ArrayList<>();
    private int canvasWidth, canvasHeight;

    Animator(Settings settings){
        this.settings = settings;
        this.type = settings.getType();
    }

    public static Animator makeAnimator(){
        return makeAnimator(new Settings());
    }

    public static Animator makeAnimator(Settings settings){
        Animator animator;
        switch (settings.getType()){
            case Line:
                animator = new LineAnimator(settings);
                break;
            case Circle:
                animator = new CircleAnimator(settings);
                break;
            default:
                animator = null;
        }
        return animator;
    }

    public abstract void reDrawTo(int progress);

    public abstract void addSpecialPoint(float x, float y);

    public abstract void addEvent(MotionEvent event);

    public abstract void clear();

    public abstract boolean isRunning();

    public abstract void updateCanvas(Canvas canvas);

    public abstract void recycle();

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public void setCanvasSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    public void setNoDrawRects(List<Rect> rects){
        noDrawRects = (ArrayList<Rect>) rects;
    }

    public List<Rect> getNoDrawRects(){
        return noDrawRects;
    }

    public void addNoDrawRect(Rect rect) {
        noDrawRects.add(rect);
    }

    boolean inNoDrawRects(float x, float y){
        boolean result = false;
        for (Rect rect :
                noDrawRects) {
            if (rect.contains((int) x, (int) y)) {
                return true;
            }
        }
        return result;
    }

    public final float getStartSize() {
        return settings.getStartSize();
    }

    public final void setStartSize(float size) {
        settings.setStartSize(size);
    }

    public final float getEndSize() {
        return settings.getEndSize();
    }

    public final void setEndSize(float size) {
        settings.setEndSize(size);
    }

    public final int getOpacity() {
        return settings.getOpacity();
    }

    public final void setOpacity(int opacity) {
        settings.setOpacity(opacity);
    }

    public final long getAnimationDuration() {
        return settings.getAnimationDuration();
    }

    public final void setAnimationDuration(long duration) {
        settings.setAnimationDuration(duration);
    }

    public final float getSpacing() {
        return settings.getSpacing();
    }

    public final void setSpacing(float spacing) {
        settings.setSpacing(spacing);
    }

    public final float getTouchSlop() {
        return settings.getTouchSlop();
    }

    public final void setTouchSlop(float touchSlop) {
        settings.setTouchSlop(touchSlop);
    }

    public final Settings getSettings() {
        return settings;
    }

    public static Animator changeSettings(Animator animator, Settings settings){
        if (animator == null){
            animator = makeAnimator(settings);
        } else {
            animator = changeType(animator, settings.getType());
            animator.settings = settings;
        }
        return animator;
    }

    public static Animator changeToDefault(Animator animator){
        return changeSettings(animator, new Settings());
    }

    public final Type getType(){
        return type;
    }

    public static Animator changeType(Animator animator, Type type){
        if (!animator.getType().equals(type)){
            int width = animator.getCanvasWidth();
            int height = animator.getCanvasHeight();
            List<Rect> rects = animator.getNoDrawRects();
            animator.recycle();
            animator.getSettings().setType(type);
            animator = makeAnimator(animator.getSettings());
            animator.setCanvasSize(width, height);
            animator.setNoDrawRects(rects);
        }
        return animator;
    }
}