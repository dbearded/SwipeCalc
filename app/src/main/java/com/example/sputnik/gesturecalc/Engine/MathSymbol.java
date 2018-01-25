package com.example.sputnik.gesturecalc.Engine;

/**
 * Created by Sputnik on 1/23/2018.
 */

public enum MathSymbol {
    ZERO("0"), ONE("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"),
    NINE("9"), PLUS("+"), MINUS("-"), MULTIPLY("*"), DIVIDE("/"), PERCENT("%"), NEGATE("!"),
    PARENTHESIS("()"), DECIMAL(".");

    private final String symbol;

    MathSymbol(String symbol){
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}