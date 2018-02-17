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
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import java.util.ArrayList;

/**
 * Created by Sputnik on 2/7/2018.
 */

public class LineAnimator implements PathAnimator{

    // TODO try Canvas.drawLine() or canvas.drawLines()


//    private static float circleStartDiameter = 16f;
//    private static float circleEndDiameter = 0f;
//    private static float circleCenterSpacing = 16f;
    private static float strokeStartWidth = 24f;
    private static float strokeEndWidth = 0f;

    private static int opacity = 164;
    private static long animationDuration = 0;
    // If false, then lines, cuz only two types
    private static boolean circleAnimType;

//    private Path path;
//    private PathMeasure pathMeasure;
    private ArrayList<LineHolder> lines = new ArrayList<>();
    private ArrayList<LineHolder> lineSubset = new ArrayList<>();
    private int newAnimationCount = 0;
//    private int contourCount = 0;
//    private float contourLength;
    private int animationCount;
    private boolean drawingSubset = false;
    private float prevX, prevY;
    private Canvas animCanvas;
    private Bitmap animBitmap;

    public LineAnimator(){
        /*path = new Path();
        pathMeasure = new PathMeasure();*/
    }

    public void reDrawTo(int progress) {
        reDrawLineTo(progress);
        drawingSubset = true;
    }

    public void setCanvasSize(int width, int height) {
        animBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        animCanvas = new Canvas(animBitmap);
    }

    private void reDrawLineTo(int progress){
        lineSubset = (ArrayList<LineHolder>) lines.clone();
        int count = (int) (((float)(100 - progress))/((float) 100)* lineSubset.size());
        for (int i = 0; i < count - 1; i++) {
            lineSubset.remove(lineSubset.size() -1);
        }
    }

    public void addSpecialPoint(float x, float y){
        /*createCircle(x,y);
        CircleHolder specialCircle = circles.get(circles.size() - 1);
        specialCircle.setColor(Color.RED);
        specialCircle.setDiameter(18f);
        addAnimators();*/
    }

    public void addPoint(float x, float y, boolean newContour) {
        if (newContour) {
            prevX = 0;
            prevY = 0;
//            path.close();
//            path.moveTo(x,y);
//            contourCount++;
            addLine(x, y);
//            distToNextCircle = circleCenterSpacing;
//            contourLength = 0;
            addAnimators();
        } else {
//            path.lineTo(x,y);
        }

        /*pathMeasure.setPath(path, false);
        for (int i = 0; i < contourCount; i++) {
            pathMeasure.nextContour();
        }*/
        addLine(x, y);

        addAnimators();
//        contourLength = pathMeasure.getLength();
        prevX = x;
        prevY = y;
    }

    /*public void setAnimType(boolean circle){
        if (circleAnimType == circle){
            return;
        }
        if (circleAnimType) {
            resetLines();
        } else {
            resetCircles();
        }
        circleAnimType = circle;
    }*/

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

    public void reset() {
        resetLines();
        drawingSubset = false;
//        contourCount = 0;
/*        path.rewind();
        pathMeasure.setPath(path, false);*/
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
            /*Path path = new Path(this.path);
            path.close();
            path.moveTo(0,0);
            path.lineTo(0,0);
            path.lineTo(750, 750);*/
/*            Paint paint = new Paint();
            paint.setDither(true);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(16);
//            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(tempPath, paint);*/
    }

    @Override
    public void recycle() {

    }

    private void drawLines(Canvas canvas) {
        animBitmap.eraseColor(Color.TRANSPARENT);
        ArrayList<LineHolder> tempLines;
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
}