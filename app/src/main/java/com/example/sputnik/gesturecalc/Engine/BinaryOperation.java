package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/23/2018.
 */

abstract class BinaryOperation implements Operator {

    protected BigDecimal leftOperand;
    protected BigDecimal rightOperand;
    // TODO will this cause every subclass to be the same?
    protected static MathSymbol symbol;
    protected static ExpressionPrecedence precedence;

    enum Operand {
        LEFT, RIGHT
    }

    BinaryOperation(){
    }

    @Override
    public String toString() {
        return symbol.toString();
    }

    BinaryOperation(BigDecimal leftOperand, BigDecimal rightOperand){
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
    }

    boolean isEmpty(Operand operand) {
        boolean result = false;
        switch (operand){
            case LEFT:
                result = leftOperand == null;
                break;
            case RIGHT:
                result = rightOperand == null;
                break;
        }

        return result;
    }

    void setOperand(Operand operand, BigDecimal number) {
        switch (operand){
            case LEFT:
                leftOperand = number;
                break;
            case RIGHT:
                rightOperand = number;
                break;
        }
    }

    BigDecimal getOperand(Operand operand) {
        switch (operand){
            case LEFT:
                return leftOperand;
            case RIGHT:
                return rightOperand;
            default:
                return null;
        }
    }

    BigDecimal removeOperand(Operand operand) {
        BigDecimal number;
        switch (operand){
            case LEFT:
                number = leftOperand;
                leftOperand = null;
                break;
            case RIGHT:
                number = rightOperand;
                rightOperand = null;
                break;
            default:
                number = null;
        }
        return number;
    }
}
