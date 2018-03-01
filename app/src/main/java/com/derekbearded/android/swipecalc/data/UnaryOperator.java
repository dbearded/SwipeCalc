package com.derekbearded.android.swipecalc.data;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Sputnik on 1/23/2018.
 */

abstract class UnaryOperator {

    UnaryType unaryType;

    enum UnaryType {
        PRE, POST
    }

    protected MathSymbol symbol;
    protected ExpressionPrecedence precedence;
    protected MathContext mathContext;

    UnaryOperator(UnaryType unaryType){
        this.unaryType = unaryType;
    }

    @Override
    public String toString() {
        return symbol.toString();
    }

    abstract BigDecimal operate(BigDecimal operand);

    abstract void setMathContext(MathContext mathContext);
}