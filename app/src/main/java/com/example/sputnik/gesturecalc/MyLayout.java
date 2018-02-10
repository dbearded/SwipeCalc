package com.example.sputnik.gesturecalc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Sputnik on 2/1/2018.
 */
public class MyLayout extends android.support.v7.widget.GridLayout {
    private final float TOUCH_SLOP = 16f;
    private final float STROKE_WDITH = 18f;
    // Envisioned for animation
    private final float MIN_EVENT_DIST = 16f;
    // Strike angle is 71/180*3.14 = 1.24f
    // Strike angle is 65/180*3.14 = 1.13f
    // Strike angle is 61/180*3.14 = 1.064f
    // Strike angle is 55/180*3.14 = 0.960
    private final float STRIKE_ANGLE = 1.24f;
    // Loop angle is 260/180*3.14
    private final float LOOP_ANGLE = 4.54f;
    private List<ButtonListener> buttonListeners = new ArrayList<ButtonListener>();
    private ButtonTextView clearButton;
    private Path entirePath = new Path();
    private Path path;
    private List<Path> paths = new LinkedList<>();
//    private List<Integer> procs = new ArrayList<>();
    private Paint paint;
    private Paint clickedPaint;
    private PathAnimator animator;
    private Rect offsetViewBounds = new Rect();
    private float[] downPoint = new float[2];
    private float[] prevPoint = new float[2];
    private float[] prevVector = new float[2];
    private float[] currVector = new float[2];
    private float angleSum;
    private float WIDTH, HEIGHT, COLUMN_COUNT, ROW_COUNT, PX_PER_COL, PX_PER_ROW;
    private int[] buttonBelowIndex = new int[2];
    // [0] = top left x-coord, [1] = top left y-coord,
    // [2] = top right x-coord, [4] = bottom left y-coord
    private int[] clearButtonBoundary = new int[4];
    private int loopNum = 0;
    boolean firstClick = false;
    boolean prevEventDown = false;

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
        setupPaint();
        setupPath();
        this.setWillNotDraw(false);
//        animator = new PathAnimator();
    }

    void setupPaint(){
//            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint = new Paint();
        paint.setDither(true);
        paint.setColor(getResources().getColor(R.color.swipe));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(16);
        setupClickedPaint();
//            this.setBackgroundColor(Color.TRANSPARENT);
    }

    void setupClickedPaint(){
        clickedPaint = new Paint();
        clickedPaint.setDither(true);
        clickedPaint.setColor(Color.RED);
        clickedPaint.setStyle(Paint.Style.STROKE);
        clickedPaint.setStrokeJoin(Paint.Join.ROUND);
        clickedPaint.setStrokeCap(Paint.Cap.ROUND);
        clickedPaint.setStrokeWidth(STROKE_WDITH);
    }

    void setupPath(){
        path = new Path();
    }

    void setupSize(){
        WIDTH = getWidth();
        HEIGHT = getHeight();
        COLUMN_COUNT = getColumnCount();
        ROW_COUNT = getRowCount();
        PX_PER_COL = WIDTH / COLUMN_COUNT;
        PX_PER_ROW = HEIGHT / ROW_COUNT;

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
    }

    int getColIndexOfLocation(float x){
        return ((int) (x) / ((int) PX_PER_COL));
    }

    int getRowIndexOfLocation(float y){
        return ((int) (y) / ((int) PX_PER_ROW));
    }

    /* // old method for use with pre-first animation
    void clearPath(){
        entirePath.reset();
        path.reset();
        for (Path path :
                paths) {
            path.reset();
        }
        paths.clear();
        procs.clear();
        this.invalidate();
    }*/

    void clearPath(){
        entirePath.reset();
        path.reset();
        /*for (Path path :
                paths) {
            path.reset();
        }*/
        paths.clear();
        animator.reset();
    }

    /* old method for use with pre-first animation
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        final int size = paths.size();
        int j = 0;
        final int sizeProcs = procs.size();
        for (int i = 0; i < size; i++) {
            if (!procs.isEmpty() && i == procs.get(j)) {
                canvas.drawPath(paths.get(i), clickedPaint);
                j += (j + 1) < sizeProcs ? 1 : 0;
            } else {
                canvas.drawPath(paths.get(i), paint);
            }
        }
    }*/

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (animator != null) {
            animator.updateCanvas(canvas);
        /*int count = paths.size();
        for (int i = 0; i < count; i++) {
            canvas.drawPath(paths.get(i), paint);
        }*/
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

    /* // Old code for first path animation
    private void processMoveAction(float x, float y){
        float dx = x - prevPoint[0];
        float dy = y - prevPoint[1];
        // Ignore event if it's too close to previous
        if (Math.abs(dx) <= TOUCH_SLOP && Math.abs(dy) <= TOUCH_SLOP){
            return;
        }
        // If the event is over the clear button
        if (getChildAt(x - STROKE_WDITH/2, y - STROKE_WDITH/2).equals(clearButton)){
            return;
        }
        // If the event is over a new button
        if (buttonBelowIndex[0] != getColIndexOfLocation(x) || buttonBelowIndex[1] != getRowIndexOfLocation(y)){
            resetOnButtonChange(x, y);
        }
        if (prevEventDown){
            procs.add(paths.size()-1);
            clickChildButton(x, y);
            firstClick = !firstClick;
            prevEventDown = false;
        }
        path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y);
        paths.add(path);
        // If there aren't enough events to calculate angleSum
        if (prevVector[0] == 0 && prevVector[1] == 0){
            prevVector[0] = x - prevPoint[0];
            prevVector[1] = y - prevPoint[1];
            return;
        }
        currVector[0] = x - prevPoint[0];
        currVector[1] = y - prevPoint[1];
        float dot = currVector[0]*prevVector[0]+currVector[1]*prevVector[1];
        float det = currVector[0]*prevVector[1]-currVector[1]*prevVector[0];
        angleSum += Math.abs(Math.atan2(det, dot));
        if (!firstClick && Math.abs(angleSum) >= STRIKE_ANGLE){
            procs.add(paths.size()-1);
            clickChildButton(x, y);
            firstClick = !firstClick;
        } else if (firstClick && Math.abs(angleSum) >= (loopNum + 1) * LOOP_ANGLE){
            procs.add(paths.size()-1);
            clickChildButton(x,y);
            loopNum++;
        }
        prevVector[0] = currVector[0];
        prevVector[1] = currVector[1];
        prevPoint[0] = x;
        prevPoint[1] = y;
    }*/

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
//            procs.add(paths.size()-1);
            clickChildButton(x, y);
            firstClick = !firstClick;
            prevEventDown = false;
        }
        /*Path tempPath = new Path();
        tempPath.moveTo(x,y);
        tempPath.lineTo(x,y+0.01f);
        paths.add(tempPath);*/
//        path.moveTo(x, y);
        path.lineTo(x, y);
        animator.updatePath(path, false);
        // If there aren't enough events to calculate angleSum
        if (prevVector[0] == 0 && prevVector[1] == 0) {
            prevVector[0] = x - prevPoint[0];
            prevVector[1] = y - prevPoint[1];
            return;
        }
        currVector[0] = x - prevPoint[0];
        currVector[1] = y - prevPoint[1];
        float dot = currVector[0] * prevVector[0] + currVector[1] * prevVector[1];
        float det = currVector[0] * prevVector[1] - currVector[1] * prevVector[0];
        angleSum += Math.abs(Math.atan2(det, dot));
        if (!firstClick && Math.abs(angleSum) >= STRIKE_ANGLE) {
//            procs.add(paths.size()-1);
            clickChildButton(x, y);
            firstClick = !firstClick;
        } else if (firstClick && Math.abs(angleSum) >= (loopNum + 1) * LOOP_ANGLE) {
//            procs.add(paths.size()-1);
            clickChildButton(x, y);
            loopNum++;
        }
        prevVector[0] = currVector[0];
        prevVector[1] = currVector[1];
        prevPoint[0] = x;
        prevPoint[1] = y;
    }

    private void resetOnButtonChange(float x , float y){
        angleSum = 0;
        loopNum = 0;
        firstClick = false;
        buttonBelowIndex[0] = getColIndexOfLocation(x);
        buttonBelowIndex[1] = getRowIndexOfLocation(y);
        prevPoint[0] = x;
        prevPoint[1] = y;
        prevVector[0] = 0;
        prevVector[1] = 0;
    }

    private ButtonTextView getChildAt(float x, float y){
        ButtonTextView button = null;
//        offsetViewBounds = new Rect();
        int count = getChildCount();
        for (int i =0; i < count; i++) {
            button = (ButtonTextView) getChildAt(i);
            button.getDrawingRect(offsetViewBounds);
            offsetDescendantRectToMyCoords(button, offsetViewBounds);
            if (offsetViewBounds.contains((int) x,(int) y)){
                return button;
            }
//                offsetViewBounds.setEmpty();
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
//                path = new Path();
                path.moveTo(eventX, eventY);
                // Move a negligible amount for PathMeasure functions to work
                path.lineTo(eventX+0.01f, eventY+0.01f);
                /*Path tempPath = new Path();
                tempPath.moveTo(eventX, eventY);
                tempPath.lineTo(eventX, eventY+0.01f);
                paths.add(tempPath);*/
                animator.updatePath(path, true);
                downPoint[0] = eventX;
                downPoint[1] = eventY;
                resetOnDownAction(eventX, eventY);
                break;
            case MotionEvent.ACTION_MOVE:
                final int history = ev.getHistorySize();
                for (int i = 0; i < history; i++){
                    processMoveAction(ev.getHistoricalX(i), ev.getHistoricalY(i));
                }
                processMoveAction(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                if (!firstClick) {
//                    procs.add(paths.size()-1);
                    notifyButtonListeners(getChildAt(eventX, eventY));
                }
                angleSum = 0;
                firstClick = false;
                loopNum = 0;
                /*int count = paths.size();
                PathMeasure measure = new PathMeasure();
                float[] pos = new float[2];
                float[] tan = new float[2];
                for (int i = 0; i < count; i++) {
                    measure.setPath(paths.get(i),false);
                    measure.getPosTan(measure.getLength(), pos, tan);
                    System.out.println("path #: " + i + "(" + pos[0] + ", " + pos[1] + ")");
                }*/
                /*int count = animator.circles.size();
                PathAnimator.CircleHolder circle;
                for (int i = 0; i < count; i++) {
                    circle = animator.circles.get(i);
                    System.out.println("circle #: " + i + "(" + circle.getX() + ", " + circle.getY() + ")");
                }*/
        }
        this.invalidate();
        return false;
    }

    /*// old method for use with pre-first animation
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float eventX = ev.getX();
        final float eventY = ev.getY();
        // Event is on clear button
        if (getChildAt(eventX - STROKE_WDITH/2, eventY - STROKE_WDITH/2).equals(clearButton)){
            clearPath();
            notifyButtonListeners(clearButton);
            return false;
        }

        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                path = new Path();
                path.moveTo(eventX, eventY);
                path.lineTo(eventX, eventY);
                paths.add(path);
                downPoint[0] = eventX;
                downPoint[1] = eventY;
                resetOnDownAction(eventX, eventY);
                break;
            case MotionEvent.ACTION_MOVE:
                final int history = ev.getHistorySize();
                for (int i = 0; i < history; i++){
                    processMoveAction(ev.getHistoricalX(i), ev.getHistoricalY(i));
                }
                processMoveAction(eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
                if (!firstClick) {
                    procs.add(paths.size()-1);
                    notifyButtonListeners(getChildAt(eventX, eventY));
                }
                angleSum = 0;
                firstClick = false;
                loopNum = 0;
        }
        this.invalidate();
        return false;
    }*/
}