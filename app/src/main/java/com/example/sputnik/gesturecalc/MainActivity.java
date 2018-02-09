package com.example.sputnik.gesturecalc;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;

import com.example.sputnik.gesturecalc.ui.BasicCalcPresenter;

public class MainActivity extends AppCompatActivity {

    MyLayout layout;
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
    }

}