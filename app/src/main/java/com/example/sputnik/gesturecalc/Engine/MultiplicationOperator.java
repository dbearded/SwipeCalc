package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/24/2018.
 */

class MultiplicationOperator extends BinaryOperation {

    static {
        symbol = MathSymbol.MULTIPLY;
        precedence = ExpressionPrecedence.MEDIUM;
    }

    @Override
    public BigDecimal operate() {
        return leftOperand.multiply(rightOperand);
    }
}
