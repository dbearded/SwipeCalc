package com.example.sputnik.gesturecalc.data;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Created by Sputnik on 1/24/2018.
 */

class NegationOperator extends UnaryOperator {

    NegationOperator(UnaryType unaryType) {
        super(unaryType);
        symbol = MathSymbol.NEGATE;
        precedence = ExpressionPrecedence.MEDIUM;
        mathContext = new MathContext(0);
    }

    @Override
    public BigDecimal operate(BigDecimal operand) {
        return operand.negate();
    }

    @Override
    void setMathContext(MathContext mathContext) {
        this.mathContext = mathContext;
    }
}
