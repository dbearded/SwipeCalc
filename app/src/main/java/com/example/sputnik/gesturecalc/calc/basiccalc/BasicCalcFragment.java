package com.example.sputnik.gesturecalc.calc.basiccalc;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.anim.FactoryAnimator;
import com.example.sputnik.gesturecalc.anim.PathAnimator;
import com.example.sputnik.gesturecalc.calc.animeditor.AnimEditorActivity;
import com.example.sputnik.gesturecalc.util.ButtonGrid;
import com.example.sputnik.gesturecalc.util.PathActivator;

/**
 * Created by Sputnik on 2/16/2018.
 */

public class BasicCalcFragment extends Fragment implements BasicCalcContract.View {

    private BasicCalcContract.Presenter presenter;
    private ButtonGrid buttonGrid;
    private TextView display, preview;
    private PathAnimator animator;
    private PathActivator activator;
    private Button designEditor;

    @Override
    public void setPresenter(BasicCalcContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void onResume() {
        super.onResume();
        presenter.start();
        buttonGrid.registerButtonListener(new ButtonGrid.ButtonListener() {
            @Override
            public void buttonPressed(String input) {
                presenter.addNewValue(input);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_basic_calc, container, false);

        buttonGrid = root.findViewById(R.id.gridLayout);
        display = root.findViewById(R.id.display);
        preview = root.findViewById(R.id.preview);
        designEditor = root.findViewById(R.id.buttonAnimEditor);

        animator = FactoryAnimator.makeAnimator(FactoryAnimator.Type.Circle);
        activator = new PathActivator();

        final ViewTreeObserver viewTreeObserver = ((ViewGroup) buttonGrid).getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                    ((ViewGroup) buttonGrid).getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    ((ViewGroup) buttonGrid).getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                buttonGrid.setupSize();
                animator.setCanvasSize(((ViewGroup) buttonGrid).getWidth(), ((ViewGroup) buttonGrid).getHeight());
            }
        });
        buttonGrid.setPathActivator(activator);
        buttonGrid.setPathAnimator(animator);

        designEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),  AnimEditorActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }

    @Override
    public void updateDisplay(String expression) {
        display.setText(expression);
    }

    @Override
    public void updatePreview(String expression) {
        preview.setText(expression);
    }
}
