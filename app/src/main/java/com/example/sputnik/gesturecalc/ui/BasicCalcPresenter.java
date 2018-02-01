package com.example.sputnik.gesturecalc.ui;

import android.view.View;

import com.example.sputnik.gesturecalc.data.Expression;
import com.example.sputnik.gesturecalc.data.MathSymbol;

/**
 * Created by Sputnik on 1/31/2018.
 */

public class BasicCalcPresenter {
    private Expression expression;
    private View view;

    // Updates model when a button is pressed
    void buttonPressed(String symbol){
        // TODO if symbol is clear, then clear expression

        expression.add(MathSymbol.fromString(symbol));
    }

    // Updates the display on the view
    void updateDisplay(){

    }

    // Updates the preview on the view
    void updatePreview(){

    }
}
