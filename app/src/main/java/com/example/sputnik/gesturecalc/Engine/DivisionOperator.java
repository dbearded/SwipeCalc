package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Sputnik on 1/24/2018.
 */

class DivisionOperator extends BinaryOperator {

    DivisionOperator(){
        symbol = MathSymbol.DIVIDE;
        precedence = ExpressionPrecedence.LOW;
        mathContext = new MathContext(0);
    }

    @Override
    public BigDecimal operate(BigDecimal leftOperand, BigDecimal rightOperand) {
        if (rightOperand.compareTo(new BigDecimal("0")) == 0){
            return null;
        }
        return leftOperand.divide(rightOperand, mathContext);
    }

    @Override
    void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }
}