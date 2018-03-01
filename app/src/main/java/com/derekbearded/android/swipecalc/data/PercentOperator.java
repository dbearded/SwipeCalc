package com.derekbearded.android.swipecalc.data;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Sputnik on 1/24/2018.
 */

class PercentOperator extends UnaryOperator {

    PercentOperator(UnaryType unaryType) {
        super(unaryType);
        symbol = MathSymbol.PERCENT;
        precedence = ExpressionPrecedence.MEDIUM;
        mathContext = new MathContext(0);
    }

    @Override
    public BigDecimal operate(BigDecimal operand) {
        return operand.divide(new BigDecimal(100.00), mathContext);
    }

    @Override
    void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }
}