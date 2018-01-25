package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/23/2018.
 */

abstract class UnaryOperation implements Operator {

    protected BigDecimal operand;
    static protected MathSymbol symbol;
    static protected ExpressionPrecedence precedence;

    UnaryOperation(){
    }

    UnaryOperation(BigDecimal number){
        this.operand = number;
    }

    boolean isEmpty() {
        return operand == null;
    }

    void setOperand(BigDecimal operand) {
        this.operand = operand;
    }

    BigDecimal getOperand() {
        return operand;
    }

    BigDecimal removeNumber() {
        BigDecimal temp = operand;
        operand = null;
        return temp;
    }

    @Override
    public String toString() {
        return symbol.toString();
    }
}