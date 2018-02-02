package com.example.sputnik.gesturecalc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    MyLayout layout;
    Canvas canvas;
    SketchView sketchView;
    View clear;
    Paint paint;
    Path path2;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.Clayout);
        clear = findViewById(R.id.clear);

        sketchView = new SketchView(this);

        paint = new Paint();
        path2 = new Path();

        layout.addView(sketchView, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ConstraintLayout.LayoutParams.MATCH_CONSTRAINT));

        paint.setDither(true);
        paint.setColor(Color.parseColor("#F4511E"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(2);

        clear.setClickable(true);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                path2.reset();
                sketchView.invalidate();
            }
        });
    }

    private class SketchView extends View {

        private View reset;
        private int xStart, yStart, xEnd, yEnd;
        private int[] resetPos = new int[2];
//        private DrawingClass paintedPath;

//        private ArrayList<DrawingClass> drawingClasses = new ArrayList<>();

        public SketchView(Context context) {
            super(context);
            Display display = getWindowManager().getDefaultDisplay();
            /*DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);*/
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
                    /*DrawingClass pathWithPaint = new DrawingClass();
                    pathWithPaint.setPaint(paint);*/
                    path2.moveTo(ev.getX(), ev.getY());
                    path2.lineTo(ev.getX(), ev.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    path2.lineTo(ev.getX(), ev.getY());
                    /*pathWithPaint.setPath(path2);
                    pathWithPaint.setPaint(paint);*/
                    break;
                case MotionEvent.ACTION_UP:
                    /*if (pathWithPaint != null){
                        drawingClasses.add(pathWithPaint);
                }*/
            }
            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            /*if (drawingClasses.size() > 0) {
                canvas.drawPath(drawingClasses.get(drawingClasses.size() - 1).getPath(), drawingClasses.get(drawingClasses.size() - 1).getPaint());
            }*/
            canvas.drawPath(path2, paint);
        }
    }

    private class DrawingClass {
        private Path path;
        private Paint paint;

        void setPath(Path path) {
            this.path = path;
        }

        void setPaint(Paint paint) {
            this.paint = paint;
        }

        Path getPath() {
            return path;
        }

        Paint getPaint() {
            return paint;
        }
    }

    /**
     * Created by Sputnik on 2/1/2018.
     */
    public static class MyLayout extends ConstraintLayout {
        View clear;
        SketchView sketchView;
//        int xStart, yStart, xEnd, yEnd;

        public MyLayout(Context context) {
            super(context);
        }

        public MyLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void onViewAdded(View view) {
            super.onViewAdded(view);
            if (R.id.gridLayout == view.getId()){
                clear = view.findViewById(R.id.clear);

            } else if (view instanceof SketchView){
                sketchView = (SketchView) view;
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            sketchView.setResetButton(clear);
            /*int[] clearPos = new int[2];
            clear.getLocationInWindow(clearPos);
            xStart = clearPos[0];
            yStart = clearPos[1];
            xEnd = xStart + clear.getWidth();
            yEnd = yStart + clear.getHeight();*/
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
