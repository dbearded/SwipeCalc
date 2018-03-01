package com.derekbearded.android.swipecalc.data;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Sputnik on 1/24/2018.
 */

class SubtractionOperator extends BinaryOperator {

    SubtractionOperator(){
        symbol = MathSymbol.MINUS;
        precedence = ExpressionPrecedence.LOWEST;
        mathContext = new MathContext(0);
    }

    @Override
    public BigDecimal operate(BigDecimal leftOperand, BigDecimal rightOperand) {
        return leftOperand.subtract(rightOperand, mathContext);
    }

    @Override
    void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }
}