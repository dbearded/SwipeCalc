package com.example.sputnik.gesturecalc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import java.util.ArrayList;

/**
 * Created by Sputnik on 2/7/2018.
 */

class PathAnimator {

    private static final float TRACE_START_DIAMETER = 50f;
    private static final float TRACE_END_DIAMETER = 0f;
    private static final float TRACE_CENTER_DISTANCE = 150f;
    private static final long ANIMATION_DURATION = 5000;

    private Path currPath;
    private PathMeasure contourMeasure, segmentMeasure;
    private AnimatorSet animatorSet;
    private ArrayList<CircleHolder> tracePoints = new ArrayList<>();
//    private int newTraceCount = 0;
    private int contourCount = 0;
    private float distToNextTrace;
    private float segmentLength;
    private boolean contourAdded;

    PathAnimator(){
        currPath = new Path();
        contourMeasure = new PathMeasure();
        segmentMeasure = new PathMeasure();
        animatorSet = new AnimatorSet();
        for (int i = 0; i < 8; i++) {
            CircleHolder circle = new CircleHolder(TRACE_START_DIAMETER, i*150, i*150);
            tracePoints.add(circle);
        }
    }

    // adds segment to current contour
    // this is the preferred method because of speed
    void addSegment(Path segment) {
        segmentMeasure.setPath(segment, false);
        segmentLength = segmentMeasure.getLength();
        if (segmentLength > distToNextTrace) {
            addTracePoints();
            // update private fields
            currPath.addPath(segment);
        }
    }

    // New contour is created for every action down event
    // that isn't separated by a call to reset()
    // Method updates previous contour if needed and current path
    void addContour(Path contour) {
        contourCount++;
        currPath.addPath(contour);
        contourMeasure.setPath(currPath, false);
        for (int i = 1; i < contourCount; i++){
            contourMeasure.nextContour();
        }
        contourAdded = true;
        distToNextTrace = 0;
        addSegment(contour);
        contourAdded = false;
    }

    private void addTracePoints() {
        float[] pos = new float[2];
        float[] tan = new float[2];
        float tempDist = distToNextTrace;
//        int tracesToAdd = (int) ((segmentLength - distToNextTrace) / TRACE_CENTER_DISTANCE);
        int tracesToAdd = 1;
        int newTraceCount = 0;
        while (newTraceCount < tracesToAdd){
            segmentMeasure.getPosTan(tempDist, pos, tan);
            CircleHolder circle = new CircleHolder(TRACE_START_DIAMETER, pos[0], pos[1]);
            tracePoints.add(circle);
            // Because tempDist is something else to begin with
            tempDist = TRACE_CENTER_DISTANCE;
            newTraceCount++;
        }
        distToNextTrace = TRACE_CENTER_DISTANCE - ((segmentLength - distToNextTrace) % TRACE_CENTER_DISTANCE);
//        addAnimators();
    }

    void reset() {
        tracePoints.clear();
        contourCount = 0;
        distToNextTrace = TRACE_CENTER_DISTANCE;
    }

    boolean isRunning(){
        return animatorSet.isRunning();
    }

    // add animators to any new trace points
    /*private void addAnimators() {
        int size = tracePoints.size();
        int initialCount = newTraceCount;
        AnimatorSet.Builder builder = null;
        while(newTraceCount > 0){
            ObjectAnimator animator = ObjectAnimator.ofFloat(tracePoints.get(size-1-newTraceCount), "diameter", TRACE_END_DIAMETER);
            animator.setDuration(ANIMATION_DURATION);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    CircleHolder tempCircle = (CircleHolder) ((ObjectAnimator) animation).getTarget();
                    // Have to check first in case it was removed with reset()
                    if (tracePoints.contains(tempCircle)){
                        tracePoints.remove(tempCircle);
                    }
                }
            });
            if (newTraceCount == initialCount){
                builder = animatorSet.play(animator);
            } else {
                builder.with(animator);
            }
            newTraceCount--;
        }
        // Now less than zero. Need to reset
        newTraceCount = 0;
        if (!animatorSet.isStarted()) {
            animatorSet.start();
        }
    }*/

    void updateCanvas(Canvas canvas){
        for (int i = 0; i < tracePoints.size(); i++) {
            CircleHolder circle = tracePoints.get(i);
            canvas.save();
            canvas.translate(circle.getX(), circle.getY());
            circle.getShape().draw(canvas);
            canvas.restore();
        }
    }

    private class CircleHolder {
        private float x, y, diameter;
        private ShapeDrawable shape;

        CircleHolder(float diameter, float x, float y){
            OvalShape circle = new OvalShape();
            circle.resize(diameter, diameter);
            this.shape = new ShapeDrawable(circle);
            shape.getPaint().setColor(Color.RED);
            this.x = x;
            this.y = y;
            this.diameter = diameter;
        }

        float getX() {
            return x;
        }

        float getY() {
            return y;
        }

        ShapeDrawable getShape() {
            return shape;
        }

        float getDiameter() {
            return diameter;
        }

        void setDiameter(float diameter) {
            this.diameter = diameter;
            shape.getShape().resize(diameter, diameter);
        }

        void setX(float x) {
            this.x = x;
        }

        void setY(float y) {
            this.y = y;
        }

        void setColor(int color){
            shape.getPaint().setColor(color);
        }
    }
}