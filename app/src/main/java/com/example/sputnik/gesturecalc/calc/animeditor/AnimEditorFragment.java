package com.example.sputnik.gesturecalc.calc.animeditor;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.anim.CircleAnimator;
import com.example.sputnik.gesturecalc.anim.FactoryAnimator;
import com.example.sputnik.gesturecalc.anim.PathAnimator;
import com.example.sputnik.gesturecalc.util.ButtonGrid;
import com.example.sputnik.gesturecalc.util.PathActivator;

/**
 * Created by Sputnik on 2/16/2018.
 */

public class AnimEditorFragment extends Fragment implements AnimEditorContract.View {

    AnimEditorContract.Presenter presenter;
    PathAnimator animator;
    TextView display, preview;
    SeekBar spacingBar;

    abstract class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
    class MyTextWatcher implements TextWatcher {
        private SeekBar seekBar;

        MyTextWatcher(SeekBar seekBar){
            this.seekBar = seekBar;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String str = s.toString();
            if (str.isEmpty()){
                return;
            }
            int val = Integer.valueOf(str);
            val = val < 0 ? 0 : val;
            val = val > seekBar.getMax() ? seekBar.getMax() : val;
            seekBar.setProgress(val);
        }
    }

    @Override
    public void setPresenter(AnimEditorContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_anim_edit, container, false);

        final ButtonGrid buttonGrid = root.findViewById(R.id.gridLayout);
        PathActivator activator = new PathActivator();
        animator = FactoryAnimator.makeAnimator(FactoryAnimator.Type.Circle);
        ViewTreeObserver viewTreeObserver = ((ViewGroup) buttonGrid).getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        ((ViewGroup) buttonGrid).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        ((ViewGroup) buttonGrid).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    buttonGrid.setupSize();
                    animator.setCanvasSize(((ViewGroup) buttonGrid).getWidth(), ((ViewGroup) buttonGrid).getHeight());
                }
            });
        }
        buttonGrid.setPathActivator(activator);
        buttonGrid.setPathAnimator(animator);

        display = root.findViewById(R.id.display);
        preview = root.findViewById(R.id.preview);

        SeekBar sizeBar = root.findViewById(R.id.seekBarSize);
        spacingBar = root.findViewById(R.id.seekBarSpacing);
        SeekBar durationBar = root.findViewById(R.id.seekBarDuration);
        SeekBar opacityBar = root.findViewById(R.id.seekBarOpacity);
        final SeekBar pathBar = root.findViewById(R.id.seekBarPath);

        final EditText sizeEdit = root.findViewById(R.id.editNumberSize);
        final EditText spacingEdit = root.findViewById(R.id.editNumberSpacing);
        final EditText durationEdit = root.findViewById(R.id.editNumberDuration);
        final EditText opacityEdit = root.findViewById(R.id.editNumberOpacity);

        Switch shapeSwitch = root.findViewById(R.id.switchCircleLine);
        Spinner spinner = root.findViewById(R.id.spinnerSetting);


        sizeBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    sizeEdit.setText(Integer.toString(progress));
                    animator.setStartSize(progress);
                }
            }
        });
        spacingBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    spacingEdit.setText(Integer.toString(progress));
                    if (animator instanceof CircleAnimator) {
                        ((CircleAnimator) animator).setCircleCenterSpacing(progress);
                    }
                }
            }
        });
        durationBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    durationEdit.setText(Integer.toString(progress));
                    animator.setAnimationDuration(progress);
                }
            }
        });
        opacityBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    opacityEdit.setText(Integer.toString(progress));
                    animator.setOpacity(progress);
                }
            }
        });
        pathBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    animator.reDrawTo(progress);
                    ((ViewGroup) buttonGrid).invalidate();
                }
            }
        });

        int sizeInit = (int) animator.getStartSize();
        int spacingInit = (int) ((CircleAnimator) animator).getCircleCenterSpacing();
        int durationInit = (int) animator.getAnimationDuration();
        int opacityInit = animator.getOpacity();

        sizeBar.setProgress(sizeInit);
        spacingBar.setProgress(spacingInit);
        durationBar.setProgress(durationInit);
        opacityBar.setProgress(opacityInit);

        sizeEdit.setText(String.valueOf(sizeInit));
        spacingEdit.setText(String.valueOf(spacingInit));
        durationEdit.setText(String.valueOf(durationInit));
        opacityEdit.setText(String.valueOf(opacityInit));

        sizeEdit.addTextChangedListener(new MyTextWatcher(sizeBar));
        spacingEdit.addTextChangedListener(new MyTextWatcher(spacingBar));
        durationEdit.addTextChangedListener(new MyTextWatcher(durationBar));
        opacityEdit.addTextChangedListener(new MyTextWatcher(opacityBar));

        shapeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               int width, height;
               width = animator.getCanvasWidth();
               height = animator.getCanvasHeight();
               animator.recycle();
               if (isChecked) {
                   animator = FactoryAnimator.makeAnimator(FactoryAnimator.Type.Line);
               } else {
                   animator = FactoryAnimator.makeAnimator(FactoryAnimator.Type.Circle);
                   spacingBar.setProgress((int) ((CircleAnimator) animator).getCircleCenterSpacing());
               }
               animator.setCanvasSize(width, height);
               buttonGrid.setPathAnimator(animator);
               ((ViewGroup) buttonGrid).invalidate();
           }
       });

        buttonGrid.registerButtonListener(new ButtonGrid.ButtonListener() {
            @Override
            public void buttonPressed(String input) {
                presenter.addNewValue(input);
                pathBar.setProgress(pathBar.getMax());
            }
        });

        presenter.start();

        return root;
    }

    public void updateDisplay(String expression) {
        display.setText(expression);
    }

    @Override
    public void updatePreview(String expression) {
        preview.setText(expression);
    }
}