package com.example.sputnik.gesturecalc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sputnik on 2/1/2018.
 */
public class MyLayout extends android.support.v7.widget.GridLayout {
    private final float TOUCH_SLOP = 16f;
    private final float STROKE_WDITH = 18f;
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
    private boolean firstLoop;
    private double angleLoop;
    private double prevAngle;
    private float angleSumAbsValue;
    boolean firstClick = false;

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

    private List<ButtonListener> buttonListeners = new ArrayList<ButtonListener>();
    private ButtonTextView clearButton;
    private Path path;
    private PathAnimator animator;
    private Rect offsetViewBounds = new Rect();
    private float[] prevPoint = new float[2];
    private float[] prevVector = new float[2];
    private float[] currVector = new float[2];
    private float PX_PER_COL;
    private float PX_PER_ROW;
    private int[] buttonBelowIndex = new int[2];
    // [0] = top left x-coord, [1] = top left y-coord,
    // [2] = top right x-coord, [4] = bottom left y-coord
    private int[] clearButtonBoundary = new int[4];
    boolean prevEventDown = false;

    Path path3 = new Path();

    interface ButtonListener {
        void buttonPressed(String input);
    }

    public MyLayout(Context context) {
        super(context);
        setup();
    }

    public MyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public MyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public void setPathAnimator(PathAnimator animator){
        this.animator = animator;
    }

    void setup(){
        path = new Path();
        this.setWillNotDraw(false);
    }

    void setupSize(){
        PX_PER_COL = (float) getWidth() / (float) getColumnCount();
        PX_PER_ROW = (float) getHeight() / (float) getRowCount();

        final View clear = findViewById(R.id.clear);
        ViewTreeObserver viewTreeObserver = clear.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16) {
                        clear.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        clear.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    setResetButton(clear);
                }
            });
        }

        MIN_ARC_LENGTH = ((PX_PER_COL + PX_PER_ROW)/2)/4;
        MIN_LOOP_LENGTH = MIN_ARC_LENGTH * 4;
    }

    int getColIndexOfLocation(float x){
        return ((int) (x) / ((int) PX_PER_COL));
    }

    int getRowIndexOfLocation(float y){
        return ((int) (y) / ((int) PX_PER_ROW));
    }

    void clearPath(){
        path.reset();
        animator.reset();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (animator != null) {
            animator.updateCanvas(canvas);
            if (animator.isRunning()){
                invalidate();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setResetButton(View clear){
        if (!isInEditMode()) {
            this.clearButton = (ButtonTextView) clear;
            int[] layoutPos = new int[2];
            getLocationInWindow(layoutPos);
            int[] clearPos = new int[2];
            clearButton.getLocationInWindow(clearPos);
            clearButtonBoundary[0] = layoutPos[0] - clearPos[0];
            clearButtonBoundary[1] = layoutPos[1] - clearPos[1];
            clearButtonBoundary[2] = clearButtonBoundary[0] + clear.getWidth();
            clearButtonBoundary[3] = clearButtonBoundary[1] + clear.getHeight();
        }
    }

    private void resetOnDownAction(float x, float y){
        prevPoint[0] = x;
        prevPoint[1] = y;
        prevVector[0] = 0;
        prevVector[1] = 0;
        currVector[0] = 0;
        currVector[1] = 0;
        buttonBelowIndex[0] = getColIndexOfLocation(x);
        buttonBelowIndex[1] = getRowIndexOfLocation(y);
        prevEventDown = true;
    }

    private void clickChildButton(float x, float y) {
        TextView child;
        Rect offsetViewBounds = new Rect();
        int count = getChildCount();
        for (int i =0; i < count; i++) {
            child = (TextView) getChildAt(i);
            child.getDrawingRect(offsetViewBounds);
            offsetDescendantRectToMyCoords(child, offsetViewBounds);
            if (offsetViewBounds.contains((int) x,(int) y)){
                child.callOnClick();
                for (ButtonListener listener :
                        buttonListeners) {
                    listener.buttonPressed(String.valueOf(child.getText()));
                }
                return;
            }
            offsetViewBounds.setEmpty();
        }
    }

    void registerButtonListener(ButtonListener listener){
        buttonListeners.add(listener);
    }

    void unregisterButtonListener(ButtonListener listener){
        buttonListeners.remove(listener);
    }

    private void processMoveAction(float x, float y) {
        if (getChildAt(x - STROKE_WDITH/2, y - STROKE_WDITH/2).equals(clearButton)) {
            return;
        }
        float dx = x - prevPoint[0];
        float dy = y - prevPoint[1];
        // Ignore event if it's too close to previous
        if (Math.abs(dx) <= TOUCH_SLOP && Math.abs(dy) <= TOUCH_SLOP) {
            return;
        }
        // If the event is over the clear button
        if (getChildAt(x - STROKE_WDITH / 2, y - STROKE_WDITH / 2).equals(clearButton)) {
            return;
        }
        // If the event is over a new button
        if (buttonBelowIndex[0] != getColIndexOfLocation(x) || buttonBelowIndex[1] != getRowIndexOfLocation(y)) {
            resetOnButtonChange(x, y);
        }
        if (prevEventDown) {
            clickChildButton(x, y);
            firstClick = !firstClick;
            prevEventDown = false;
        }
        path.lineTo(x, y);
        animator.updatePath(path, false);
        // If there aren't enough events to calculate angleSumAbsValue
        if (prevVector[0] == 0 && prevVector[1] == 0) {
            prevVector[0] = x - prevPoint[0];
            prevVector[1] = y - prevPoint[1];
            return;
        }
        currVector[0] = x - prevPoint[0];
        currVector[1] = y - prevPoint[1];
        float dot = currVector[0] * prevVector[0] + currVector[1] * prevVector[1];
        float det = currVector[0] * prevVector[1] - currVector[1] * prevVector[0];
        double angleLocal = Math.atan2(det, dot);
        if (isActivation(x,y,prevPoint[0],prevPoint[1],angleLocal, prevAngle)){
            clickChildButton(x,y);
            animator.addSpecial(x,y);
        }
        prevVector[0] = currVector[0];
        prevVector[1] = currVector[1];
        prevPoint[0] = x;
        prevPoint[1] = y;
        prevAngle = angleLocal;
    }

    private double euclidDistance(float x, float y, float prevX, float prevY){
        return Math.sqrt((x-prevX)*(x-prevX)+(y-prevY)*(y-prevY));
    }

    private void resetActivation(){
        resetFirstClick();
        resetZigZag();
        resetLoop();
        loopDominant = false;
        zzDominant = false;
    }

    private boolean isActivation(float x, float y, float prevX, float prevY, double angle, double prevAng){
        boolean result = false;
        if (!firstClick){
            if(isFirstClick(x, y, prevX, prevY, angle, prevAng)){
                firstClick = true;
                result = true;
            }
        }

        if (isLoop(x, y, prevX, prevY, angle, prevAng) && !zzDominant){
            result = true;
            loopDominant = true;
            resetZigZag();
        }
        if (isZigZag(x, y, prevX, prevY, angle, prevAng) && !loopDominant){
            result = true;
            zzDominant = true;
            resetLoop();
        }

        return result;
    }

    private boolean isCusp(float x, float y, float prevX, float prevY, double angle, double prevAng){
        boolean result = false;

        // Store angle of segment just previous to one over the threshold
        // Once angle is reduced below threshold, check for a cusp
        // Approach is total continuous angles above a threshold local angle
        // (which ensures sharp angles) and once a local angle is below this
        // threshold, then check the accumulated angle value against a cusp
        // angle threshold. (Note that the angle must be in the same direction)

        // Validate angles are in same direction
        if ((angle > 0 && prevAng < 0) || (angle < 0 && prevAng > 0)){
            // Reset
            resetCusp();
            return false;
        }

        // Check to see if a cusp was just completed
        if (Math.abs(cuspAngleSum + angle) > CUSP_THRESHOLD_ANGLE){
            // Reset
            resetCusp();
            return true;
        }

        if (Math.abs(angle) < CUSP_START_ANGLE){
            // Not a cusp, but remember this angle for next time
            cuspAngleSum = (float) angle;
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
            cuspAngleSum += angle;
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

    private boolean isFirstClick(float x, float y, float prevX, float prevY, double angle, double prevAng){
        boolean result = false;
        angleSumAbsValue += Math.abs(angle);

        if (angleSumAbsValue > STRIKE_ANGLE && !firstClick){
            firstClick = true;
            result = true;
        }

        return result;
    }

    private boolean isLoop(float x, float y, float prevX, float prevY, double angle, double prevAng){
        boolean result = false;

        if (isCusp(x,y,prevX, prevY, angle, prevAng)){
            angleLoop = 0;
            return false;
        }

        angleLoop += angle;

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

    private boolean isZigZag(float x, float y, float prevX, float prevY, double angle, double prevAng) {
        boolean result = false;

        // Update the bend functions only if needed
        if (!zzBendCW){
            zzBendCW = isCWBend(x, y, prevX, prevY, (float) angle, (float) prevAng);
        }
        if (!zzBendCCW){
            zzBendCCW = isCCWBend(x, y, prevX, prevY, (float) angle, (float) prevAng);
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
            zzInterDist += euclidDistance(x, y, prevX, prevY);
        }

        return  result;

    }

    private void resetCWBend(){
        cwAngleSum = 0;
        bendCW = false;
        cwHystSum = 0;
    }

    private boolean isCWBend(float x, float y, float prevX, float prevY, float angle, float prevAng){
        boolean result = false;

        if (bendCW){
            return true;
        }

        // If angle not within tolerance
        // For clarity, the evaluated expression below is broken up
        // into its two parts: direction (angle > 0) and tolerance
        // (angle > BEND_HYSTERESIS). This could be simplified to
        // angle > BEND_HYSTERESIS because a CW angle is negative.
        if (angle > 0){
            if (angle > BEND_HYSTERESIS || cwHystSum + angle > BEND_MAX_HYSTERESIS_ANGLE) {
                resetCWBend();
            } else {
                cwHystSum += angle;
                cwAngleSum += angle;
            }
        } else {
            if (Math.abs(cwAngleSum + angle) >= BEND_THRESHOLD){
                result = true;
                bendCW = true;
                // Reset
                resetCWBend();
            } else {
                cwAngleSum += angle;
            }
        }

        return result;
    }

    private void resetCCWBend(){
        ccwAngleSum = 0;
        bendCCW = false;
        ccwHystSum = 0;
    }

    private boolean isCCWBend(float x, float y, float prevX, float prevY, float angle, float prevAng){
        boolean result = false;

        if (bendCCW){
            return true;
        }

        // If angle not within tolerance
        if (angle < 0){
            if (Math.abs(angle) > BEND_HYSTERESIS || Math.abs(ccwHystSum + angle) > BEND_MAX_HYSTERESIS_ANGLE) {
                resetCCWBend();
            } else {
                ccwHystSum += angle;
                ccwAngleSum += angle;
            }
        } else {
            if ((ccwAngleSum + angle - ccwHystSum) >= BEND_THRESHOLD){
                result = true;
                bendCCW = true;
                // Reset
                resetCCWBend();
            } else {
                ccwAngleSum += angle;
            }
        }

        return result;
    }

    private void resetOnButtonChange(float x , float y){
        resetActivation();
        buttonBelowIndex[0] = getColIndexOfLocation(x);
        buttonBelowIndex[1] = getRowIndexOfLocation(y);
        prevPoint[0] = x;
        prevPoint[1] = y;
        prevVector[0] = 0;
        prevVector[1] = 0;
    }

    private ButtonTextView getChildAt(float x, float y){
        ButtonTextView button = null;
        int count = getChildCount();
        for (int i =0; i < count; i++) {
            button = (ButtonTextView) getChildAt(i);
            button.getDrawingRect(offsetViewBounds);
            offsetDescendantRectToMyCoords(button, offsetViewBounds);
            if (offsetViewBounds.contains((int) x,(int) y)){
                return button;
            }
        }
        return button;
    }

    private void notifyButtonListeners(ButtonTextView button){
        for (ButtonListener listener :
                buttonListeners) {
            listener.buttonPressed(String.valueOf(button.getText()));
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float eventX = ev.getX();
        final float eventY = ev.getY();
        // Event is on clear button
        if (getChildAt(eventX - STROKE_WDITH/2, eventY - STROKE_WDITH/2).equals(clearButton)){
            // clear any history
            final int count = ev.getHistorySize();
            for (int i = 0; i < count; i++) {
                ev.getHistoricalX(i);
            }
            clearPath();
            notifyButtonListeners(clearButton);
            this.invalidate();
            return false;
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                // Move a negligible amount for PathMeasure functions to work
                path.lineTo(eventX+0.01f, eventY+0.01f);
                path3.moveTo(eventX, eventY);
                path3.lineTo(eventX+1, eventY+1);
                animator.updatePath(path, true);
                animator.addSpecial(eventX, eventY);
                resetOnDownAction(eventX, eventY);
                break;
            case MotionEvent.ACTION_MOVE:
                final int history = ev.getHistorySize();
                for (int i = 0; i < history; i++){
                    path3.lineTo(ev.getHistoricalX(i), ev.getHistoricalY(i));
                    processMoveAction(ev.getHistoricalX(i), ev.getHistoricalY(i));
                }
                processMoveAction(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                if (!firstClick) {
                    animator.addSpecial(eventX, eventY);
                    clickChildButton(eventX, eventY);
                }
                resetActivation();
                /*PathMeasure pathMeasure = new PathMeasure(path, false);
                System.out.println("Contour is closed? " + pathMeasure.isClosed());
                Region region = new Region();
                RectF rectF = new RectF();
                path.computeBounds(rectF, true);
                Region clip = new Region((int) rectF.left, (int) rectF.top, (int) rectF.right,(int) rectF.bottom);
                region.setPath(path, clip);
                System.out.println("Region is complex: " + Boolean.toString(region.isComplex()));
                Path path2 = new Path();
                path2.moveTo(0,0);
                path2.lineTo(10,10);
                Region clip2 = new Region(0, 0, 10, 10);
                region.setPath(path2, clip2);
                System.out.println("Region2 is complex: " + Boolean.toString(region.isComplex()));
                path3.computeBounds(rectF, true);
                clip = new Region((int) rectF.left, (int) rectF.top, (int) rectF.right,(int) rectF.bottom);
                region.setPath(path, clip);
                System.out.println("Region is complex: " + Boolean.toString(region.isComplex()));
                RegionIterator regionIterator = new RegionIterator(region);
                Rect rect = new Rect();
                while (regionIterator.next(rect)){
                    System.out.println(rect.left + " "+ rect.top + " " + rect.right + " " + rect.bottom);
                }
                path3.rewind();

                regionIterator = new RegionIterator(region);
                rect = new Rect();
                while (regionIterator.next(rect)){
                    region.op(rect, region, Region.Op.UNION);
                }

                System.out.println("New Region rectangle");

                rect = new Rect();
                regionIterator = new RegionIterator(region);
                while (regionIterator.next(rect)){
                    System.out.println(rect.left + " "+ rect.top + " " + rect.right + " " + rect.bottom);
                }*/
        }
        this.invalidate();
        return false;
    }
}