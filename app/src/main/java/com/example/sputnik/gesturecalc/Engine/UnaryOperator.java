package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;
import java.util.function.BiFunction;

/**
 * Created by Sputnik on 1/24/2018.
 */

interface UnaryOperator {
    BigDecimal operate(BigDecimal number);
}
