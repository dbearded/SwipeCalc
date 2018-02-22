package com.example.sputnik.gesturecalc.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.example.sputnik.gesturecalc.util.PathActivator;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Sputnik on 2/7/2018.
 */

public class LineAnimatorDeuce implements PathAnimator{

    private static float strokeStartWidth = 18f;
    private static float strokeEndWidth = 0f;

    private static int opacity = 164;
    private static long animationDuration = 0;

    private float touchSlop = 16f;

    private LinkedList<LineHolder> lines = new LinkedList<>();
    private LinkedList<LineHolder> lineSubset = new LinkedList<>();
    private ArrayList<Rect> noDrawRects = new ArrayList<>();
    private int newAnimationCount = 0;
    private int animationCount;
    private boolean drawingSubset = false;
    private float prevX, prevY;
    private Canvas animCanvas;
    private Bitmap animBitmap;
    private boolean dontDraw;
    private float sX, sY, eX, eY;
    private int prevEvent;
    private LineHolder drawLine;
    private LinkedList<LineHolder> drawLines;

    public LineAnimatorDeuce(){
    }

    public void reDrawTo(int progress) {
        reDrawLineTo(progress);
        drawingSubset = true;
    }

    @Override
    public void addNoDrawRect(Rect rect) {
        noDrawRects.add(rect);
    }

    private boolean inNoDrawRects(float x, float y){
        boolean result = false;
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

    private void resetPoints() {
        sX = 0;
        sY = 0;
        eX = 0;
        eY = 0;
    }

    private void addPoint(float x, float y, boolean newContour) {
        if (newContour) {
            sX = x;
            sY = y;
        } else {
            // Account for taps
            eX = sX == x ? x + 0.1f : x;
            eY = sY == y ? y + 0.1f : y;
            addLine(sX, sY, eX, eY);
            addAnimators();
            sX = x;
            sY = y;
        }
        prevX = x;
        prevY = y;
    }

    @Override
    public void addEvent(MotionEvent event) {
        float evX = event.getX();
        float evY = event.getY();

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (inNoDrawRects(evX, evY)){
                    dontDraw = true;
                    return;
                }
                addPoint(evX, evY, true);
                break;
            case MotionEvent.ACTION_MOVE:
                if (dontDraw){
                    return;
                }
                int count = event.getHistorySize();
                for (int i = 0; i < count; i++) {
                    float histX = event.getHistoricalX(i);
                    float histY = event.getHistoricalY(i);
                    if (PathActivator.euclidDistance(histX, histY, prevX, prevY) < touchSlop) {
                        continue;
                    }
                    addPoint(histX, histY, false);
                }
                if (PathActivator.euclidDistance(evX, evY, prevX, prevY) < touchSlop) {
                    break;
                }
                addPoint(evX, evY, false);
                break;
            case MotionEvent.ACTION_UP:
                if (prevEvent == MotionEvent.ACTION_DOWN && !dontDraw){
                    addPoint(evX, evY, true);
                }
            case MotionEvent.ACTION_CANCEL:
                dontDraw = false;
                break;
        }
        prevEvent = action;
    }

    private void addLine(float sX, float sY, float eX, float eY){
        LineHolder line = new LineHolder(sX, sY, eX, eY);
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
        resetPoints();
    }

    private void resetLines(){
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
        if (drawingSubset) {
            drawLines = lineSubset;
            drawingSubset = false;
        } else {
            drawLines = lines;
        }
        for (LineHolder line :
                drawLines) {
            animCanvas.drawLine(line.sX, line.sY, line.eX, line.eY, line.getPaint());
//            canvas.drawLine(line.sX, line.sY, line.eX, line.eY, line.getPaint());
        }

        /*int size = tempLines.size();
        for (int i = 0; i < size; i++) {
            animCanvas.drawPath(lines.get(i).getPath(), lines.get(i).getPaint());
        }*/
        canvas.drawBitmap(animBitmap, 0, 0, null);
    }

    // Use a linear gradient along line for color change
    // animate the color
    class LineHolder {
        float sX, sY, eX, eY;
        private Paint paint;

        LineHolder(float startX, float startY, float endX, float endY) {
            sX = startX;
            sY = startY;
            eX = endX;
            eY = endY;
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setStrokeJoin(Paint.Join.MITER);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            paint.setStrokeWidth(LineAnimatorDeuce.strokeStartWidth);
            paint.setColor(Color.parseColor("#a46fa7be"));
            paint.setAlpha(LineAnimatorDeuce.opacity);
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

        Paint getPaint(){
            return paint;
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
        LineAnimatorDeuce.opacity = opacity;
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