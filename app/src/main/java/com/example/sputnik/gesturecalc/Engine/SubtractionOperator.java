package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/24/2018.
 */

class SubtractionOperator extends BinaryOperation {

    static {
        symbol = MathSymbol.MINUS;
        precedence = ExpressionPrecedence.LOW;
    }

    @Override
    public BigDecimal operate() {
        return leftOperand.subtract(rightOperand);
    }
}