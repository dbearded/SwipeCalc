package com.example.sputnik.gesturecalc.ui;

import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.DesignerActivity;
import com.example.sputnik.gesturecalc.MainActivity;
import com.example.sputnik.gesturecalc.MyLayout;
import com.example.sputnik.gesturecalc.PathAnimator;
import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.data.Expression;
import com.example.sputnik.gesturecalc.data.MathSymbol;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 1/31/2018.
 */

public class DesignerCalcPresenter implements Observer {
    private Expression expression;
    private DesignerActivity activity;
    private TextView display;
    private TextView preview;
    private MyLayout layout;
    private AppCompatEditText sizeEdit, spacingEdit, durationEdit, opacityEdit;
    private AppCompatSeekBar sizeBar, spacingBar, durationBar, opacityBar;
    private PathAnimator animator;

    public DesignerCalcPresenter(DesignerActivity activity){
        this.activity = activity;
        layout = activity.findViewById(R.id.gridLayout);
        display = activity.findViewById(R.id.display);
        preview = activity.findViewById(R.id.preview);
        expression = new Expression();
        expression.addObserver(this);
        sizeEdit = activity.findViewById(R.id.editNumberSize);
        spacingEdit = activity.findViewById(R.id.editNumberSpacing);
        durationEdit = activity.findViewById(R.id.editNumberDuration);
        opacityEdit = activity.findViewById(R.id.editNumberOpacity);
        sizeBar = activity.findViewById(R.id.seekBarSize);
        spacingBar = activity.findViewById(R.id.seekBarSpacing);
        durationBar = activity.findViewById(R.id.seekBarDuration);
        opacityBar = activity.findViewById(R.id.seekBarOpacity);

        animator = new PathAnimator();
        layout.setPathAnimator(animator);
        float size = animator.getCIRCLE_START_DIAMETER();
        sizeEdit.setText(Float.toString(size));
        sizeBar.setProgress((int) size);
        float distance = animator.getCIRCLE_CENTER_DISTANCE();
        spacingEdit.setText(Float.toString(distance));
        spacingBar.setProgress((int) distance);
        long duration = animator.getANIMATION_DURATION();
        durationEdit.setText(Long.toString(duration));
        durationBar.setProgress((int) duration);
        int opacity = animator.getOPACITY();
        opacityEdit.setText(Integer.toString(opacity));
        opacityBar.setProgress(opacity);
    }

    // Updates model when a button is pressed
    public void buttonPressed(String symbol){
        switch(symbol){
            case "C":
                expression.clear();
                updateDisplay("");
                updatePreview("");
                break;
            case "=":
                updateDisplay(expression.getValue());
                updatePreview("");
                break;
            case "\u00b1":
                expression.add(MathSymbol.fromString("\u00af"));
            case "()":
                expression.add(MathSymbol.fromString("("));
                break;
            default:
                expression.add(MathSymbol.fromString(symbol));
                break;
        }
    }

    public void settingsChanged(){
        String duration = durationEdit.getText().toString();
        if (duration.isEmpty()) {
            return;
        }
        animator.setANIMATION_DURATION(Integer.parseInt(duration));
        String distance = spacingEdit.getText().toString();
        if (distance.isEmpty()) {
            return;
        }
        animator.setCIRCLE_CENTER_DISTANCE(Float.parseFloat(distance));
        String diameter = sizeEdit.getText().toString();
        if (diameter.isEmpty()) {
            return;
        }
        animator.setCIRCLE_START_DIAMETER(Float.parseFloat(diameter));
        String opacity = opacityEdit.getText().toString();
        if (opacity.isEmpty()) {
            return;
        }
        animator.setOPACITY(Integer.parseInt(opacity));
    }

    @Override
    public void update(Observable o, Object arg) {
        updateDisplay(expression.toStringGroupingAsInputted());
        updatePreview(expression.getValue());
    }

    // Updates the display on the view
    void updateDisplay(String input){
        if (input == null){
            input = "";
        }
        display.setText(input);
    }

    // Updates the preview on the view
    void updatePreview(String value){
        if (value == null){
            value = "";
        }
        preview.setText(value);
    }
}
