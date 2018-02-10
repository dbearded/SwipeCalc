package com.example.sputnik.gesturecalc;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.example.sputnik.gesturecalc.ui.BasicCalcPresenter;
import com.example.sputnik.gesturecalc.ui.DesignerCalcPresenter;

public class MainActivity extends AppCompatActivity {

    MyLayout layout;
    BasicCalcPresenter presenter;
    Button designerButton;
    PathAnimator pathAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        presenter = new BasicCalcPresenter(this);
        pathAnimator = new PathAnimator();

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.v("onCreate", "memoryClass:" + Integer.toString(memoryClass));

        layout = findViewById(R.id.gridLayout);
        ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
        if (viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                        layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    layout.setupSize();
                }
            });
        }

        layout.registerButtonListener(new MyLayout.ButtonListener() {
            @Override
            public void buttonPressed(String input) {
                presenter.buttonPressed(input);
            }
        });
        layout.setPathAnimator(pathAnimator);

        designerButton = findViewById(R.id.buttonAnimEditor);
        designerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DesignerActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
    }

}