package com.example.sputnik.gesturecalc.util;

import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Observable;

/**
 * Created by Sputnik on 2/14/2018.
 */

public class PathActivator extends Observable {

    // Strike angle is 71/180*3.14 = 1.24f
    // Strike angle is 65/180*3.14 = 1.13f
    // Strike angle is 61/180*3.14 = 1.064f
    // Strike angle is 55/180*3.14 = 0.960
    private final float STRIKE_ANGLE = 1.24f;
    // Loop angle is 260/180*3.14 = 4.54
    // Loop first angle is 240/180*3.14 = 4.187
    // Loop first angle is 220/180*3.14 =
    private final float FIRST_LOOP_ANGLE = 3.84f;
    private final float ADDTL_LOOP_ANGLE = 6.28f;
    // Zig-zag angle is 100/180*3.14
    private final float ZIG_ZAG_ANGLE = 1.744f;
    private float MIN_ARC_LENGTH;
    private float MIN_LOOP_LENGTH;
    // Local loop angle is 60/180*3.14;
    private float MAX_LOCAL_LOOP_ANGLE = 1.047f;
    private float touchSlop = 16f;
    private boolean firstLoop;
    private double angleLoop;
    private float angleSumAbsValue;
    boolean firstClick;

    private float cuspAngleSum;
    private boolean possibleCusp;
    // Cusp start angle is 30/180*3.14
    // Cusp start angle is 60/180*3.14
    // Cusp start angle is 50/180*3.14
    private float CUSP_START_ANGLE = 0.872f;
    // Cust threshold angle is 150/180*3.14
    private float CUSP_THRESHOLD_ANGLE = 2.617f;
    private int CUSP_MAX_SEGEMENT_COUNT = 3;
    private int cuspSegmentCount;

    private float zzInterDist;
    private float ZZ_THRESHOLD_DIST = 80;
    private boolean zzBendCW, zzBendCCW;

    // Bend hysteresis angle is 5/180*3.14
    private float BEND_HYSTERESIS = 0.872f;
    // Bend threshold is 120/180*3.14;
    private float BEND_THRESHOLD = 2.093f;
    private float cwAngleSum;
    private float ccwAngleSum;
    private boolean bendCW, bendCCW;
    private float BEND_MAX_HYSTERESIS_ANGLE = 0.174f;
    private float cwHystSum;
    private float ccwHystSum;

    private boolean zzDominant, loopDominant;

    private float curX, curY, prevX, prevY;
    private float[] curVect = new float[2];
    private float[] prevVect = new float[2];
    private double curAngle, prevAngle;
    private boolean firstUpdate;
    private int actionType;
    private int prevActionType;
    private boolean inFirstButton;
    private Rect curRectView;
    private ViewBoundaryListener viewBoundaryListener;
    private PointF downPoint = new PointF();
    private ArrayList<Rect> noActivateRects = new ArrayList<>();
    private boolean invalidate;

    public interface ViewBoundaryListener{
        // Returns new ViewBounds
        Rect updateViewBounds(float x, float y);
    }

    public PathActivator() {
    }

    // Only holds one at a time
    public void registerViewBoundaryListener(ViewBoundaryListener viewBoundaryListener){
        this.viewBoundaryListener = viewBoundaryListener;
    }

    public void addNoActivateRect(Rect rect) {
        noActivateRects.add(rect);
    }

    private boolean inNoActivateRects(float x, float y){
        boolean result = false;
        for (Rect rect :
                noActivateRects) {
            if (rect.contains((int) x, (int) y)) {
                return true;
            }
        }
        return result;
    }

    public void unregisterViewBoundaryListener(ViewBoundaryListener viewBoundaryListener){
        this.viewBoundaryListener = null;
    }

    // Get new view bounds from ViewBoundaryListener that contains the current point
    private void getNewViewBounds(){
        Rect temp = viewBoundaryListener.updateViewBounds(curX, curY);
        curRectView = temp;
        // new view bounds so restart activations
        restart();
    }

    public float getTouchSlop() {
        return touchSlop;
    }

    public void setTouchSlop(float touchSlop) {
        this.touchSlop = touchSlop;
    }

    public void addEvent(MotionEvent event) {
        float evX = event.getX();
        float evY = event.getY();

        actionType = event.getAction();
        switch (actionType) {
            case MotionEvent.ACTION_DOWN:
                if (inNoActivateRects(evX, evY)){
                    invalidate = true;
                }
                inFirstButton = true;
                addPoint(evX, evY);
                downPoint.set(evX, evY);
                prevActionType = MotionEvent.ACTION_DOWN;
                break;
            case MotionEvent.ACTION_MOVE:
                if (invalidate){
                    return;
                }
                int count = event.getHistorySize();
                for (int i = 0; i < count; i++) {
                    processActionMove(event.getHistoricalX(i), event.getHistoricalY(i));
                }
                processActionMove(evX, evY);
                break;
            case MotionEvent.ACTION_UP:
                if (prevActionType == MotionEvent.ACTION_DOWN){
                    activate();
                } else if (!firstClick && !inFirstButton) {
                    activate();
                }
                reset();
                prevActionType = MotionEvent.ACTION_UP;
                invalidate = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                invalidate = false;
                break;
        }
    }

    private void processActionMove(float x, float y){
        if (euclidDistance(x, y, curX, curY) < touchSlop) {
            return;
        }
        addPoint(x, y);
        if (prevActionType == MotionEvent.ACTION_DOWN){
            activate(downPoint);
            firstClick = true;
            prevActionType = MotionEvent.ACTION_MOVE;
        }
    }

    // Adds a point to the current contour
    private void addPoint(float x, float y){
        if (!firstUpdate){
            firstUpdate = true;
            firstClick = true;
        }
        updateFields(x, y);
        // Not enough info yet
        if (prevVect[0] == 0 && prevVect[1] == 0){
            return;
        }
        if (isActivation()){
            setChanged();
            notifyObservers(new PointF(curX, curY));
        }
    }

    public static double euclidDistance(float x, float y, float prevX, float prevY){
        return Math.sqrt((x-prevX)*(x-prevX)+(y-prevY)*(y-prevY));
    }

    public static float scalarCrossProduct(float x1, float y1, float x2, float y2){
        return x1*y2 - y1*x2;
    }

    public static float dotProduct(float x1, float y1, float x2, float y2){
        return x1*x2 + y1*y2;
    }

    public static double angleBetweenVectors(float x1, float y1, float x2, float y2) {
        return Math.atan2(scalarCrossProduct(x1, y1, x2, y2), dotProduct(x1, y1, x2, y2));
    }

    private void activate(){
        setChanged();
        notifyObservers(new PointF(curX, curY));
    }

    private void activate(PointF point){
        setChanged();
        notifyObservers(point);
    }

    private void updateFields(float x, float y){
        prevX = curX;
        prevY = curY;
        curX = x;
        curY = y;
        prevVect[0] = curVect[0];
        prevVect[1] = curVect[1];
        if (prevX == 0 && prevY == 0){
            return;
        }
        curVect[0] = curX - prevX;
        curVect[1] = curY - prevY;
        prevAngle = curAngle;
        curAngle = angleBetweenVectors(curVect[0], curVect[1], prevVect[0], prevVect[1]);
        if (viewBoundaryListener != null) {
            if (curRectView == null || curRectView.isEmpty() || !curRectView.contains((int) curX, (int) curY)){
                getNewViewBounds();
            }
        }
    }

    private void resetFields(){
        prevX = 0;
        prevY = 0;
        curX = 0;
        curY = 0;
        prevVect[0] = 0;
        prevVect[1] = 0;
        curVect[0] = 0;
        curVect[1] = 0;
        prevAngle = 0;
        curAngle = 0;
        firstUpdate = false;
        inFirstButton = false;
        if (curRectView != null) {
            curRectView.setEmpty();
        }
    }

    public float getCurX(){
        return curX;
    }

    public float getCurY(){
        return curY;
    }

    private boolean isActivation(){
        boolean result = false;
        if (!firstClick){
            if(isFirstClick()){
                firstClick = true;
                result = true;
            }
        }

        if (isLoop() && !zzDominant){
            result = true;
            loopDominant = true;
            resetZigZag();
        }
        if (isZigZag() && !loopDominant){
            result = true;
            zzDominant = true;
            resetLoop();
        }

        return result;
    }

    // This should be called when starting a new gesture path
    private void reset(){
        restart();
        resetFields();
    }

    // This should be called when crossing a boundary
    // that requires the gesture trackers to start over
    // at the most previous point
    private void restart(){
        resetFirstClick();
        resetZigZag();
        resetLoop();
        loopDominant = false;
        zzDominant = false;
        firstClick = false;
        inFirstButton = false;
    }

    private boolean isCusp(){
        boolean result = false;

        // Store angle of segment just previous to one over the threshold
        // Once angle is reduced below threshold, check for a cusp
        // Approach is total continuous angles above a threshold local angle
        // (which ensures sharp angles) and once a local angle is below this
        // threshold, then check the accumulated angle value against a cusp
        // angle threshold. (Note that the angle must be in the same direction)

        // Validate angles are in same direction
        if ((curAngle > 0 && prevAngle < 0) || (curAngle < 0 && prevAngle > 0)){
            // Reset
            resetCusp();
            return false;
        }

        // Check to see if a cusp was just completed
        if (Math.abs(cuspAngleSum + curAngle) > CUSP_THRESHOLD_ANGLE){
            // Reset
            resetCusp();
            return true;
        }

        if (Math.abs(curAngle) < CUSP_START_ANGLE){
            // Not a cusp, but remember this angle for next time
            cuspAngleSum = (float) curAngle;
            cuspSegmentCount = 1;
            possibleCusp = false;
        } else {
            possibleCusp = true;
            cuspSegmentCount++;
            // Check if are too many segments (this helps trigger the cusp earlier)
            if (cuspSegmentCount > CUSP_MAX_SEGEMENT_COUNT){
                // Not a cusp, reset
                cuspAngleSum = 0;
                cuspSegmentCount = 1;
                possibleCusp = false;
            }

            // Angle is large, so add it to the local running total
            cuspAngleSum += curAngle;
        }
        return  result;
    }

    // Called when crossing a button boundary
    private void resetCusp(){
        cuspAngleSum = 0;
        cuspSegmentCount = 0;
        possibleCusp = false;
    }

    // Called when crossing button boundaries
    private void resetFirstClick(){
        angleSumAbsValue = 0;
        firstClick = false;
    }

    private boolean isFirstClick(){
        boolean result = false;
        angleSumAbsValue += Math.abs(curAngle);

        if (angleSumAbsValue > STRIKE_ANGLE && !firstClick){
            firstClick = true;
            result = true;
        }

        return result;
    }

    private boolean isLoop(){
        boolean result = false;

        if (isCusp()){
            angleLoop = 0;
            return false;
        }

        angleLoop += curAngle;

        // Check for possible cusp first
        if (possibleCusp){
            return false;
        }

        // Check for first loop
        if (Math.abs(angleLoop) > FIRST_LOOP_ANGLE && !firstLoop){
            firstLoop = true;
            angleLoop = 0;
            return true;
        }

        // Check for additional loops
        if (Math.abs(angleLoop) >= ADDTL_LOOP_ANGLE){
            angleLoop = 0;
            return true;
        }

        return result;
    }

    // Called when crossing button boundaries
    private void resetLoop(){
        angleLoop = 0;
        firstLoop = false;
        resetCusp();
    }

    // Called when crossing button boundaries OR
    // when a full zig-zag is activated (both angles)
    private void resetZigZag(){
        zzInterDist = 0;
        zzBendCCW = false;
        zzBendCW = false;
        resetCWBend();
        resetCCWBend();
    }

    private boolean isZigZag() {
        boolean result = false;

        // Update the bend functions only if needed
        if (!zzBendCW){
            zzBendCW = isCWBend();
        }
        if (!zzBendCCW){
            zzBendCCW = isCCWBend();
        }
        // Check for ZZag
        if (zzBendCW && zzBendCCW){
            if (zzInterDist > ZZ_THRESHOLD_DIST){
                result = true;
            } else {
                result = false;
                resetZigZag();
            }
            resetZigZag();
            return result;
        }

        // In between zig and zag
        if (zzBendCW || zzBendCCW){
            zzInterDist += euclidDistance(curY, curY, prevX, prevY);
        }

        return  result;

    }

    private void resetCWBend(){
        cwAngleSum = 0;
        bendCW = false;
        cwHystSum = 0;
    }

    private boolean isCWBend(){
        boolean result = false;

        if (bendCW){
            return true;
        }

        // If angle not within tolerance
        // For clarity, the evaluated expression below is broken up
        // into its two parts: direction (angle > 0) and tolerance
        // (angle > BEND_HYSTERESIS). This could be simplified to
        // angle > BEND_HYSTERESIS because a CW angle is negative.
        if (curAngle > 0){
            if (curAngle > BEND_HYSTERESIS || cwHystSum + curAngle > BEND_MAX_HYSTERESIS_ANGLE) {
                resetCWBend();
            } else {
                cwHystSum += curAngle;
                cwAngleSum += curAngle;
            }
        } else {
            if (Math.abs(cwAngleSum + curAngle) >= BEND_THRESHOLD){
                result = true;
                bendCW = true;
                // Reset
                resetCWBend();
            } else {
                cwAngleSum += curAngle;
            }
        }

        return result;
    }

    private void resetCCWBend(){
        ccwAngleSum = 0;
        bendCCW = false;
        ccwHystSum = 0;
    }

    private boolean isCCWBend(){
        boolean result = false;

        if (bendCCW){
            return true;
        }

        // If angle not within tolerance
        if (curAngle < 0){
            if (Math.abs(curAngle) > BEND_HYSTERESIS || Math.abs(ccwHystSum + curAngle) > BEND_MAX_HYSTERESIS_ANGLE) {
                resetCCWBend();
            } else {
                ccwHystSum += curAngle;
                ccwAngleSum += curAngle;
            }
        } else {
            if ((ccwAngleSum + curAngle - ccwHystSum) >= BEND_THRESHOLD){
                result = true;
                bendCCW = true;
                // Reset
                resetCCWBend();
            } else {
                ccwAngleSum += curAngle;
            }
        }

        return result;
    }
}
