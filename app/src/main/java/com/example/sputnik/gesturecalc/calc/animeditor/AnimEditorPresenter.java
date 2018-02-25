package com.example.sputnik.gesturecalc.calc.animeditor;

import com.example.sputnik.gesturecalc.data.Expression;
import com.example.sputnik.gesturecalc.data.MathSymbol;
import com.example.sputnik.gesturecalc.util.PathActivator;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Sputnik on 1/31/2018.
 */

class AnimEditorPresenter implements Observer, AnimEditorContract.Presenter {
    private static final String CAKE = "84302253047020543"; // "the cake is a lie"
    private StringBuilder builder = new StringBuilder();
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
                clearCake();
                break;
            case "=":
                if (!expression.getValue().isEmpty()) {
                    expression.clear(true, false);
                    view.updateDisplay(expression.getValue());
                    view.updatePreview("");
                }
                clearCake();
                break;
            case "\u00b1":
                expression.add(MathSymbol.fromString("\u00af"));
                clearCake();
            case "()":
                expression.add(MathSymbol.fromString("("));
                clearCake();
                break;
            default:
                expression.add(MathSymbol.fromString(symbol));
                checkCake(symbol);
                break;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        view.updateDisplay(expression.toStringGroupingAsInputted());
        view.updatePreview(expression.getValue());
    }

    private void checkCake(String symbol){
        builder.append(symbol);
        int comparison = builder.length() - CAKE.length();
        if (comparison == 0){
            if (builder.toString().equals(CAKE)){
                view.showDevOpts();
            } else {
                clearCake();
            }
        }
    }

    private void clearCake(){
        builder.setLength(0);
    }
}