package com.example.sputnik.gesturecalc;

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

public class PathAnimator {

    private static float circleStartDiameter = 16f;
    private static float circleEndDiameter = 0f;
    private static float circleCenterSpacing = 16f;
    private static float strokeStartWidth = 24f;
    private static float strokeEndWidth = 0f;

    private static int opacity = 164;
    private static long animationDuration = 0;
    // If false, then lines, cuz only two types
    private static boolean circleAnimType;

    private Path path;
    private PathMeasure pathMeasure;
    private ArrayList<CircleHolder> circles = new ArrayList<>();
    private ArrayList<CircleHolder> circleSubset = new ArrayList<>();
    private ArrayList<LineHolder> lines = new ArrayList<>();
    private ArrayList<LineHolder> lineSubset = new ArrayList<>();
    private int newAnimationCount = 0;
    private int contourCount = 0;
    private float distToNextCircle;
    private float contourLength;
    private float[] circPos = new float[2];
    private int animationCount;
    private boolean drawingSubset = false;
    private float prevX, prevY;
    private Canvas animCanvas;
    private Bitmap animBitmap;

    public PathAnimator(){
        path = new Path();
        pathMeasure = new PathMeasure();
    }

    public void reDrawTo(int progress) {
        if (circleAnimType){
            reDrawCirclesTo(progress);
        } else {
            reDrawLineTo(progress);
        }
        drawingSubset = true;
    }

    public void setSize(int width, int height) {
        animBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        animCanvas = new Canvas(animBitmap);
    }

    private void reDrawCirclesTo(int progress) {
        circleSubset = (ArrayList<CircleHolder>) circles.clone();
        int count = (int) (((float)(100 - progress))/((float) 100)* circleSubset.size());
        for (int i = 0; i < count-1; i++) {
            circleSubset.remove(circleSubset.size()-1);
        }
    }

    private void reDrawLineTo(int progress){
        lineSubset = (ArrayList<LineHolder>) lines.clone();
        int count = (int) (((float)(100 - progress))/((float) 100)* lineSubset.size());
        for (int i = 0; i < count - 1; i++) {
            lineSubset.remove(lineSubset.size() -1);
        }
    }

    void addSpecial(float x, float y){
        createCircle(x,y);
        CircleHolder specialCircle = circles.get(circles.size() - 1);
        specialCircle.setColor(Color.RED);
        specialCircle.setDiameter(18f);
        addAnimators();
    }

    void update(float x, float y, boolean newContour) {
        if (newContour) {
            prevX = 0;
            prevY = 0;
//            path.close();
            path.moveTo(x,y);
            contourCount++;
            if (circleAnimType){
                createCircle(x,y);
            } else {
                addLine(x, y);
            }
            distToNextCircle = circleCenterSpacing;
            contourLength = 0;
            addAnimators();
        } else {
            path.lineTo(x,y);
        }

        pathMeasure.setPath(path, false);
        for (int i = 0; i < contourCount; i++) {
            pathMeasure.nextContour();
        }

        if (circleAnimType){
            addCircles();
        } else {
            addLine(x, y);
        }

        addAnimators();
        contourLength = pathMeasure.getLength();
        prevX = x;
        prevY = y;
    }

    void setAnimType(boolean circle){
        if (circleAnimType == circle){
            return;
        }
        if (circleAnimType) {
            resetLines();
        } else {
            resetCircles();
        }
        circleAnimType = circle;
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
        // update private fields since added a segment
        distToNextCircle = circleCenterSpacing - ((segmentLength - distToNextCircle) % circleCenterSpacing);
    }

    private void createCircle(float x, float y) {
        CircleHolder circle = new CircleHolder(circleStartDiameter, x - circleStartDiameter / 2, y - circleStartDiameter / 2);
        circles.add(circle);
        newAnimationCount++;
    }

    private void createLine(Path segment) {
        LineHolder line = new LineHolder(segment);
        lines.add(line);
        newAnimationCount++;
    }

    void reset() {
        resetCircles();
        resetLines();
        drawingSubset = false;
        contourCount = 0;
        path.rewind();
        pathMeasure.setPath(path, false);
        prevX = 0;
        prevY = 0;
    }

    private void resetCircles(){
        circles.clear();
        circleSubset.clear();
        distToNextCircle = circleCenterSpacing;
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

    boolean isRunning(){
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
            if (circleAnimType){
                newCircleAnimation();
            } else {
                newLineAnimation();
            }
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

    void updateCanvas(Canvas canvas){
        if (circleAnimType){
            drawCircles(canvas);
        } else {
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

    class CircleHolder {
        private float x, y;
        private ShapeDrawable shape;

        CircleHolder(float diameter, float x, float y){
            OvalShape circle = new OvalShape();
            circle.resize(diameter, diameter);
            this.shape = new ShapeDrawable(circle);
            shape.getPaint().setColor(Color.parseColor("#a46fa7be"));
            shape.getPaint().setAlpha(PathAnimator.opacity);
            this.x = x;
            this.y = y;
        }

        float getX() {
            return x;
        }

        float getY() {
            return y;
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


        void setX(float x) {
            this.x = x - shape.getShape().getWidth() / 2;
        }

        void setY(float y) {
            this.y = y - shape.getShape().getHeight() / 2;
        }

        void setColor(int color){
            shape.getPaint().setColor(color);
        }
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
            paint.setStrokeWidth(PathAnimator.strokeStartWidth);
            paint.setColor(Color.parseColor("#a46fa7be"));
            paint.setAlpha(PathAnimator.opacity);
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

    public void setCircleStartDiameter(float diameter){
        circleStartDiameter = diameter;
    }

    public void setCircleEndDiameter(float diameter){
        circleEndDiameter = diameter;
    }

    public void setCircleCenterSpacing(float spacing){
        circleCenterSpacing = spacing;
    }

    public void setOpacity(int opacity){
        PathAnimator.opacity = opacity;
    }

    public void setAnimationDuration(int duration){
        animationDuration = duration;
    }

    public void setAnimationType(boolean type) {
        circleAnimType = type;
    }

    public float getCircleStartDiameter(){
        return circleStartDiameter;
    }

    public float getCircleEndDiameter(){
        return circleEndDiameter;
    }

    public float getCircleCenterSpacing(){
        return circleCenterSpacing;
    }

    public int getOpacity(){
        return opacity;
    }

    public long getAnimationDuration(){
        return animationDuration;
    }

    public boolean getAnimationType() {
        return circleAnimType;
    }
}