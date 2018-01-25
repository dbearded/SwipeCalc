package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sputnik on 1/23/2018.
 */

public class Expression {
    private ExpressionNode head;
    private ExpressionNode last;
    private List<MathSymbol> numberBuilder = new ArrayList<>();

    enum Node {
        LEFT_CHILD, RIGHT_CHILD, PARENT
    }

    enum OperatorType {
        PRE_UNARY, POST_UNARY, BINARY
    }

    class ExpressionNode {
        private ExpressionNode leftChild;
        private ExpressionNode rightChild;
        private ExpressionNode parent;
//        private MathSymbol symbol;
        private Operator operator;
        private BigDecimal number;

        ExpressionNode(ExpressionNode parent, Operator operator){
            this.parent = parent;
            this.operator = operator;
        }

        // Evaluates child nodes and this node
        // TODO what about null nodes or null numbers in nodes?
        void evaluate(){
            // if operator is null, then current node is operand only and must be leaf
            if (operator == null){
                // TODO return? or make sure operand is not null?
//                return;
            }
            // if operator is unary, then only left child should be defined, right should be null
            if (operator instanceof BinaryOperation){
                leftChild.evaluate();
                ((BinaryOperation) operator).setOperand(BinaryOperation.Operand.LEFT, leftChild.number);
                rightChild.evaluate();
                ((BinaryOperation) operator).setOperand(BinaryOperation.Operand.RIGHT, rightChild.number);
                number = operator.operate();
            }

            // if operator is binary, then left and right should be defined
            if (operator instanceof UnaryOperation){
                leftChild.evaluate();
                ((UnaryOperation) operator).setOperand(leftChild.number);
                number = operator.operate();
            }
        }
    }

    public boolean append(MathSymbol symbol){
        boolean result;

        // First ExpressionNode
        // First node must be a numeral, positive, negative, or negation operator
        if (last == null){
            // TODO add in grouping symbols
            if (isNumeral(symbol)){
                numberBuilder.add(symbol);
                head = new ExpressionNode(null, null);
                head.number = new BigDecimal(numberBuilder.toString());
                last = head;
                result = true;
            } else if (isPreUnaryOperator(symbol)) {
                head = new ExpressionNode(null, constructOperator(symbol, OperatorType.PRE_UNARY));
                last = head;
                result = true;
            } else {
                    // invalid first input
                    return false;
                }
        } else {
            if (isNumeral(last.symbol)){
                if (isNumeral(symbol)){
                    last.rightChild = new ExpressionNode(last);
                    last = last.rightChild;
                    last.symbol = symbol;
                    evaluate(last);
                }
            } else if (isUnaryOperator(symbol)){

            }
        }

        return result;
    }

    private boolean evaluate(ExpressionNode node){
        boolean result;

        // TODO define method.

        return result;
    }

    private boolean evalatue(){
        return evaluate(last);
    }

    private boolean isNumeral(MathSymbol symbol){
        boolean result;
        switch (symbol){
            case ZERO:
            case ONE:
            case TWO:
            case THREE:
            case FOUR:
            case FIVE:
            case SIX:
            case SEVEN:
            case EIGHT:
            case NINE:
                result = true;
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    private boolean isPreUnaryOperator(MathSymbol symbol){
        boolean result;
        switch (symbol){
            case MINUS:
            case NEGATE:
                result = true;
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    private boolean isPostUnaryOperator(MathSymbol symbol){
        boolean result;
        switch (symbol){
            case PERCENT:
            case NEGATE:
                result = true;
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    private boolean isBinaryOperator(MathSymbol symbol){
        boolean result;
        switch (symbol){
            case PLUS:
            case MINUS:
            case MULTIPLY:
            case DIVIDE:
                result = true;
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    private boolean isGroupingOperator(MathSymbol symbol){
        boolean result;
        switch (symbol){
            case PARENTHESIS:
                result = true;
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    private boolean isUniqueOperator(MathSymbol symbol){
        boolean result;
        switch (symbol){
            case DECIMAL:
                result = true;
                break;
            default:
                result = false;
                break;
        }

        return result;
    }

    private Operator constructOperator(MathSymbol mathSymbol, OperatorType operatorType) {
        Operator operator;
        switch (operatorType) {
            case PRE_UNARY:
            case POST_UNARY:
                operator = getUnaryOperator(mathSymbol);
                break;
            case BINARY:
                operator = getBinaryOperator(mathSymbol);
                break;
            default:
                operator = null;
                break;
        }
            return operator;
    }

    private Operator getBinaryOperator(MathSymbol mathSymbol) {
        Operator operator;
        switch (mathSymbol) {
            case PLUS:
                operator = new AdditionOperator();
                break;
            case MINUS:
                operator = new SubtractionOperator();
                break;
            case MULTIPLY:
                operator = new MultiplicationOperator();
                break;
            case DIVIDE:
                operator = new DivisionOperator();
                break;
            default:
                operator = null;
                break;
        }
        return operator;
    }

    private Operator getUnaryOperator(MathSymbol mathSymbol) {
        Operator operator;
        switch (mathSymbol) {
            case MINUS:
            case NEGATE:
                operator = new NegationOperator();
                break;
            case PERCENT:
                operator = new PercentOperator();
                break;
            default:
                operator = null;
                break;
        }
        return operator;
    }
}