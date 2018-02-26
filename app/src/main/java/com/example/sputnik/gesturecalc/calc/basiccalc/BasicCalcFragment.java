package com.example.sputnik.gesturecalc.calc.basiccalc;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.anim.Animator;
import com.example.sputnik.gesturecalc.calc.animeditor.AnimEditorActivity;
import com.example.sputnik.gesturecalc.util.ButtonGrid;
import com.example.sputnik.gesturecalc.util.PathActivator;
import com.example.sputnik.gesturecalc.util.Util;

/**
 * Created by Sputnik on 2/16/2018.
 */

public class BasicCalcFragment extends Fragment implements BasicCalcContract.View, SharedPreferences.OnSharedPreferenceChangeListener {

    private BasicCalcContract.Presenter presenter;
    private Animator animator;
    private TextView preview;
    private EditText display;
    private SharedPreferences sharedPref;
    private ButtonGrid buttonGrid;
    private ImageView delete;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Util.updateAnimatorSettingsOnChange(animator, sharedPreferences, key);
    }

    @Override
    public void setPresenter(BasicCalcContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_basic_calc, container, false);

        buttonGrid = root.findViewById(R.id.gridLayout);
        display = root.findViewById(R.id.display);
        preview = root.findViewById(R.id.preview);
        delete = root.findViewById(R.id.delete);
        Button designEditor = root.findViewById(R.id.buttonAnimEditor);

        PathActivator activator = new PathActivator();

        final ViewTreeObserver viewTreeObserver = ((ViewGroup) buttonGrid).getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    ((ViewGroup) buttonGrid).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    ((ViewGroup) buttonGrid).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                // layout will happen after first onResume() so this won't cause a NPE
                animator.setCanvasSize(((ViewGroup) buttonGrid).getWidth(), ((ViewGroup) buttonGrid).getHeight());
            }
        });
        buttonGrid.setPathActivator(activator);

        presenter.start();
        buttonGrid.registerButtonListener(new ButtonGrid.ButtonListener() {
            @Override
            public void buttonPressed(String input) {
                presenter.addNewValue(input);
            }
        });

        designEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AnimEditorActivity.class);
                startActivity(intent);
            }
        });

        sharedPref = Util.getSharedPrefSettings(getActivity());

        delete.setClickable(true);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(10);
                presenter.delete();
            }
        });

        // Works for >= 21 API
        display.setShowSoftInputOnFocus(false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        animator = Animator.changeSettings(animator, Util.loadAnimatorSettings(getActivity()));
        buttonGrid.setPathAnimator(animator);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        Util.saveAnimatorSettings(getActivity(), animator.getSettings());
        super.onPause();
    }

    @Override
    public void updateDisplay(String expression) {
        display.setText(expression);
        display.append("");
    }

    @Override
    public void updatePreview(String expression) {
        preview.setText(expression);
    }
}
