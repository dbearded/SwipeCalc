package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/23/2018.
 */

interface BinaryOperatorOld extends Operator {
    enum Operand {
        LEFT, RIGHT;
    }

    boolean isEmpty(Operand operand);
    boolean setOperand(Operand operand, BigDecimal number);
    BigDecimal getOperand(Operand operand);
    BigDecimal removeOperand(Operand operand);
}
