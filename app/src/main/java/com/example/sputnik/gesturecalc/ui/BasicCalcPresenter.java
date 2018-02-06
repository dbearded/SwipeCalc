package com.example.sputnik.gesturecalc.ui;

import android.widget.TextView;

import com.example.sputnik.gesturecalc.MainActivity;
import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.data.Expression;
import com.example.sputnik.gesturecalc.data.MathSymbol;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 1/31/2018.
 */

public class BasicCalcPresenter implements Observer {
    private Expression expression;
    private MainActivity activity;
    private TextView display;
    private TextView preview;

    public BasicCalcPresenter(MainActivity activity){
        this.activity = activity;
        display = activity.findViewById(R.id.display);
        preview = activity.findViewById(R.id.preview);
        expression = new Expression();
        expression.addObserver(this);
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
            case "\u2212":
                expression.add(MathSymbol.fromString("-"));
                break;
            case "\u00d7":
                expression.add(MathSymbol.fromString("*"));
                break;
            case "\u00f7":
                expression.add(MathSymbol.fromString("/"));
                break;
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
