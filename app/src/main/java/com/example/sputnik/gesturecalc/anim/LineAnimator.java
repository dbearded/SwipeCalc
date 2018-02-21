package com.example.sputnik.gesturecalc.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.example.sputnik.gesturecalc.util.PathActivator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Sputnik on 2/7/2018.
 */

public class LineAnimator implements PathAnimator{

    // TODO try Canvas.drawLine() or canvas.drawLines()

    private static float strokeStartWidth = 18f;
    private static float strokeEndWidth = 0f;

    private static int opacity = 164;
    private static long animationDuration = 0;

    private float touchSlop = 16f;

    private LinkedList<LineHolder> lines = new LinkedList<>();
    private LinkedList<LineHolder> lineSubset = new LinkedList<>();
    private Rect[] noDrawRects;
    private int newAnimationCount = 0;
    private int animationCount;
    private boolean drawingSubset = false;
    private float prevX, prevY;
    private Canvas animCanvas;
    private Bitmap animBitmap;
    private boolean invalidate;

    public LineAnimator(){
    }

    public void reDrawTo(int progress) {
        reDrawLineTo(progress);
        drawingSubset = true;
    }

    @Override
    public void setNoDrawRects(Rect... rects) {
        noDrawRects = rects;
    }

    private boolean inNoDrawRects(float x, float y){
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

    public void setCanvasSize(int width, int height) {
        animBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        animCanvas = new Canvas(animBitmap);
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

    private void createLine(Path segment) {
        LineHolder line = new LineHolder(segment);
        lines.add(line);
        newAnimationCount++;
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

    // add animators to any new trace points
    private void addAnimators() {
        // Setting for disabling animations
        if (animationDuration == 0){
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

    private void newLineAnimation(){
        int size = lines.size();
        AnimatorSet animatorSet = new AnimatorSet();
        LineHolder tempLine = lines.get(size - newAnimationCount);
        ObjectAnimator widthAnim = ObjectAnimator.ofFloat(tempLine, "width", strokeEndWidth);
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
        widthAnim.setDuration(animationDuration);
        ObjectAnimator alphaAnim = ObjectAnimator.ofInt(tempLine, "alpha", 0);
        alphaAnim.setDuration(animationDuration);
        animatorSet.play(widthAnim).with(alphaAnim);
        animatorSet.start();
        animationCount++;
    }

    public void updateCanvas(Canvas canvas){
        drawLines(canvas);
    }

    @Override
    public void recycle() {

    }

    @Override
    public int getCanvasWidth() {
        return animBitmap.getWidth();
    }

    @Override
    public int getCanvasHeight() {
        return animBitmap.getHeight();
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
//            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            animCanvas.drawPath(lines.get(i).getPath(), lines.get(i).getPaint());
        }
        canvas.drawBitmap(animBitmap, 0, 0, null);
    }

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
            paint.setStrokeWidth(LineAnimator.strokeStartWidth);
            paint.setColor(Color.parseColor("#a46fa7be"));
            paint.setAlpha(LineAnimator.opacity);
        }

        void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        void setWidth(float width) {
            paint.setStrokeWidth(width);
        }

        int getAlpha() {
            return paint.getAlpha();
        }

        float getWidth() {
            return paint.getStrokeWidth();
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


    @Override
    public void setStartSize(float size) {
        strokeStartWidth = size;
    }

    @Override
    public void setEndSize(float size) {
        strokeEndWidth = size;
    }

    public void setOpacity(int opacity){
        LineAnimator.opacity = opacity;
    }

    public void setAnimationDuration(int duration){
        animationDuration = duration;
    }

    @Override
    public float getStartSize() {
        return strokeStartWidth;
    }

    @Override
    public float getEndSize() {
        return strokeEndWidth;
    }

    public int getOpacity(){
        return opacity;
    }

    public long getAnimationDuration(){
        return animationDuration;
    }

    public float getTouchSlop(){
        return touchSlop;
    }

    public void setTouchSlop(float touchSlop) {
        this.touchSlop = touchSlop;
    }
}