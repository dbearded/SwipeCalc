package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/24/2018.
 */

class DivisionOperator extends BinaryOperation {

    static {
        symbol = MathSymbol.DIVIDE;
        precedence = ExpressionPrecedence.MEDIUM;
    }

    @Override
    public BigDecimal operate() {
        return leftOperand.divide(rightOperand, BigDecimal.ROUND_FLOOR);
    }
}