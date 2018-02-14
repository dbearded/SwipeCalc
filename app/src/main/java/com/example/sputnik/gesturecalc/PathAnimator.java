package com.example.sputnik.gesturecalc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

    private static float circleStartDiameter = 16f;
    private static float circleEndDiameter = 0f;
    private static float circleCenterSpacing = 16f;
    private static long animationDuration = 0;
    private static int opacity = 164;

    private Path path;
    private PathMeasure pathMeasure;
    private ArrayList<CircleHolder> circles = new ArrayList<>();
    private ArrayList<CircleHolder> circleSubset = new ArrayList<>();
    private int newCircleCount = 0;
    private int contourCount = 0;
    private float distToNextTrace;
    private float contourLength;
    private float[] pos = new float[2];
    private float[] tan = new float[2];
    private int animationCount;
    private boolean drawingSubset = false;

    public PathAnimator(){
        path = new Path();
        pathMeasure = new PathMeasure();
    }

    public void reDrawTo(int progress) {
        circleSubset = (ArrayList<CircleHolder>) circles.clone();
        int count = (int) (((float)(100 - progress))/((float) 100)* circleSubset.size());
        for (int i = 0; i < count-1; i++) {
            circleSubset.remove(circleSubset.size()-1);
        }
        drawingSubset = true;
    }

    void addSpecial(float x, float y){
        addCircle(x,y);
        CircleHolder specialCircle = circles.get(circles.size() - 1);
        specialCircle.setColor(Color.RED);
        specialCircle.setDiameter(18f);
        addAnimators();
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
            distToNextTrace = circleCenterSpacing;
            contourLength = 0;
            addAnimators();
        }

        float segmentLength = pathMeasure.getLength() - contourLength;
        if (segmentLength < distToNextTrace){
            return;
        }
        float tempDist = distToNextTrace;
        // 1 is added to account for the segment being longer than distToNextTrace
        int circlesToAdd = 1 + (int) ((segmentLength - distToNextTrace) / circleCenterSpacing);
        for (int i = 0; i < circlesToAdd; i++) {
            pathMeasure.getPosTan(contourLength + tempDist, pos, tan);
            addCircle(pos[0], pos[1]);
            contourLength += tempDist;
            tempDist = circleCenterSpacing;
        }
        addAnimators();
        // update private fields since added a segment
        distToNextTrace = circleCenterSpacing - ((segmentLength - distToNextTrace) % circleCenterSpacing);
        path.reset();
        path.addPath(newPath);
        pathMeasure.setPath(path, false);
        for (int i = 0; i < contourCount; i++){
            pathMeasure.nextContour();
        }
        contourLength = pathMeasure.getLength();
    }

    private void addCircle(float x, float y) {
        CircleHolder circle = new CircleHolder(circleStartDiameter, x - circleStartDiameter / 2, y - circleStartDiameter / 2);
        circles.add(circle);
        newCircleCount++;
    }

    void reset() {
        circles.clear();
        circleSubset.clear();
        drawingSubset = false;
        contourCount = 0;
        distToNextTrace = circleCenterSpacing;
        path.reset();
        pathMeasure.setPath(path, false);
    }

    boolean isRunning(){
        return animationCount != 0;
    }

    // add animators to any new trace points
    private void addAnimators() {
        // Setting for disabling animations
        if (animationDuration == 0){
            newCircleCount = 0;
            return;
        }
        int size = circles.size();
        while(newCircleCount > 0){
            ObjectAnimator animator = ObjectAnimator.ofFloat(circles.get(size - newCircleCount), "diameter", circleEndDiameter);
            animator.setDuration(animationDuration);
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
        ArrayList<CircleHolder> tempCircles;
        if (drawingSubset) {
            tempCircles = circleSubset;
            drawingSubset = false;
        } else {
            tempCircles = circles;
        }
        for (int i = 0; i < tempCircles.size(); i++) {
            CircleHolder circle = tempCircles.get(i);
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
            shape.getPaint().setColor(Color.parseColor("#a46fa7be"));
            shape.getPaint().setAlpha(PathAnimator.opacity);
            this.x = x;
            this.y = y;
            this.diameter = diameter;
            this.opacity = PathAnimator.opacity;
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

    public void setCircleStartDiameter(float diameter){
        circleStartDiameter = diameter;
    }

    public void setCircleEndDiameter(float diameter){
        circleEndDiameter = diameter;
    }

    public void setCircleCenterSpacing(float spacing){
        circleCenterSpacing = spacing;
    }

    public void setOpacity(int opacity){
        PathAnimator.opacity = opacity;
    }

    public void setAnimationDuration(int duration){
        animationDuration = duration;
    }

    public float getCircleStartDiameter(){
        return circleStartDiameter;
    }

    public float getCircleEndDiameter(){
        return circleEndDiameter;
    }

    public float getCircleCenterSpacing(){
        return circleCenterSpacing;
    }

    public int getOpacity(){
        return opacity;
    }

    public long getANIMATION_DURATION(){
        return animationDuration;
    }
}