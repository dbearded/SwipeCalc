package com.example.sputnik.gesturecalc.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.MotionEvent;

import com.example.sputnik.gesturecalc.util.PathActivator;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Sputnik on 2/7/2018.
 */

class CircleAnimator extends com.example.sputnik.gesturecalc.anim.Animator {

    private Path path;
    private PathMeasure pathMeasure;
    private LinkedList<CircleHolder> circles = new LinkedList<>();
    private LinkedList<CircleHolder> circleSubset = new LinkedList<>();
    private LinkedList<CircleHolder> specialCircles = new LinkedList<>();
    private int newAnimationCount = 0;
    private int contourCount = 0;
    private float discreteLength;
    private float[] circPos = new float[2];
    private int animationCount;
    private boolean drawingSubset;
    private float prevX, prevY;
    private boolean invalidate;
    private boolean newContourPrevAdded;
    private CircleHolder drawCircle;
    private LinkedList<CircleHolder> drawCircles;

    class CircleHolder {
        private float x, y;
        private ShapeDrawable shape;

        CircleHolder(float diameter, float x, float y){
            OvalShape circle = new OvalShape();
            circle.resize(diameter, diameter);
            this.shape = new ShapeDrawable(circle);
            shape.getPaint().setColor(Color.parseColor("#a46fa7be"));
            shape.getPaint().setAlpha(getOpacity());
            this.x = x;
            this.y = y;
        }

        float getX() {
            return x;
        }

        void setX(float x) {
            this.x = x - shape.getShape().getWidth() / 2;
        }

        float getY() {
            return y;
        }

        void setY(float y) {
            this.y = y - shape.getShape().getHeight() / 2;
        }

        ShapeDrawable getShape() {
            return shape;
        }

        float getDiameter() {
            return shape.getShape().getWidth();
        }

        void setDiameter(float diameter) {
            shape.getShape().resize(diameter, diameter);
        }

        void setColor(int color){
            shape.getPaint().setColor(color);
        }

        void setTransferMode(PorterDuffXfermode xfermode){
            shape.getPaint().setXfermode(xfermode);
        }
    }

    public CircleAnimator(Settings settings){
        super(settings);
        path = new Path();
        pathMeasure = new PathMeasure();
    }

    public void reDrawTo(int progress) {
        reDrawCirclesTo(progress);
        drawingSubset = true;
    }

    private void reDrawCirclesTo(int progress) {
        circleSubset = (LinkedList<CircleHolder>) circles.clone();
        int count = (int) (((float)(100 - progress))/((float) 100)* circleSubset.size());
        for (int i = 0; i < count-1; i++) {
            circleSubset.removeLast();
        }
    }

    public void addSpecialPoint(float x, float y){
        if (inNoDrawRects(x, y)){
            return;
        }
        createCircle(x,y);
        CircleHolder specialCircle = circles.getLast();
        specialCircle.setColor(Color.RED);
        specialCircle.setDiameter(getStartSize());
        addAnimators();
    }

    @Override
    public void addEvent(MotionEvent event) {
        float evX = event.getX();
        float evY = event.getY();

        int actionType = event.getAction();
        switch (actionType) {
            case MotionEvent.ACTION_DOWN:
                if (inNoDrawRects(evX, evY)){
                    invalidate = true;
                    return;
                }
                addPoint(evX, evY, true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (invalidate){
                    return;
                }
                int count = event.getHistorySize();
                for (int i = 0; i < count; i++) {
                    float histX = event.getHistoricalX(i);
                    float histY = event.getHistoricalY(i);
                    if (PathActivator.euclidDistance(histX, histY, prevX, prevY) < getTouchSlop()) {
                        continue;
                    }
                    addPoint(histX, histY, false);
                }
                if (PathActivator.euclidDistance(evX, evY, prevX, prevY) < getTouchSlop()) {
                    break;
                }
                addPoint(evX, evY, false);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                invalidate = false;
                break;
        }
    }

    private void addPoint(float x, float y, boolean newContour) {
        if (newContour) {
            path.moveTo(x,y);
            prevX = x;
            prevY = y;
            discreteLength = 0;
            newContourPrevAdded = true;
        } else {
            path.lineTo(x,y);
            if (newContourPrevAdded) {
                contourCount++;
                newContourPrevAdded = false;
            }
            pathMeasure.setPath(path, false);
            for (int i = 0; i < contourCount; i++) {
                pathMeasure.nextContour();
            }
            addCircles();
            addAnimators();
            prevX = x;
            prevY = y;
        }

    }

    private void addCircles(){
        float spacing = getSpacing();
        float segmentLength = pathMeasure.getLength() - discreteLength;
        if (segmentLength < spacing) {
            return;
        }
        int circlesToAdd = (int) (segmentLength / spacing);
        for (int i = 0; i < circlesToAdd; i++) {
            discreteLength += spacing;
            pathMeasure.getPosTan(discreteLength, circPos, null);
            createCircle(circPos[0], circPos[1]);
        }
    }

    public void clear(){
        clearCircles();
        path.rewind();
        path.moveTo(prevX, prevY);
        pathMeasure.setPath(path, false);
        contourCount = 0;
        drawingSubset = false;
        newContourPrevAdded = true;
        discreteLength = 0;
    }

    private void clearCircles(){
        circles.clear();
        circleSubset.clear();
    }

    public boolean isRunning(){
        return animationCount != 0;
    }

    public void updateCanvas(Canvas canvas){
        drawCircles(canvas);
    }

    @Override
    public void recycle() {
        path.reset();
        circles.clear();
        circleSubset.clear();
        specialCircles.clear();
    }

    private void drawCircles(Canvas canvas){
        if (drawingSubset) {
            drawCircles = circleSubset;
            drawingSubset = false;
        } else {
            drawCircles = circles;
        }
        for (int i = 0; i < drawCircles.size(); i++) {
            drawCircle = drawCircles.get(i);
            if (drawCircle.getShape().getPaint().getColor() == Color.RED){
                specialCircles.add(drawCircle);
            }
            canvas.save();
            canvas.translate(drawCircle.getX(), drawCircle.getY());
            drawCircle.getShape().draw(canvas);
            canvas.restore();
        }
        while(!specialCircles.isEmpty()) {
            drawCircle = specialCircles.remove();
            canvas.save();
            canvas.translate(drawCircle.getX(), drawCircle.getY());
            drawCircle.getShape().draw(canvas);
            canvas.restore();
        }
    }

    private void createCircle(float x, float y) {
        float diameter = getStartSize();
        CircleHolder circle = new CircleHolder(diameter, x - diameter / 2, y - diameter / 2);
        circles.add(circle);
        newAnimationCount++;
    }

    // add animators to any new trace points
    private void addAnimators() {
        // Setting for disabling animations
        if (getAnimationDuration() == 0){
            newAnimationCount = 0;
            return;
        }
        while(newAnimationCount > 0){
            newCircleAnimation();
            newAnimationCount--;
        }
        // Now count is less than zero. Need to reset
        newAnimationCount = 0;
    }

    private void newCircleAnimation(){
        int size = circles.size();
        ObjectAnimator animator = ObjectAnimator.ofFloat(circles.get(size - newAnimationCount), "diameter", getEndSize());
        animator.setDuration(getAnimationDuration());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                CircleHolder tempCircle = (CircleHolder) ((ObjectAnimator) animation).getTarget();
                // Have to check first in case it was removed with reset()
                if (circles.contains(tempCircle)){
                    circles.remove(tempCircle);
                }
                animationCount--;
            }
        });
        animator.start();
        animationCount++;
    }
}