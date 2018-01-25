package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/24/2018.
 */

class NegationOperator extends UnaryOperation {

    static {
        symbol = MathSymbol.NEGATE;
        precedence = ExpressionPrecedence.MEDIUM;
    }

    @Override
    public BigDecimal operate() {
        return operand.negate();
    }
}
