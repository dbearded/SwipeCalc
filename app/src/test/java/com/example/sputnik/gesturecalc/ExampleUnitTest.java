package com.example.sputnik.gesturecalc;

import com.example.sputnik.gesturecalc.data.Expression;
import com.example.sputnik.gesturecalc.data.MathSymbol;

import org.junit.ComparisonFailure;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Example local unit TestInterface, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void first_number_node_isCorrect() throws Exception{
        assertEquals((new BigDecimal(2)).toString(), Expression.evaluateInput("2"));
    }

    @Test
    public void first_operator_node_isCorrect() throws Exception{
        Expression expression = new Expression();
        expression.add(MathSymbol.MINUS);
        assertEquals("-", expression.toString());
    }

    @Test
    public void enumset_tests() throws Exception{
        assertTrue(Expression.numerals.contains(MathSymbol.TWO));
        MathSymbol[] symbols = Expression.stringInputToMathSymbols(".");
        assertTrue(Expression.numerals.contains(symbols[0]));
    }

    @Test
    public void two_integer_String_isCorrect() throws Exception{
        Expression expression = new Expression();
        expression.add(MathSymbol.ONE);
        expression.add(MathSymbol.TWO);
        assertEquals("12", expression.toString());
        assertEquals((new BigDecimal("12")).toString(), expression.getValue());
    }

    @Test
    public void n_integer_isCorrect() throws Exception{
        assertEquals("112345678900", Expression.inputToString("112345678900"));
        assertEquals((new BigDecimal("112345678900")).toString(), Expression.evaluateInput("112345678900"));
    }

    @Test
    public void leading_zero_test() throws Exception{
        Expression expression = new Expression();
        expression.add(Expression.stringInputToMathSymbols("0"));
        assertEquals("0", expression.toString());
        assertEquals((new BigDecimal(0)).toString(), expression.getValue());

        expression.add(Expression.stringInputToMathSymbols("0"));
        // This should FAIL
//        assertEquals("00", expression.toString());

        expression.add(Expression.stringInputToMathSymbols(".0"));
        assertEquals("0", expression.toString());

        expression.add(Expression.stringInputToMathSymbols(".01"));
        assertEquals("0.001", expression.toString());
    }

    @Test
    public void decimal_number_tests() throws Exception{
        Expression expression = new Expression();
        expression.add(Expression.stringInputToMathSymbols("1."));
        assertNotEquals("1.", (new BigDecimal("1.").toString()));
        assertNotEquals("1.", expression.toString());

        expression.add(Expression.stringInputToMathSymbols("0"));
        assertEquals("1", expression.toString());

        expression.add(Expression.stringInputToMathSymbols("."));
        assertNotEquals("1.0.", expression.toString());
        assertEquals("1", expression.getValue());
    }

    @Test
    public void two_integer_addition_isCorrect() throws Exception{
        assertEquals((new BigDecimal(4)).toString(), Expression.evaluateInput("2+2"));
        assertEquals((new BigDecimal(5)).toString(), Expression.evaluateInput("2+3"));
    }

    @Test
    public void two_decimal_addition_isCorrect() throws Exception{
        assertEquals("0.1+0.1", Expression.inputToString("0.1+0.1"));
        assertEquals((new BigDecimal("0.2")).toString(), Expression.evaluateInput("0.1+0.1"));
    }

    @Test
    public void n_integer_addition_isCorrect() throws Exception {
        assertEquals((new BigDecimal("55")).toString(), Expression.evaluateInput("0+1+2+3+4+5+6+7+8+9+10"));
        assertEquals("0+1+2+3+4+5+6+7+8+9+10", Expression.inputToString("0+1+2+3+4+5+6+7+8+9+10"));
        assertEquals((new BigDecimal("1021")).toString(), Expression.evaluateInput("1+20+467+533"));
    }

    @Test
    public void n_decimal_addition_isCorrect() throws Exception{
        assertEquals((new BigDecimal(".111111")).toString(), Expression.evaluateInput("0+.1+.01+.001+.0001+.00001+.000001"));
        assertEquals("0+0.1+0.01+0.001+0.0001+0.00001+0.000001", Expression.inputToString("0+.1+.01+.001+.0001+.00001+.000001"));
        assertEquals((new BigDecimal("1435.14159")).toString(), Expression.evaluateInput("1432+3.14159"));
        assertEquals("1432+3.14159", Expression.inputToString("1432+3.14159"));
    }

    @Test
    public void two_integer_subtraction_isCorrect() throws Exception{
        assertEquals((new BigDecimal("3")).toString(), Expression.evaluateInput("4-1"));
        assertEquals((new BigDecimal("14")).toString(), Expression.evaluateInput("1015-1001"));
    }

    @Test
    public void n_integer_subtraction_isCorrect() throws Exception{
        assertEquals((new BigDecimal("3")).toString(), Expression.evaluateInput("10-1-2-3-1"));
    }

    @Test
    public void two_decimal_subtraction_isCorrect() throws Exception{
        assertEquals("9.3", Expression.evaluateInput("10.-.7"));
    }

    @Test
    public void n_decimal_subtraction_isCorrect() throws Exception{
        Random random = new Random();
        int size = random.nextInt(99) + 1;
        double[] input = new double[size];
        StringBuilder builder = new StringBuilder(size*2-1);
        MathContext mathContext = new MathContext(14, RoundingMode.HALF_EVEN);
        BigDecimal result = new BigDecimal(0, mathContext);
        DecimalFormat decimalFormat = new DecimalFormat("0.##############");
        String expressionOutput = null;
        for (int i = 0; i < input.length; i++) {
            input[i] = ((double) random.nextInt(1000)) * random.nextDouble();
            builder.append(input[i]);
            if (i != (input.length - 1)) {
                builder.append("-");
            }
            if (i == 0){
                result = new BigDecimal(Double.toString(input[i]),mathContext);
            } else {
                result = result.subtract(new BigDecimal(Double.toString(input[i]),mathContext), mathContext);
            }
        }
        try {
            expressionOutput = Expression.evaluateInput(builder.toString());
            assertEquals(decimalFormat.format(result), expressionOutput);
        } catch (AssertionError cf){
            throw new ComparisonFailure(cf.getMessage() + "TestInterface did not work with given input: " + builder.toString(), result.toString(), expressionOutput);
        }
    }

    @Test
    public void n_decimal_multiplication_isCorrect() throws Exception{
        Random random = new Random();
        int size = random.nextInt(19) + 1;
        double[] input = new double[size];
        StringBuilder builder = new StringBuilder(size*2-1);
        MathContext mathContext = new MathContext(14, RoundingMode.HALF_EVEN);
        BigDecimal result = new BigDecimal(0, mathContext);
        DecimalFormat decimalFormat = new DecimalFormat("0.##############");
        String expressionOutput = null;
        for (int i = 0; i < input.length; i++) {
            input[i] = ((double) random.nextInt(10)) * random.nextDouble()+0.01;
            builder.append(input[i]);
            if (i != (input.length - 1)) {
                builder.append("*");
            }
            if (i == 0){
                result = new BigDecimal(Double.toString(input[i]), mathContext);
            } else {
                result = result.multiply(new BigDecimal(Double.toString(input[i]), mathContext), mathContext);
            }
        }
        try {
            expressionOutput = Expression.evaluateInput(builder.toString());
            assertEquals(decimalFormat.format(result), expressionOutput);
        } catch (AssertionError cf){
            throw new ComparisonFailure(cf.getMessage() + "TestInterface did not work with given input: " + builder.toString(), result.toString(), expressionOutput);
        }
    }

    @Test
    public void n_decimal_division_isCorrect() throws Exception{
        Random random = new Random();
        int size = random.nextInt(19)+1;
        double[] input = new double[size];
        StringBuilder builder = new StringBuilder(size*2-1);
        MathContext mathContext = new MathContext(14, RoundingMode.HALF_EVEN);
        BigDecimal result = new BigDecimal(0, mathContext);
        String expressionOutput = null;
        DecimalFormat decimalFormat = new DecimalFormat("0.##############");
        for (int i = 0; i < input.length; i++) {
            input[i] = ((double) random.nextInt(10)) * random.nextDouble()+0.01;
            builder.append(input[i]);
            if (i != (input.length - 1)) {
                builder.append("/");
            }
            if (i == 0){
                result = new BigDecimal(Double.toString(input[i]),mathContext);
            } else {
                result = result.divide(new BigDecimal(Double.toString(input[i]),mathContext),mathContext);
            }
        }
        try {
            expressionOutput = Expression.evaluateInput(builder.toString());
            assertEquals(decimalFormat.format(result), expressionOutput);
        } catch (AssertionError cf){
            throw new ComparisonFailure(cf.getMessage() + " TestInterface did not work with given input: " + builder.toString(), result.toString(), expressionOutput);
        } catch (Exception e){
            throw new Exception("TestInterface did not work. Expected output: " + result.toString() + " input: " + builder.toString(), e);
        }
    }

    @Test
    public void n_element_subtraction_isCorrect() throws Exception{
        assertEquals("10-0.5-3-15-0.001", Expression.inputToString("10-0.5-3-15-0.001"));
        assertEquals("-8.501", Expression.evaluateInput("10-0.5-3-15-0.001"));
        assertEquals("-8.501", Expression.evaluateInput("10-.5-3-15-.001"));
    }

    @Test
    public void n_element_addition_isCorrect() throws Exception{
        assertEquals("10+0.5+3+1+0.001", Expression.inputToString("10+0.5+3+1+0.001"));
        assertEquals("14.501", Expression.evaluateInput("10+0.5+3+1+0.001"));
        assertEquals("14.501", Expression.evaluateInput("10+.5+3+1+.001"));
    }

    @Test
    public void n_element_multiplication_isCorrect() throws Exception{
        assertEquals("10*0.5*3*1*0.001", Expression.inputToString("10*0.5*3*1*0.001"));
        assertEquals("0.015", Expression.evaluateInput("10*0.5*3*1*0.001"));
        assertEquals("0.015", Expression.evaluateInput("10*.5*3*1*.001"));
    }

    @Test
    public void percent_isCorrect() throws Exception{
        assertEquals("0.01", Expression.evaluateInput("1%"));
        assertEquals("5", Expression.evaluateInput("500%"));
        assertEquals("0.1032", Expression.evaluateInput("10.32%"));
        assertEquals("0.001032", Expression.evaluateInput("10.32%%"));
        assertEquals("5%", Expression.inputToString("5%"));
        assertEquals("5%%", Expression.inputToString("5.0%%"));
    }

    @Test
    public void negation_isCorrect() throws Exception{
        assertEquals("-1", Expression.evaluateInput("-1"));
        assertEquals("-5", Expression.evaluateInput("-5.00"));
        assertEquals("-3.14", Expression.evaluateInput("-3.14"));
        assertEquals("3.14", Expression.evaluateInput("--3.14"));
        assertEquals("-3.14", Expression.evaluateInput("---3.14"));
    }

    @Test
    public void multiplication_addition_isCorrect() throws Exception{
        assertEquals("14", Expression.evaluateInput("3*4+2"));
        assertEquals("14", Expression.evaluateInput("2+4*3"));
        assertEquals("37", Expression.evaluateInput("6+5*3*2+1"));
    }

    @Test
    public void addition_subtraction_isCorrect() throws Exception{
        assertEquals("14", Expression.evaluateInput("15+2-3"));
    }

    @Test
    public void multiplication_subtraction_isCorrect() throws Exception{
        assertEquals("27", Expression.evaluateInput("15*2-3"));
        assertEquals("9", Expression.evaluateInput("15-2*3"));
    }

    @Test
    public void addition_negation_isCorrect() throws Exception{
        assertEquals("-2", Expression.evaluateInput("-3+5+-4"));
    }

    @Test
    public void non_grouped_expression_isCorrect() throws Exception{
        assertEquals("-98.15", Expression.evaluateInput("-3.1+5+-4*26-5%+-1+25/5"));
        assertEquals("109.05", Expression.evaluateInput("3+5*6+4*23-14+-2+5%"));
        assertEquals("2", Expression.evaluateInput("1--1"));
        assertEquals("2", Expression.evaluateInput("1----1"));
    }

    @Test
    public void delete_isCorrect() throws Exception{
        Expression expression = new Expression();
        MathSymbol[] symbols = Expression.stringInputToMathSymbols("-3.1+5+-4*26-5%+-1+25/5");
        expression.add(symbols);
        assertEquals("-3.1+5+-4*26-5%+-1+25/5", expression.toString());
        assertEquals("-98.15", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5%+-1+25/", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5%+-1+25", expression.toString());
        assertEquals("-78.15", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5%+-1+2", expression.toString());
        assertEquals("-101.15", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5%+-1+", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5%+-1", expression.toString());
        assertEquals("-103.15", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5%+-", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5%+", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5%", expression.toString());
        assertEquals("-102.15", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-5", expression.toString());
        assertEquals("-107.1", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26-", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*26", expression.toString());
        assertEquals("-102.1", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*2", expression.toString());
        assertEquals("-6.1", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4*", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-4", expression.toString());
        assertEquals("-2.1", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+-", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5+", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1+5", expression.toString());
        assertEquals("1.9", expression.getValue());
        expression.delete();
        assertEquals("-3.1+", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-3.1", expression.toString());
        assertEquals("-3.1", expression.getValue());
        expression.delete();
        assertEquals("-3", expression.toString());
        assertEquals("-3", expression.getValue());
        expression.delete();
        assertEquals("-", expression.toString());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("", expression.toString());
        assertEquals("", expression.getValue());

        expression.add(Expression.stringInputToMathSymbols("0.003"));
        expression.delete();
        assertEquals("0", expression.toString());
        expression.add(Expression.stringInputToMathSymbols("3"));
        assertEquals("0.003", expression.toString());
        assertEquals("0.003", expression.getValue());
    }

    @Test
    public void grouped_expression_isCorrect() throws Exception{
        assertEquals("4", Expression.evaluateInput("(1)+(3)"));
        assertEquals("4", Expression.evaluateInput("(1+3)"));
        assertEquals("8", Expression.evaluateInput("(1+3)*2"));
        assertEquals("7", Expression.evaluateInput("(1+3*2"));
        assertEquals("13.14", Expression.evaluateInput("3.14+5*2"));
        assertEquals("16.28", Expression.evaluateInput("(3.14+5)*2"));
        assertEquals("(3.14+5)*2", Expression.inputToString("(3.14+5)*2"));
        assertEquals("(3.14+5)*2", Expression.inputToString("(((3.14+5)*2"));
        assertEquals("(3.14+5)*2", Expression.inputToString("(((3.14+5)*2))"));
        assertEquals("((3.14+5)*2)*", Expression.inputToString("(((3.14+5)*2))))))"));
        assertEquals("16.28", Expression.evaluateInput("(((3.14+5)*2))"));
        assertEquals("-8", Expression.evaluateInput("-(6+2"));
        assertEquals("1632", Expression.evaluateInput("-(6+2)*17(-(2+3*(1-2)+13))"));
    }

    @Test
    public void grouping_toString_isCorrect(){
        Expression expression = new Expression("((((6+2");
        assertEquals("((((6+2", expression.toStringGroupingAsInputted());
        assertEquals("6+2", expression.toString());

        Expression expression2 = new Expression("((((6+2))");
        assertEquals("((((6+2))", expression2.toStringGroupingAsInputted());
        assertEquals("6+2", expression2.toString());

        Expression expression3 = new Expression("6+2)))");
        assertEquals("6+2*(((", expression3.toStringGroupingAsInputted());
        assertEquals("6+2*", expression3.toString());

        Expression expression4 = new Expression("6+(((2-14");
        assertEquals("6+(((2-14", expression4.toStringGroupingAsInputted());
        assertEquals("6+(2-14)", expression4.toString());

        Expression expression5 = new Expression("(((6+2))*(5%-7");
        assertEquals("(((6+2))*(5%-7", expression5.toStringGroupingAsInputted());
        assertEquals("(6+2)*(5%-7)", expression5.toString());

        Expression expression6 = new Expression("(((3*5))+((2*7");
        assertEquals("(((3*5))+((2*7", expression6.toStringGroupingAsInputted());
        assertEquals("(3*5)+(2*7)", expression6.toString());
    }

    @Test
    public void grouped_delete_isCorrect(){
        Expression expression = new Expression("-(6+2)*17(-(2+3*(1-2)+13))");
        /*assertEquals("-(6+2)*17*(-(2+3*(1-2)+13))", expression.toString());
        assertEquals("1632", expression.getValue());*/
        expression.delete();
        /*assertEquals("-(6+2)*17*(-(2+3*(1-2)+13)", expression.toStringGroupingAsInputted());
        assertEquals("1632", expression.getValue());*/
        expression.delete();
        /*assertEquals("-(6+2)*17*(-(2+3*(1-2)+13", expression.toStringGroupingAsInputted());
        assertEquals("1632", expression.getValue());*/
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3*(1-2)+1", expression.toStringGroupingAsInputted());
        assertEquals("0", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3*(1-2)+", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3*(1-2)", expression.toStringGroupingAsInputted());
        assertEquals("-136", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3*(1-2", expression.toStringGroupingAsInputted());
        assertEquals("-136", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3*(1-", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3*(1", expression.toStringGroupingAsInputted());
        assertEquals("680", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3*(", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3*", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+3", expression.toStringGroupingAsInputted());
        assertEquals("680", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2+", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(2", expression.toStringGroupingAsInputted());
        assertEquals("272", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-(", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(-", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*(", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17*", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*17", expression.toStringGroupingAsInputted());
        assertEquals("-136", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*1", expression.toStringGroupingAsInputted());
        assertEquals("-8", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)*", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6+2)", expression.toStringGroupingAsInputted());
        assertEquals("-8", expression.getValue());
        expression.delete();
        assertEquals("-(6+2", expression.toStringGroupingAsInputted());
        assertEquals("-8", expression.getValue());
        expression.delete();
        assertEquals("-(6+", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-(6", expression.toStringGroupingAsInputted());
        assertEquals("-6", expression.getValue());
        expression.delete();
        assertEquals("-(", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("-", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
        expression.delete();
        assertEquals("", expression.toStringGroupingAsInputted());
        assertEquals("", expression.getValue());
    }
}