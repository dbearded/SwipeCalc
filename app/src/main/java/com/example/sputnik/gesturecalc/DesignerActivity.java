package com.example.sputnik.gesturecalc;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;

import com.example.sputnik.gesturecalc.ui.DesignerCalcPresenter;

/**
 * Created by Sputnik on 2/9/2018.
 */

public class DesignerActivity extends AppCompatActivity {
    MyLayout layout;
    AppCompatSeekBar sizeBar, spacingBar, durationBar, opacityBar;
    AppCompatEditText sizeEdit, spacingEdit, durationEdit, opacityEdit;
    SwitchCompat shapeSwitch;
    AppCompatSpinner spinner;
    SpinnerAdapter spinnerAdapter;
    DesignerCalcPresenter presenter;
    interface MyInterpolator{
        double interpolate(double min, double progress, double max);
    }
    class LinearInterpolator implements MyInterpolator{
        @Override
        public double interpolate(double min, double progress, double max) {
            if (progress <= min){
                return min;
            }
            if (progress >= max){
                return max;
            }
            return progress/(max-min)*max;
        }
    }
    // Will have to override SeekBar class to use this
    class LogarithmicInterpolator implements MyInterpolator{
        // Going from text to seek bar
        @Override
        public double interpolate(double min, double progress, double max) {
            if (progress < min || progress > max){
                throw new IllegalArgumentException("Parameter progress not within bounds: " + progress);
            }
            if (min < 1){
                throw  new IllegalArgumentException("Min must be >= 1: " + min);
            }
            return (Math.log10(progress)-Math.log10(min))/(Math.log10(max)-Math.log(min));
        }
    }
    class MySeekBarChangeListener<T extends AppCompatEditText> implements SeekBar.OnSeekBarChangeListener{
        private T t;
        private DesignerCalcPresenter presenter;

        MySeekBarChangeListener(T t, DesignerCalcPresenter presenter){
            this.t = t;
            this.presenter = presenter;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                t.setText(Integer.toString(progress));
                presenter.settingsChanged();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
    class MyTextWatcher<T extends  SeekBar, V extends MyInterpolator> implements TextWatcher{

        private T t;
        private V v;
        private DesignerCalcPresenter presenter;
        private int min;

        MyTextWatcher(T t, V v, DesignerCalcPresenter presenter, int min){
            this.t = t;
            this.v = v;
            this.presenter = presenter;
            this.min = min;
        }

        void setMin(int min){
            this.min = min;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (text.isEmpty()) {
                return;
            }
            t.setProgress((int) v.interpolate(min, Double.valueOf(s.toString()), t.getMax()));
            presenter.settingsChanged();
        }
    }
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer);

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

        sizeBar = findViewById(R.id.seekBarSize);
        spacingBar = findViewById(R.id.seekBarSpacing);
        durationBar = findViewById(R.id.seekBarDuration);
        opacityBar = findViewById(R.id.seekBarOpacity);

        sizeEdit = findViewById(R.id.editNumberSize);
        spacingEdit = findViewById(R.id.editNumberSpacing);
        durationEdit = findViewById(R.id.editNumberDuration);
        opacityEdit = findViewById(R.id.editNumberOpacity);

        shapeSwitch = findViewById(R.id.switchCircleLine);
        spinner = findViewById(R.id.spinnerSetting);

        presenter = new DesignerCalcPresenter(this);

        sizeBar.setOnSeekBarChangeListener(new MySeekBarChangeListener<>(sizeEdit, presenter));
        spacingBar.setOnSeekBarChangeListener(new MySeekBarChangeListener<>(spacingEdit, presenter));
        durationBar.setOnSeekBarChangeListener(new MySeekBarChangeListener<>(durationEdit, presenter));
        opacityBar.setOnSeekBarChangeListener(new MySeekBarChangeListener<>(opacityEdit, presenter));

        sizeEdit.addTextChangedListener(new MyTextWatcher<>(sizeBar, new LinearInterpolator(), presenter, 5));
        spacingEdit.addTextChangedListener(new MyTextWatcher<>(spacingBar, new LinearInterpolator(), presenter, 5));
        durationEdit.addTextChangedListener(new MyTextWatcher<>(durationBar, new LinearInterpolator(), presenter, 50));
        opacityEdit.addTextChangedListener(new MyTextWatcher<>(opacityBar, new LinearInterpolator(), presenter, 0));
//        opacityEdit.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                String text = s.toString();
//                if (text.isEmpty()) {
//                    return;
//                }
//                opacityBar.setProgress(Integer.valueOf(text));
//            }
//        });

        shapeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                presenter.settingsChanged();
            }
        });
    }
}
