package com.derekbearded.android.swipecalc.data;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Sputnik on 1/23/2018.
 */

abstract class BinaryOperator {

    // TODO will this cause every subclass to be the same?
    protected MathSymbol symbol;
    protected ExpressionPrecedence precedence;
    protected MathContext mathContext;

    @Override
    public String toString() {
        return symbol.toString();
    }

    abstract BigDecimal operate(BigDecimal leftOperand, BigDecimal rightOperand);

    abstract void setMathContext(MathContext mathContext);
}
