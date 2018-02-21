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

/**
 * Created by Sputnik on 2/7/2018.
 */

public class CircleAnimator implements PathAnimator{

    private static float circleStartDiameter = 16f;
    private static float circleEndDiameter = 0f;
    private static float circleCenterSpacing = 16f;

    private static int opacity = 164;
    private static long animationDuration = 0;
    private float touchSlop = 16f;

    private Path path;
    private PathMeasure pathMeasure;
    private ArrayList<CircleHolder> circles = new ArrayList<>();
    private ArrayList<CircleHolder> circleSpecial = new ArrayList<>();
    private ArrayList<CircleHolder> circleSubset = new ArrayList<>();
    private Rect[] noDrawRects;
    private int newAnimationCount = 0;
    private int contourCount = 0;
    private float distToNextCircle;
    private float contourLength;
    private float[] circPos = new float[2];
    private int animationCount;
    private boolean drawingSubset;
    private float prevX, prevY;
    private int canvasWidth, canvasHeight;
    private boolean invalidate;
    private boolean newContourPrevAdded;

    class CircleHolder {
        private float x, y;
        private ShapeDrawable shape;

        CircleHolder(float diameter, float x, float y){
            OvalShape circle = new OvalShape();
            circle.resize(diameter, diameter);
            this.shape = new ShapeDrawable(circle);
            shape.getPaint().setColor(Color.parseColor("#a46fa7be"));
            shape.getPaint().setAlpha(CircleAnimator.opacity);
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

    public CircleAnimator(){
        path = new Path();
        pathMeasure = new PathMeasure();
    }    public void reDrawTo(int progress) {
        reDrawCirclesTo(progress);
        drawingSubset = true;
    }

    public float getCircleCenterSpacing(){
        return circleCenterSpacing;
    }    @Override
    public void setNoDrawRects(Rect... rects) {
        noDrawRects = rects;
    }

    public void setCircleCenterSpacing(float spacing){
        circleCenterSpacing = spacing;
    }    private boolean inNoDrawRects(float x, float y){
        boolean result = false;
        if (noDrawRects == null){
            return false;
        }
        for (Rect rect :
                noDrawRects) {
            if (rect.contains((int) x, (int) y)) {
                return true;
            }
        }
        return result;
    }

    private void createSpecialCircle(float x, float y){
        CircleHolder circle = new CircleHolder(circleStartDiameter, x - circleStartDiameter / 2, y - circleStartDiameter / 2);
        circleSpecial.add(circle);
        newAnimationCount++;
    }    public void setCanvasSize(int width, int height) {
        canvasWidth = width;
        canvasHeight = height;
    }

    private void reDrawCirclesTo(int progress) {
        circleSubset = (ArrayList<CircleHolder>) circles.clone();
        int count = (int) (((float)(100 - progress))/((float) 100)* circleSubset.size());
        for (int i = 0; i < count-1; i++) {
            circleSubset.remove(circleSubset.size()-1);
        }
    }

    public void addSpecialPoint(float x, float y){
        if (inNoDrawRects(x, y)){
            return;
        }
        createCircle(x,y);
        CircleHolder specialCircle = circles.get(circles.size() - 1);
        specialCircle.setColor(Color.RED);
        specialCircle.setDiameter(circleStartDiameter);
        addAnimators();
    }

    private void addPoint(float x, float y, boolean newContour) {
        if (newContour) {
            path.moveTo(x,y);
            distToNextCircle = circleCenterSpacing;
            contourLength = 0;
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
            contourLength = pathMeasure.getLength();
            prevX = x;
            prevY = y;
        }

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
                    if (PathActivator.euclidDistance(histX, histY, prevX, prevY) < touchSlop) {
                        break;
                    }
                    addPoint(histX, histY, false);
                }
                if (PathActivator.euclidDistance(evX, evY, prevX, prevY) < touchSlop) {
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

    private void addCircles(){
        float segmentLength = pathMeasure.getLength() - contourLength;
        if (segmentLength < distToNextCircle) {
            return;
        }
        float tempDist = distToNextCircle;
        // 1 is added to account for the segment being longer than distToNextCircle
        int circlesToAdd = 1 + (int) ((segmentLength - distToNextCircle) / circleCenterSpacing);
        for (int i = 0; i < circlesToAdd; i++) {
            pathMeasure.getPosTan(contourLength + tempDist, circPos, null);
            createCircle(circPos[0], circPos[1]);
            contourLength += tempDist;
            tempDist = circleCenterSpacing;
        }
        // addPoint private fields since added a segment
        distToNextCircle = circleCenterSpacing - ((segmentLength - distToNextCircle) % circleCenterSpacing);
    }

    private void createCircle(float x, float y) {
        CircleHolder circle = new CircleHolder(circleStartDiameter, x - circleStartDiameter / 2, y - circleStartDiameter / 2);
        circles.add(circle);
        newAnimationCount++;
    }



    public void clear(){
        clearCircles();
        path.rewind();
        path.moveTo(prevX, prevY);
        pathMeasure.setPath(path, false);
        contourCount = 0;
        drawingSubset = false;
        distToNextCircle = circleCenterSpacing;
        newContourPrevAdded = true;
    }

    private void clearCircles(){
        circles.clear();
        circleSubset.clear();
    }

    public boolean isRunning(){
        return animationCount != 0;
    }

    // add animators to any new trace points
    private void addAnimators() {
        // Setting for disabling animations
        if (animationDuration == 0){
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
        ObjectAnimator animator = ObjectAnimator.ofFloat(circles.get(size - newAnimationCount), "diameter", circleEndDiameter);
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
    }

    public void updateCanvas(Canvas canvas){
        drawCircles(canvas);
    }

    @Override
    public void recycle() {

    }

    @Override
    public int getCanvasWidth() {
        return canvasWidth;
    }

    @Override
    public int getCanvasHeight() {
        return canvasHeight;
    }

    private void drawCircles(Canvas canvas){
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



    public void setStartSize(float size){
        circleStartDiameter = size;
    }

    public void setEndSize(float size){
        circleEndDiameter = size;
    }



    public void setOpacity(int opacity){
        CircleAnimator.opacity = opacity;
    }

    public void setAnimationDuration(int duration){
        animationDuration = duration;
    }


    public float getStartSize(){
        return circleStartDiameter;
    }

    public float getEndSize(){
        return circleEndDiameter;
    }



    public int getOpacity(){
        return opacity;
    }

    public long getAnimationDuration(){
        return animationDuration;
    }

}