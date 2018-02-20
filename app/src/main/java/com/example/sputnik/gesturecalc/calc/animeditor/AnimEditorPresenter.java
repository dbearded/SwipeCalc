package com.example.sputnik.gesturecalc.calc.animeditor;

import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.SwitchCompat;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.example.sputnik.gesturecalc.util.ButtonGridCompat;
import com.example.sputnik.gesturecalc.util.PathActivator;
import com.example.sputnik.gesturecalc.anim.CircleAnimator;
import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.data.Expression;
import com.example.sputnik.gesturecalc.data.MathSymbol;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 1/31/2018.
 */

class AnimEditorPresenter implements Observer, AnimEditorContract.Presenter {
    private Expression expression;
    private AnimEditorContract.View view;

    public AnimEditorPresenter(AnimEditorContract.View view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void clear() {
        expression.clear();
        view.updateDisplay("");
        view.updatePreview("");
    }

    @Override
    public void start() {
        expression = new Expression();
        expression.addObserver(this);
    }

    // Updates model when a button is pressed
    public void addNewValue(String symbol){
        switch(symbol){
            case "C":
                clear();
                break;
            case "=":
                view.updateDisplay(expression.getValue());
                view.updatePreview("");
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

    @Override
    public void update(Observable o, Object arg) {
        view.updateDisplay(expression.toStringGroupingAsInputted());
        view.updatePreview(expression.getValue());
    }
}