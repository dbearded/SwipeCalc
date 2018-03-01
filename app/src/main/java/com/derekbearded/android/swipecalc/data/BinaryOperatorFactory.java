package com.derekbearded.android.swipecalc.data;

/**
 * Created by Sputnik on 1/25/2018.
 */

class BinaryOperatorFactory {
    BinaryOperator getBinaryOperator(MathSymbol symbol){
        switch (symbol){
            case PLUS:
                return new AdditionOperator();
            case MINUS:
                return new SubtractionOperator();
            case MULTIPLY:
                return new MultiplicationOperator();
            case DIVIDE:
                return new DivisionOperator();
            default:
                return null;
        }
    }
}