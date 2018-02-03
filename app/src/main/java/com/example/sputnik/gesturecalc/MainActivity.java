package com.example.sputnik.gesturecalc;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MyLayout layout;
//    Canvas canvas;
//    SketchView sketchView;
    View clear;
    /*Paint paint;
    Path path2;
    Bitmap bitmap;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.v("onCreate", "memoryClass:" + Integer.toString(memoryClass));

        layout = findViewById(R.id.gridLayout);
        clear = findViewById(R.id.clear);

//        sketchView = new SketchView(this);

        /*paint = new Paint();
        path2 = new Path();*/

//        layout.addView(sketchView, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT));

        /*paint.setDither(true);
        paint.setColor(Color.parseColor("#F4511E"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(2);*/

        clear.setClickable(true);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.resetPath();
//                sketchView.invalidate();
            }
        });
    }

    /*private class SketchView extends View {

        private View reset;
        private int xStart, yStart, xEnd, yEnd;
        private int[] resetPos = new int[2];
//        private DrawingClass paintedPath;

//        private ArrayList<DrawingClass> drawingClasses = new ArrayList<>();

        public SketchView(Context context) {
            super(context);
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            Point size = new Point();
            display.getSize(size);
            bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            this.setBackgroundColor(Color.TRANSPARENT);
        }

        public void setResetButton(View reset){
            this.reset = reset;
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int yOffset = dm.heightPixels - ((ViewGroup) getParent()).getMeasuredHeight();
            this.reset.getLocationInWindow(resetPos);
            xStart = resetPos[0];
            yStart = resetPos[1] - yOffset;
            xEnd = xStart + clear.getWidth();
            yEnd = yStart + clear.getHeight();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {

            final int xPos = (int) ev.getX();
            final int yPos = (int) ev.getY();
            // Above clear button
            if (xPos >= xStart && xPos <= xEnd && yPos >= yStart && yPos <= yEnd){
                return false;
            }
//            canvas.drawPath(path2, paint);
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    DrawingClass pathWithPaint = new DrawingClass();
                    pathWithPaint.setupPaint(paint);
                    path2.moveTo(ev.getX(), ev.getY());
                    path2.lineTo(ev.getX(), ev.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    path2.lineTo(ev.getX(), ev.getY());
                    pathWithPaint.setPath(path2);
                    pathWithPaint.setupPaint(paint);
                    break;
                case MotionEvent.ACTION_UP:
                    if (pathWithPaint != null){
                        drawingClasses.add(pathWithPaint);
                }
            }
            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (drawingClasses.size() > 0) {
                canvas.drawPath(drawingClasses.get(drawingClasses.size() - 1).getPath(), drawingClasses.get(drawingClasses.size() - 1).getPaint());
            }
            canvas.drawPath(path2, paint);
        }
    }*/

    public static class ButtonTextView extends android.support.v7.widget.AppCompatTextView{
        private Path pathButton;

        public ButtonTextView(Context context) {
            super(context);
        }

        public ButtonTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public ButtonTextView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        void setPath(Path path){
            pathButton = path;
        }
    }

    /**
     * Created by Sputnik on 2/1/2018.
     */
    public static class MyLayout extends android.support.v7.widget.GridLayout {
        private View reset;
//        SketchView sketchView;
        private int xStart, yStart, xEnd, yEnd;
        private Path entirePath = new Path();


        private int[] resetPos = new int[2];
        private List<Path> paths = new LinkedList<>();
        private Paint paint;
        private Path path;
        private Bitmap bitmap;
        private Canvas canvas;

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
            setupCanvas();
            this.setWillNotDraw(false);
        }

        void setupPaint(){
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setDither(true);
            paint.setColor(getResources().getColor(R.color.swipe));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(16);
//            this.setBackgroundColor(Color.TRANSPARENT);
        }

        void setupPath(){
            path = new Path();
        }

        void setupCanvas(){
            if (!isInEditMode()) {
                Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
                DisplayMetrics displayMetrics = new DisplayMetrics();
                display.getMetrics(displayMetrics);
                Point size = new Point();
                display.getSize(size);
                bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
            }
        }

        void resetPath(){
            entirePath.reset();
            path.reset();
            paths.clear();
            this.invalidate();
        }

        @Override
        public void onViewAdded(View view) {
            super.onViewAdded(view);
            if (R.id.clear == view.getId()){
                reset = view;

            } /*else if (view instanceof SketchView){
                sketchView = (SketchView) view;
            }*/
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            /*for (Path path :
                    paths) {
                canvas.drawPath(path, paint);
            }*/
//            while(!paths.isEmpty()){
//                entirePath.addPath(((LinkedList<Path>) paths).remove());
//            }
//            canvas.drawPath(entirePath, paint);
            /*entirePath.reset();
            for (Path path :
                    paths) {
                entirePath.addPath(path);
            }
            canvas.drawPath(entirePath, paint);*/
            canvas.drawPath(path, paint);
        }

        /*@Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            *//*if (drawingClasses.size() > 0) {
                canvas.drawPath(drawingClasses.get(drawingClasses.size() - 1).getPath(), drawingClasses.get(drawingClasses.size() - 1).getPaint());
            }*//*
            canvas.drawPath(path, paint);
        }*/


        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            setResetButton(reset);
            /*int[] clearPos = new int[2];
            clear.getLocationInWindow(clearPos);
            xStart = clearPos[0];
            yStart = clearPos[1];
            xEnd = xStart + clear.getWidth();
            yEnd = yStart + clear.getHeight();*/
        }

        public void setResetButton(View reset){
            if (!isInEditMode()) {
                this.reset = reset;
                DisplayMetrics dm = new DisplayMetrics();
                Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
                display.getMetrics(dm);
//                int yOffset = dm.heightPixels - getRootView().getMeasuredHeight();
                int yOffset = 0;
                this.reset.getLocationInWindow(resetPos);
                /*xStart = resetPos[0];
                yStart = resetPos[1] - yOffset;*/
                xStart = 0;
                yStart = 0;
                xEnd = xStart + reset.getWidth();
                yEnd = yStart + reset.getHeight();
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {


            final int xPos = (int) ev.getX();
            final int yPos = (int) ev.getY();
            // Above clear button
            if (xPos >= xStart && xPos <= xEnd && yPos >= yStart && yPos <= yEnd){
                return false;
            }
//            canvas.drawPath(path2, paint);
//            Path path = new Path();
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    /*DrawingClass pathWithPaint = new DrawingClass();
                    pathWithPaint.setupPaint(paint);*/
//                    path = new Path();
                    path.moveTo(ev.getX(), ev.getY());
                    path.lineTo(ev.getX(), ev.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    for (int i = 0; i < ev.getHistorySize(); i++){
//                        path = new Path();
//                        path.moveTo(ev.getHistoricalX(i), ev.getHistoricalY(i));
                        path.lineTo(ev.getHistoricalX(i), ev.getHistoricalY(i));
//                        paths.add(path);
                    }
                    /*path.addRect((float) ev.getX(),(float) ev.getY(),(float) ev.getX()+xEnd,(float) ev.getY()+(yEnd-yStart), Path.Direction.CCW);*/
//                    path.moveTo(ev.getX(), ev.getY());
                    path.lineTo(ev.getX(), ev.getY());
                    /*pathWithPaint.setPath(path2);
                    pathWithPaint.setupPaint(paint);*/
                    break;
                case MotionEvent.ACTION_UP:
                    /*if (pathWithPaint != null){
                        drawingClasses.add(pathWithPaint);
                }*/
            }
//            canvas.drawPath(path, paint);
//            path.addCircle(300,500,100,Path.Direction.CCW);
//            paths.add(path);

            this.invalidate();
            return true;
        }

/*        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            final int action = ev.getAction();

            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
                return false; // let the child handle this
            }

            final int xPos = (int) ev.getX();
            final int yPos = (int) ev.getY();

            // Above clear button
            if (xPos >= xStart && xPos <= xEnd && yPos >= yStart && yPos <= yEnd){
                return clear.callOnClick();
//                return false;
            }
            return false;
        }*/
    }
}