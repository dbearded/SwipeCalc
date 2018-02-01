package com.example.sputnik.gesturecalc.data;

/**
 * Created by Sputnik on 1/25/2018.
 */

class UnaryOperatorFactory {
    UnaryOperator getUnaryOperator(MathSymbol symbol){
        switch (symbol){
            case MINUS:
            case NEGATE:
                return new NegationOperator(UnaryOperator.UnaryType.PRE);
            case PERCENT:
                return new PercentOperator(UnaryOperator.UnaryType.POST);
            default:
                return null;
        }
    }
}