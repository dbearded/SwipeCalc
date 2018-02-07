package com.example.sputnik.gesturecalc.data;

/**
 * Created by Sputnik on 1/23/2018.
 */

public enum MathSymbol {
    ZERO("0"), ONE("1"), TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"),
    NINE("9"), DECIMAL("."), PLUS("+"), MINUS("\u2212"), MULTIPLY("\u00d7"), DIVIDE("\u00f7"), PERCENT("%"), NEGATE("\u00af"),
    LEFT_PARENTHESIS("("), RIGHT_PARENTHESIS(")"), UNSPECIFIED_PARENTHESIS("()");

    private final String symbol;

    MathSymbol(String symbol){
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }

    public static MathSymbol fromString(String symbol) {

        for (MathSymbol s : MathSymbol.values()) {
            if (s.symbol.equalsIgnoreCase(symbol)) {
                return s;
            }
        }
        throw new IllegalArgumentException("No MathSymbol with text: " + symbol);
    }
}