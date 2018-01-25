package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/24/2018.
 */

class AdditionOperator extends BinaryOperation{

    static {
        symbol = MathSymbol.PLUS;
        precedence = ExpressionPrecedence.LOW;
    }

    @Override
    public BigDecimal operate() {
        return leftOperand.add(rightOperand);
    }
}
