package com.example.sputnik.gesturecalc.calc.animeditor;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Switch;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.anim.Animator;
import com.example.sputnik.gesturecalc.anim.Settings;
import com.example.sputnik.gesturecalc.util.ButtonGrid;
import com.example.sputnik.gesturecalc.util.PathActivator;
import com.example.sputnik.gesturecalc.util.Util;

/**
 * Created by Sputnik on 2/16/2018.
 */

public class AnimEditorFragment extends Fragment implements AnimEditorContract.View, SharedPreferences.OnSharedPreferenceChangeListener {

    AnimEditorContract.Presenter presenter;
    Animator animator;
    ButtonGrid buttonGrid;
    TextView display, preview;
    SeekBar spacingBar, sizeBar, durationBar, opacityBar, pathBar;
    EditText sizeEdit, spacingEdit, durationEdit, opacityEdit;
    Switch shapeSwitch;
    Spinner spinner;

    abstract class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Util.updateAnimatorSettingsOnChange(animator, sharedPreferences, key);
    }

    @Override
    public void setPresenter(AnimEditorContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_anim_edit, container, false);

        buttonGrid = root.findViewById(R.id.gridLayout);
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
                    animator.setCanvasSize(((ViewGroup) buttonGrid).getWidth(), ((ViewGroup) buttonGrid).getHeight());
                }
            });
        }
        buttonGrid.setPathActivator(new PathActivator());

        display = root.findViewById(R.id.display);
        preview = root.findViewById(R.id.preview);

        sizeBar = root.findViewById(R.id.seekBarSize);
        spacingBar = root.findViewById(R.id.seekBarSpacing);
        durationBar = root.findViewById(R.id.seekBarDuration);
        opacityBar = root.findViewById(R.id.seekBarOpacity);
        pathBar = root.findViewById(R.id.seekBarPath);

        sizeEdit = root.findViewById(R.id.editNumberSize);
        spacingEdit = root.findViewById(R.id.editNumberSpacing);
        durationEdit = root.findViewById(R.id.editNumberDuration);
        opacityEdit = root.findViewById(R.id.editNumberOpacity);

        shapeSwitch = root.findViewById(R.id.switchCircleLine);
        spinner = root.findViewById(R.id.spinnerSetting);

        presenter.start();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        Settings settings = Util.loadAnimatorSettings(getActivity());
        updateCustomizeables(settings);
        animator = Animator.changeSettings(animator, settings);
        buttonGrid.setPathAnimator(animator);
        setViewListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
        Util.saveAnimatorSettings(getActivity(), animator.getSettings());
    }

    public void updateDisplay(String expression) {
        display.setText(expression);
    }

    @Override
    public void updatePreview(String expression) {
        preview.setText(expression);
    }

    private void updateCustomizeables(Settings settings) {
        int sizeInit = (int) settings.getStartSize();
        int spacingInit = (int) settings.getSpacing();
        int durationInit = (int) settings.getAnimationDuration();
        int opacityInit = settings.getOpacity();
        boolean isChecked = settings.getType().equals(Animator.Type.Line);

        sizeBar.setProgress(sizeInit);
        spacingBar.setProgress(spacingInit);
        durationBar.setProgress(durationInit);
        opacityBar.setProgress(opacityInit);

        sizeEdit.setText(String.valueOf(sizeInit));
        spacingEdit.setText(String.valueOf(spacingInit));
        durationEdit.setText(String.valueOf(durationInit));
        opacityEdit.setText(String.valueOf(opacityInit));

        shapeSwitch.setChecked(isChecked);
    }

    private void setViewListeners(){
        sizeBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    sizeEdit.setText(Integer.toString(progress));
                } else {
                    animator.setStartSize(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                animator.setStartSize(seekBar.getProgress());
            }
        });
        spacingBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    spacingEdit.setText(Integer.toString(progress));
                } else {
                    animator.setSpacing(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                animator.setSpacing(seekBar.getProgress());
            }
        });
        durationBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    durationEdit.setText(Integer.toString(progress));
                } else {
                    animator.setAnimationDuration(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                animator.setAnimationDuration(seekBar.getProgress());
            }
        });
        opacityBar.setOnSeekBarChangeListener(new MySeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    opacityEdit.setText(Integer.toString(progress));
                } else {
                    animator.setOpacity(progress);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                animator.setOpacity(seekBar.getProgress());
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

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sizeEdit.addTextChangedListener(new MyTextWatcher(sizeBar));
        spacingEdit.addTextChangedListener(new MyTextWatcher(spacingBar));
        durationEdit.addTextChangedListener(new MyTextWatcher(durationBar));
        opacityEdit.addTextChangedListener(new MyTextWatcher(opacityBar));

        shapeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    animator = Animator.changeType(animator, Animator.Type.Line);
                } else {
                    animator = Animator.changeType(animator, Animator.Type.Circle);
                    spacingEdit.setText(String.valueOf((int) animator.getSpacing()));
                }
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
    }
}