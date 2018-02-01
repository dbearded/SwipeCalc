package com.example.sputnik.gesturecalc.data;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Sputnik on 1/24/2018.
 */

class AdditionOperator extends BinaryOperator {

    AdditionOperator(){
        symbol = MathSymbol.PLUS;
        precedence = ExpressionPrecedence.LOWEST;
        mathContext = new MathContext(0);
    }

    @Override
    BigDecimal operate(BigDecimal leftOperand, BigDecimal rightOperand) {
        return leftOperand.add(rightOperand, mathContext);
    }

    @Override
    void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }
}