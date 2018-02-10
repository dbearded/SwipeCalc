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

public class PathAnimator {

    private static float CIRCLE_START_DIAMETER = 25f;
    private static float CIRCLE_END_DIAMETER = 0f;
    private static float CIRCLE_CENTER_DISTANCE = 50f;
    private static long ANIMATION_DURATION = 1000;
    private static int OPACITY = 255;

    private Path path;
    private PathMeasure pathMeasure;
    private AnimatorSet animatorSet;
    private ArrayList<CircleHolder> circles = new ArrayList<>();
    private int newCircleCount = 0;
    private int contourCount = 0;
    private float distToNextTrace;
    private float contourLength;
    private float[] pos = new float[2];
    private float[] tan = new float[2];
    private int animationCount;

    public PathAnimator(){
        path = new Path();
        pathMeasure = new PathMeasure();
        animatorSet = new AnimatorSet();
        /*for (int i = 0; i < 12; i++) {
            CircleHolder circle = new CircleHolder(CIRCLE_START_DIAMETER, i*50, i*50);
            circles.add(circle);
        }*/
    }

    // updates the current contour by replacing it with the parameter
    void updatePath(Path newPath, boolean newContour) {
        contourCount += newContour ? 1 : 0;
        pathMeasure.setPath(newPath, false);
        for (int i = 0; i < contourCount; i++) {
            pathMeasure.nextContour();
        }

        if (newContour){
            pathMeasure.getPosTan(0f, pos, tan);
            addCircle(pos[0], pos[1]);
            // Add any additional points in contour
            distToNextTrace = CIRCLE_CENTER_DISTANCE;
            contourLength = 0;
            addAnimators();
        }

        float segmentLength = pathMeasure.getLength() - contourLength;
        if (segmentLength < distToNextTrace){
            return;
        }
        float tempDist = distToNextTrace;
        // 1 is added to account for the segment being longer than distToNextTrace
        int circlesToAdd = 1 + (int) ((segmentLength - distToNextTrace) / CIRCLE_CENTER_DISTANCE);
        for (int i = 0; i < circlesToAdd; i++) {
            pathMeasure.getPosTan(contourLength +(i+1)*tempDist, pos, tan);
            addCircle(pos[0], pos[1]);
            // Because tempDist is something else to begin with
            tempDist = CIRCLE_CENTER_DISTANCE;
        }
        addAnimators();
        // update private fields since added a segment
        distToNextTrace = CIRCLE_CENTER_DISTANCE - ((segmentLength - distToNextTrace) % CIRCLE_CENTER_DISTANCE);
        path.reset();
        path.addPath(newPath);
        pathMeasure.setPath(path, false);
        for (int i = 0; i < contourCount; i++){
            pathMeasure.nextContour();
        }
        contourLength = pathMeasure.getLength();
    }

    private void addCircle(float x, float y) {
        CircleHolder circle = new CircleHolder(CIRCLE_START_DIAMETER, x - CIRCLE_START_DIAMETER / 2, y - CIRCLE_START_DIAMETER / 2);
        circles.add(circle);
        newCircleCount++;
    }

    void reset() {
        circles.clear();
        contourCount = 0;
        distToNextTrace = CIRCLE_CENTER_DISTANCE;
        path.reset();
        pathMeasure.setPath(path, false);
    }

    boolean isRunning(){
        return animationCount != 0;
    }

    // add animators to any new trace points
    private void addAnimators() {
        int size = circles.size();
        while(newCircleCount > 0){
            ObjectAnimator animator = ObjectAnimator.ofFloat(circles.get(size - newCircleCount), "diameter", CIRCLE_END_DIAMETER);
            animator.setDuration(ANIMATION_DURATION);
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
            newCircleCount--;
        }
        // Now count is less than zero. Need to reset
        newCircleCount = 0;
    }

    void updateCanvas(Canvas canvas){
        for (int i = 0; i < circles.size(); i++) {
            CircleHolder circle = circles.get(i);
            canvas.save();
            canvas.translate(circle.getX(), circle.getY());
            circle.getShape().draw(canvas);
            canvas.restore();
        }
    }

    class CircleHolder {
        private float x, y, diameter;
        private ShapeDrawable shape;
        private int opacity;

        CircleHolder(float diameter, float x, float y){
            OvalShape circle = new OvalShape();
            circle.resize(diameter, diameter);
            this.shape = new ShapeDrawable(circle);
            shape.getPaint().setColor(Color.RED);
            shape.getPaint().setAlpha(OPACITY);
            this.x = x;
            this.y = y;
            this.diameter = diameter;
            this.opacity = OPACITY;
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

        int getOpacity(){
            return opacity;
        }

        void setDiameter(float diameter) {
            this.diameter = diameter;
            shape.getShape().resize(diameter, diameter);
        }

        void setX(float x) {
            this.x = x - diameter / 2;
        }

        void setY(float y) {
            this.y = y - diameter / 2;
        }

        void setOpactiy(int a){
            this.opacity = a;
        }

        void setColor(int color){
            shape.getPaint().setColor(color);
        }
    }

    public void setCIRCLE_START_DIAMETER(float diameter){
        CIRCLE_START_DIAMETER = diameter;
    }

    public void setCIRCLE_END_DIAMETER(float diameter){
        CIRCLE_END_DIAMETER = diameter;
    }

    public void setCIRCLE_CENTER_DISTANCE(float distance){
        CIRCLE_CENTER_DISTANCE = distance;
    }

    public void setOPACITY(int opacity){
        OPACITY = opacity;
    }

    public void setANIMATION_DURATION(int duration){
        ANIMATION_DURATION = duration;
    }

    public float getCIRCLE_START_DIAMETER(){
        return CIRCLE_START_DIAMETER;
    }

    public float getCIRCLE_END_DIAMETER(){
        return CIRCLE_END_DIAMETER;
    }

    public float getCIRCLE_CENTER_DISTANCE(){
        return CIRCLE_CENTER_DISTANCE;
    }

    public int getOPACITY(){
        return OPACITY;
    }

    public long getANIMATION_DURATION(){
        return ANIMATION_DURATION;
    }
}