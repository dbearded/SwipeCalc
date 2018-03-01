package com.derekbearded.android.swipecalc.calc.basiccalc;

import com.derekbearded.android.swipecalc.data.Expression;
import com.derekbearded.android.swipecalc.data.MathSymbol;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 1/31/2018.
 */

class BasicCalcPresenter implements Observer, BasicCalcContract.Presenter {
    private Expression expression;
    private BasicCalcContract.View view;
    private boolean prevEquals;

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
                if (!expression.getValue().isEmpty()) {
                    expression.clear(true, false);
                    view.updateDisplay(expression.getValue());
                    view.updatePreview("");
                }
                prevEquals = true;
                break;
            case "\u00b1":
                // if plus-minus, then negate
                expression.add(MathSymbol.fromString("\u00af"));
                break;
            case "( )":
                expression.add(MathSymbol.fromString("("));
                break;
            case "+":
            case "\u2212":
                // minus
            case "\u00d7":
                // times
            case "\u00f7":
                // divide
            case "%":
                // catching all operators
                expression.add(MathSymbol.fromString(symbol));
                break;
            default:
                // numerals only here
                if (prevEquals){
                    expression.clear(false, false);
                    prevEquals = false;
                }
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
    public void delete() {
        expression.delete();
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
