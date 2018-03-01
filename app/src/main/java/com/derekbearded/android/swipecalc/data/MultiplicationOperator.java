package com.derekbearded.android.swipecalc.data;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Sputnik on 1/24/2018.
 */

class MultiplicationOperator extends BinaryOperator {

    MultiplicationOperator(){
        symbol = MathSymbol.MULTIPLY;
        precedence = ExpressionPrecedence.LOW;
        mathContext = new MathContext(0);
    }

    @Override
    public BigDecimal operate(BigDecimal leftOperand, BigDecimal rightOperand) {
        return leftOperand.multiply(rightOperand, mathContext);
    }

    @Override
    void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }
}
