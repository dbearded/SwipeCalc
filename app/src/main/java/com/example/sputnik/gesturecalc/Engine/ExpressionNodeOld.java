package com.example.sputnik.gesturecalc.Engine;

import java.math.BigDecimal;

/**
 * Created by Sputnik on 1/23/2018.
 */

class ExpressionNodeOld {
    private BigDecimal number;
    private MathSymbol symbol;
    private ExpressionNodeOld leftChild;
    private ExpressionNodeOld rightChild;
    private ExpressionNodeOld parent;

    enum Node {
        LEFT_CHILD, RIGHT_CHILD, PARENT
    }

    ExpressionNodeOld(){
    }

    ExpressionNodeOld(BigDecimal number){
        this.number = number;
    }

    ExpressionNodeOld(MathSymbol symbol){
        this.symbol = symbol;
    }

    ExpressionNodeOld(ExpressionNodeOld parentNode){
        this.parent = parentNode;
    }

    ExpressionNodeOld(BigDecimal number, MathSymbol symbol){
        this.number = number;
        this.symbol = symbol;
    }

    ExpressionNodeOld(BigDecimal number, MathSymbol symbol, ExpressionNodeOld parentNode){
        this.number = number;
        this.symbol = symbol;
        this.parent = parentNode;
    }

    void setNumber(BigDecimal number){
        this.number = number;
    }

    BigDecimal getNumber(){
        return number;
    }

    void setSymbol(MathSymbol symbol){
        this.symbol = symbol;
    }

    MathSymbol getSymbol(){
        return symbol;
    }

    void setNode(Node node, ExpressionNodeOld expressionNode){
        switch (node){
            case PARENT:
                this.parent = expressionNode;
                break;
            case LEFT_CHILD:
                this.leftChild = expressionNode;
                break;
            case RIGHT_CHILD:
                this.rightChild = expressionNode;
                break;
            default:
                break;
        }
    }

    ExpressionNodeOld getNode(Node node){
        switch (node){
            case PARENT:
                return this.parent;
            case LEFT_CHILD:
                return this.leftChild;
            case RIGHT_CHILD:
                return this.rightChild;
            default:
                return null;
        }
    }

}
