package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/24/2018.
 */

class PercentOperator extends UnaryOperation {

    static {
        symbol = MathSymbol.PERCENT;
        precedence = ExpressionPrecedence.MEDIUM;
    }

    @Override
    public BigDecimal operate() {
        return operand.divide(new BigDecimal(100.00), BigDecimal.ROUND_FLOOR);
    }
}
