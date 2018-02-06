package com.example.sputnik.gesturecalc;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.ui.BasicCalcPresenter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MyLayout layout;
    View clear;
    BasicCalcPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new BasicCalcPresenter(this);

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.v("onCreate", "memoryClass:" + Integer.toString(memoryClass));

        layout = findViewById(R.id.gridLayout);
        ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16){
                        layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    layout.setupSize();
                }
            });
        }
        clear = findViewById(R.id.clear);
        viewTreeObserver = clear.getViewTreeObserver();
        if (viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < 16){
                        clear.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    layout.setResetButton(clear);
                }
            });
        }
        /*clear.setClickable(true);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.clearPath();
            }
        });*/

        layout.registerButtonListener(new MyLayout.ButtonListener() {
            @Override
            public void buttonPressed(String input) {
                presenter.buttonPressed(input);
            }
        });
    }

    public static class ButtonTextView extends android.support.v7.widget.AppCompatTextView{

        public ButtonTextView(Context context) {
            super(context);
            setupClickListener();
        }

        public ButtonTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setupClickListener();
        }

        public ButtonTextView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setupClickListener();
        }

        private void setupClickListener() {
            this.setClickable(true);
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(10);
                }
            });
        }
    }

    /**
     * Created by Sputnik on 2/1/2018.
     */
    public static class MyLayout extends android.support.v7.widget.GridLayout {
        private final float TOUCH_SLOP = 16f;
        // Envisioned for animation
        private final float MIN_EVENT_DIST = 16f;
        // Strike angle is 71/180*3.14 = 1.24f
        // Strike angle is 65/180*3.14 = 1.13f
        // Strike angle is 61/180*3.14 = 1.064f
        // Strike angle is 55/180*3.14 = 0.960
        // Strike angle is
        private final float STRIKE_ANGLE = 0.96f;
        // Loop angle is 260/180*3.14
        private final float LOOP_ANGLE = 4.54f;
        private List<ButtonListener> buttonListeners = new ArrayList<ButtonListener>();
        private ButtonTextView clearButton;
        private Path entirePath = new Path();
        private Path path;
        private List<Path> paths = new LinkedList<>();
        private List<Integer> procs = new ArrayList<>();
        private Paint paint;
        private Paint clickedPaint;
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

        void setup(){
            setupPaint();
            setupPath();
            this.setWillNotDraw(false);
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
            clickedPaint.setStrokeWidth(18);
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
        }

        int getColIndexOfLocation(float x){
            return ((int) (x) / ((int) PX_PER_COL));
        }

        int getRowIndexOfLocation(float y){
            return ((int) (y) / ((int) PX_PER_ROW));
        }

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
        }

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

        private void processMoveAction(float x, float y){
            float dx = x - prevPoint[0];
            float dy = y - prevPoint[1];
            // Ignore event if it's too close to previous
            if (Math.abs(dx) <= TOUCH_SLOP && Math.abs(dy) <= TOUCH_SLOP){
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
            Rect offsetViewBounds = new Rect();
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

        /*@Override
        public boolean onTouchEvent(MotionEvent ev) {
            final float eventX = ev.getX();
            final float eventY = ev.getY();

            // Event is on clear button
            if (eventX >= clearButtonBoundary[0] && eventX <= clearButtonBoundary[2] && eventY >= clearButtonBoundary[1] && eventY <= clearButtonBoundary[3]){
                return false;
            }

            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    path = new Path();
                    path.moveTo(eventX, eventY);
                    path.lineTo(eventX, eventY);
                    paths.add(path);
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
                    angleSum = 0;
                    firstClick = false;
                    loopNum = 0;
            }
            this.invalidate();
            return true;
        }*/

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            final float eventX = ev.getX();
            final float eventY = ev.getY();
            // Event is on clear button
            if (getChildAt(eventX, eventY).equals(clearButton)){
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
//                    procs.add(paths.size()-1);
                    downPoint[0] = eventX;
                    downPoint[1] = eventY;
                    resetOnDownAction(eventX, eventY);
//                    notifyButtonListeners(getChildAt(eventX, eventY));
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
        }
    }
}