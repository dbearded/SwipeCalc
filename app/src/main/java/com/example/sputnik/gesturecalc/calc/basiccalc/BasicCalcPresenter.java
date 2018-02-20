package com.example.sputnik.gesturecalc.calc.basiccalc;

import android.widget.TextView;

import com.example.sputnik.gesturecalc.R;
import com.example.sputnik.gesturecalc.data.Expression;
import com.example.sputnik.gesturecalc.data.MathSymbol;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 1/31/2018.
 */

class BasicCalcPresenter implements Observer, BasicCalcContract.Presenter {
    private Expression expression;
    private BasicCalcContract.View view;

    public BasicCalcPresenter(BasicCalcContract.View view){
        this.view = view;
        view.setPresenter(this);
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
    public void clear() {
        expression.clear();
        view.updateDisplay("");
        view.updatePreview("");
    }

    @Override
    public void setExpression(String expr) {
        if (expression == null) {
            start();
        } else {
            clear();
        }
        if (expr == null){
            expression.add(MathSymbol.ZERO);
        }
        char[] chars = expr.toCharArray();
        for (char c :
                chars) {
            addNewValue(String.valueOf(c));
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        view.updateDisplay(expression.toStringGroupingAsInputted());
        view.updatePreview(expression.getValue());
    }
}
