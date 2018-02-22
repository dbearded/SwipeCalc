package com.example.sputnik.gesturecalc.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.anim.PathAnimator;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 2/1/2018.
 */
public class ButtonGridCompat extends android.support.v7.widget.GridLayout implements ButtonGrid {
//    private final float STROKE_WIDTH = 18f;
    private List<ButtonGrid.ButtonListener> buttonListeners = new ArrayList<>();
    private PathAnimator animator;
    private PathActivator activator;
    private Rect buttonBoundary = new Rect();

    public interface ButtonListener {
        void buttonPressed(String input);
    }

    public ButtonGridCompat(Context context) {
        super(context);
        setup();
    }

    public ButtonGridCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ButtonGridCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public void setPathAnimator(PathAnimator animator){
        this.animator = animator;
    }

    public void setPathActivator(final PathActivator activator) {
        this.activator = activator;
        this.activator.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                float x = ((PointF) arg).x;
                float y = ((PointF) arg).y;
                clickChildAt(x, y);
                animator.addSpecialPoint(x, y);
            }
        });
        this.activator.registerViewBoundaryListener(new PathActivator.ViewBoundaryListener() {
            @Override
            public Rect updateViewBounds(float x, float y) {
                return getChildViewBoundsAt(x, y);
            }
        });
    }

    void setup(){
        this.setWillNotDraw(false);
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
    public void onViewAdded(final View child) {
        int childId = child.getId();
        if (childId == R.id.clear){
            ViewTreeObserver viewTreeObserver = child.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < 16) {
                            child.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            child.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        child.getDrawingRect(buttonBoundary);
                        offsetDescendantRectToMyCoords(child, buttonBoundary);
                        animator.addNoDrawRect(buttonBoundary);
                        activator.addNoActivateRect(buttonBoundary);
                    }
                });
            }
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    animator.clear();
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(10);
                }
            });
        } else {
            child.setClickable(true);
            child.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(10);
                }
            });
        }
        super.onViewAdded(child);
    }

    private void clickChildAt(float x, float y) {
        View child;
        Rect offsetViewBounds = new Rect();
        int count = getChildCount();
        for (int i =0; i < count; i++) {
            child = getChildAt(i);
            child.getDrawingRect(offsetViewBounds);
            offsetDescendantRectToMyCoords(child, offsetViewBounds);
            if (offsetViewBounds.contains((int) x,(int) y)){
                child.callOnClick();
                notifyButtonListeners((TextView) child);
                return;
            }
            offsetViewBounds.setEmpty();
        }
    }

    public void registerButtonListener(ButtonGrid.ButtonListener listener){
        buttonListeners.add(listener);
    }

    public void unregisterButtonListener(ButtonGrid.ButtonListener listener){
        buttonListeners.remove(listener);
    }

    private Rect getChildViewBoundsAt(float x, float y){
        Rect rect = new Rect();
        View child;
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            child = getChildAt(i);
            child.getDrawingRect(rect);
            offsetDescendantRectToMyCoords(child, rect);
            if (rect.contains((int) x, (int) y)){
                return rect;
            }
        }
        return rect;
    }

    private void notifyButtonListeners(TextView button){
        String str = String.valueOf(button.getText());
        for (ButtonGrid.ButtonListener listener :
                buttonListeners) {
            listener.buttonPressed(str);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        animator.addEvent(event);
        activator.addEvent(event);
        this.invalidate(); // this should probably be done elsewhere
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // capture all touch events
        // will call click events on children manually
        return true;
    }
}