package com.derekbearded.android.swipecalc.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Keep;
import android.view.MotionEvent;

import com.derekbearded.android.swipecalc.util.PathActivator;

import java.util.LinkedList;

/**
 * Created by Sputnik on 2/7/2018.
 */

class LineAnimator extends com.derekbearded.android.swipecalc.anim.Animator {

    private LinkedList<LineHolder> lines = new LinkedList<>();
    private LinkedList<LineHolder> lineSubset = new LinkedList<>();
    private int newAnimationCount = 0;
    private int animationCount;
    private boolean drawingSubset = false;
    private float prevX, prevY;
    private Canvas animCanvas;
    private Bitmap animBitmap;
    private boolean invalidate;

    // Use a linear gradient along line for color change
    // animate the color
    class LineHolder {
        private Path path;
        private Paint paint;

        LineHolder(Path segment) {
            path = segment;
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            paint.setStrokeWidth(getStartSize());
            paint.setColor(Color.parseColor("#F4511E"));
            paint.setAlpha(getOpacity());
        }

        @Keep
        public int getAlpha() {
            return paint.getAlpha();
        }

        @Keep
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Keep
        public float getWidth() {
            return paint.getStrokeWidth();
        }

        @Keep
        public void setWidth(float width) {
            paint.setStrokeWidth(width);
        }

        Path getPath(){
            return path;
        }

        Paint getPaint(){
            return paint;
        }

        void recycle(){
            path.rewind();
        }

        void reset() {
            path.reset();
        }
    }

    public LineAnimator(Settings settings){
        super(settings);
    }

    public void reDrawTo(int progress) {
        reDrawLineTo(progress);
        drawingSubset = true;
    }

    private void reDrawLineTo(int progress){
        lineSubset = (LinkedList<LineHolder>) lines.clone();
        int count = (int) (((float)(100 - progress))/((float) 100)* lineSubset.size());
        for (int i = 0; i < count - 1; i++) {
            lineSubset.removeLast();
        }
    }

    public void addSpecialPoint(float x, float y){
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
            prevX = 0;
            prevY = 0;
            addLine(x, y);
            addAnimators();
        }
        addLine(x, y);
        addAnimators();
//        contourLength = pathMeasure.getLength();
        prevX = x;
        prevY = y;
    }

    private void addLine(float x, float y){
        Path segment = new Path();
        if (prevX == 0 && prevY == 0){
            segment.moveTo(x, y);
        } else {
            segment.moveTo(prevX, prevY);
        }
        segment.lineTo(x, y);
        createLine(segment);
    }

    // add animators to any new trace points
    private void addAnimators() {
        // Setting for disabling animations
        if (getAnimationDuration() == 0){
            newAnimationCount = 0;
            return;
        }
        while(newAnimationCount > 0){
            newLineAnimation();
            newAnimationCount--;
        }
        // Now count is less than zero. Need to reset
        newAnimationCount = 0;
    }

    private void createLine(Path segment) {
        LineHolder line = new LineHolder(segment);
        lines.add(line);
        newAnimationCount++;
    }

    private void newLineAnimation(){
        int size = lines.size();
        AnimatorSet animatorSet = new AnimatorSet();
        LineHolder tempLine = lines.get(size - newAnimationCount);
        ObjectAnimator widthAnim = ObjectAnimator.ofObject(tempLine, "width", new FloatEvaluator(), getEndSize());
//        ObjectAnimator widthAnim = ObjectAnimator.ofFloat(tempLine, "width", getEndSize());
        widthAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                LineHolder tempLine = (LineHolder) ((ObjectAnimator) animation).getTarget();
                if (lines.contains(tempLine)){
//                    lines.get(lines.indexOf(tempLine)).recycle(); // Use this later
                    lines.remove(tempLine);
                }
                animationCount--;
            }
        });
        widthAnim.setDuration(getAnimationDuration());
        ObjectAnimator alphaAnim = ObjectAnimator.ofObject(tempLine, "alpha", new IntEvaluator(), 0);
//        ObjectAnimator alphaAnim = ObjectAnimator.ofInt(tempLine, "alpha", 0);
        alphaAnim.setDuration(getAnimationDuration());
        animatorSet.play(widthAnim).with(alphaAnim);
        animatorSet.start();
        animationCount++;
    }

    public void clear() {
        reset();
    }

    private void reset() {
        resetLines();
        drawingSubset = false;
        prevX = 0;
        prevY = 0;
    }

    private void resetLines(){
        int count = lines.size();
        for (int i = 0; i < count; i++) {
            lines.get(i).reset();
        }
        lines.clear();
        lineSubset.clear();
        animBitmap.eraseColor(Color.TRANSPARENT);
    }

    public boolean isRunning(){
        return animationCount != 0;
    }

    public void updateCanvas(Canvas canvas){
        drawLines(canvas);
    }

    @Override
    public void recycle() {
        for (LineHolder line :
                lines) {
            line.getPath().reset();
        }
        lines.clear();
    }

    public void setCanvasSize(int width, int height) {
        super.setCanvasSize(width, height);
        if (width <= 0 || height <= 0){
            return;
        }
        animBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        animCanvas = new Canvas(animBitmap);
    }

    private void drawLines(Canvas canvas) {
        animBitmap.eraseColor(Color.TRANSPARENT);
        LinkedList<LineHolder> tempLines;
        if (drawingSubset) {
            tempLines = lineSubset;
            drawingSubset = false;
        } else {
            tempLines = lines;
        }
        int size = tempLines.size();
        for (int i = 0; i < size; i++) {
            animCanvas.drawPath(lines.get(i).getPath(), lines.get(i).getPaint());
        }
        canvas.drawBitmap(animBitmap, 0, 0, null);
    }
}