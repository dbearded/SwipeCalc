package com.derekbearded.android.swipecalc.data;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Observable;

/**
 * Created by Sputnik on 1/23/2018.
 */

public class Expression extends Observable {
    private static final int PRECISION = 14;
    public static EnumSet<MathSymbol> numerals = EnumSet.range(MathSymbol.ZERO, MathSymbol.DECIMAL);
    private static EnumSet<MathSymbol> preUnaryOperators = EnumSet.of(MathSymbol.NEGATE, MathSymbol.MINUS);
    private static EnumSet<MathSymbol> negationOperators = EnumSet.of(MathSymbol.NEGATE, MathSymbol.MINUS);
    private static EnumSet<MathSymbol> postUnaryOperators = EnumSet.of(MathSymbol.PERCENT);
    private static EnumSet<MathSymbol> binaryOperators = EnumSet.range(MathSymbol.PLUS, MathSymbol.DIVIDE);
    private static EnumSet<MathSymbol> noChaining = EnumSet.of(MathSymbol.MINUS, MathSymbol.NEGATE);
    private static EnumSet<MathSymbol> negation = EnumSet.of(MathSymbol.NEGATE, MathSymbol.MINUS);
    private static EnumSet<MathSymbol> groupingOperators = EnumSet.range(MathSymbol.LEFT_PARENTHESIS, MathSymbol.UNSPECIFIED_PARENTHESIS);
    private ExpressionNode head, tail;
    private LinkedList<ExpressionNode> negStack = new LinkedList<>();
    private StringBuilder numberBuilder = new StringBuilder();
    private MathContext mathContext = new MathContext(PRECISION, RoundingMode.HALF_EVEN);
    private int groupLevel = 0;
    // Used for early breaking out of tail
    private boolean foundPrevious = false;

    private static abstract class ExpressionNode {
        protected ExpressionNode[] children;
        protected ExpressionNode parent;
        protected BigDecimal number;
        protected ExpressionPrecedence precedence;
        protected ExpressionType type;
        protected int nodeGroupLevel = 0;
        enum ExpressionType {
            UNARY_PRE, UNARY_POST, BINARY, NUMBER
        }

        ExpressionNode(ExpressionType type){
            this.type = type;
        }

        abstract void evaluate();
    }

    private static class BinaryOperatorNode extends ExpressionNode {

        private BinaryOperator operator;

        BinaryOperatorNode(BinaryOperator binaryOperator, MathContext mathContext){
            this(binaryOperator);
            this.operator.setMathContext(mathContext);
        }

        BinaryOperatorNode(BinaryOperator binaryOperator) {
            super(ExpressionType.BINARY);
            this.operator = binaryOperator;
            children = new ExpressionNode[2];
            precedence = operator.precedence;
        }

        @Override
        void evaluate() {
            if (children[0] != null && children[1] != null && children[0].number != null && children[1].number != null) {
                number = operator.operate(children[0].number, children[1].number);
            } else {
                number = null;
            }
        }

        @Override
        public String toString() {
            return operator.toString();
        }

        MathSymbol getSymbol() {
            return operator.symbol;
        }
    }

    private static class UnaryOperatorNode extends ExpressionNode {

        private UnaryOperator operator;

        UnaryOperatorNode(UnaryOperator operator, MathContext mathContext){
            this(operator);
            this.operator.setMathContext(mathContext);
        }

        UnaryOperatorNode(UnaryOperator operator) {
            // Default to PRE and change if needed after construction
            super(ExpressionType.UNARY_PRE);
            this.operator = operator;
            // Unary type has two slots to store a child. [0] represents a left child for a
            // Post Unary Operator  and [1] represents a right child for a Pre Unary Operator
            // (in-order traversal LR)
            children = new ExpressionNode[2];
            precedence = operator.precedence;
            if (operator.unaryType.equals(UnaryOperator.UnaryType.POST)){
                this.type = ExpressionType.UNARY_POST;
            }
        }

        MathSymbol getSymbol() {
            return operator.symbol;
        }

        @Override
        void evaluate() {
            switch (type) {
                case UNARY_POST:
                    if (children[0] != null && children[0].number != null) {
                        number = operator.operate(children[0].number);
                    } else {
                        number = null;
                    }
                    break;
                case UNARY_PRE:
                    if (children[1] != null && children[1].number != null) {
                        number = operator.operate(children[1].number);
                    } else {
                        number = null;
                    }
                    break;
            }
        }

        @Override
        public String toString() {
            return operator.toString();
        }
    }

    private static class NumberNode extends ExpressionNode {

        NumberNode(BigDecimal number) {
            super(ExpressionType.NUMBER);
            this.number = number;
            precedence = ExpressionPrecedence.NUMBER;
            children = new ExpressionNode[2];
        }

        @Override
        void evaluate() {
        }

        @Override
        public String toString() {
            return number.toString();
        }
    }

    public Expression(){}

    public Expression(String str){
        add(stringInputToMathSymbols(str));
    }

    public Expression(MathSymbol... symbols){
        add(symbols);
    }

    /**
     * Method used for testing
     * @param args String of valid input
     * @return evaluated expression
     */
    public static String evaluateInput(String args) {
        MathSymbol[] symbols = stringInputToMathSymbols(args);
        Expression expression = new Expression();
        for (MathSymbol s :
                symbols) {
            expression.add(s);
        }
        return expression.getValue();
    }

    /**
     * Method used for testing to read in a string, parse it, and regurgitate the string
     * @param args string input
     * @return string generated by traversing the Expression data structure
     */
    public static String inputToString(String args){
        MathSymbol[] symbols = stringInputToMathSymbols(args);
        Expression expression = new Expression();
        for (MathSymbol s :
                symbols) {
            expression.add(s);
        }
        return expression.toString();
    }

    /**
     * Method mainly used for testing
     * @param args string of valid input
     * @return array of MathSymbols for adding to Expression data structure
     */
    public static MathSymbol[] stringInputToMathSymbols(String args) {
        MathSymbol[] symbols = new MathSymbol[args.length()];
        char[] chars = args.toCharArray();
        for (int i=0; i < args.length(); i++) {
            symbols[i] = MathSymbol.fromString(Character.toString(chars[i]));
        }
        return symbols;
    }

    /**
     * Method for input of a multi-digit expression
     * @param symbols variable number of MathSymbol
     */
    public void add(MathSymbol... symbols){
        for (MathSymbol symbol :
                symbols) {
//            add(symbol, false, true); // For testing
            add(symbol, false, false); // When testing is finished, use this instead
        }
        forceEvaluate();
        notifyObservers();
    }

    public void add(MathSymbol symbol){
        add(symbol, true, true);
    }

    public void add(MathSymbol symbol, boolean notifyObservers){
        add(symbol, notifyObservers, true);
    }

    public void clear() {
        head = null;
        tail = null;
        numberBuilder.setLength(0);
        groupLevel = 0;
        negStack.clear();
        setChanged();
        notifyObservers();
    }

    public void clear(boolean keepHead, boolean notify){
        if (keepHead) {
            if (head.number != null) {
                ExpressionNode tempHead = new NumberNode(head.number);
                head = null;
                head = tempHead;
                tail = tempHead;
                numberBuilder.setLength(0);
                numberBuilder.append(head.number.toString());
                groupLevel = 0;
                negStack.clear();
                setChanged();
                if (notify) {
                    notifyObservers();
                }
            }
        } else {
            clear();
        }
    }

    public void delete() {
        if (tail == null){
            if (groupLevel > 0){
                groupLevel--;
                setChanged();
                notifyObservers();
            }
            return;
        }
        if (groupLevel == tail.nodeGroupLevel){
            switch (tail.type){
                case NUMBER:
                    deleteNumeral();
                    break;
                case UNARY_POST:
                case UNARY_PRE:
                case BINARY:
                    deleteNode(tail);
                    break;
            }
        } else if (groupLevel < tail.nodeGroupLevel) {
            groupLevel++;
        } else {
            groupLevel--;
        }
        // Now check to see if same group level with tail and re-enter node if its a number
        if (tail != null && groupLevel == tail.nodeGroupLevel){
            if (tail instanceof NumberNode) {
                numberBuilder.setLength(0);
                numberBuilder.append(tail.toString());
            }
        }
        setChanged();
        notifyObservers();
    }

    /**
     * Forces a re-evaluation of the Expression. To obtain an updated result
     * without forcing a re-evaluation, use getValue() instead.
     *
     * @return the result of the Expression, or null if the expression cannot be evaluated
     * @see #getValue()
     */
    public String forceEvaluate() {
        forceEvaluate(head);
        return head.number.toString();
    }

    /**
     * Returns an updated result of the Expression
     *
     * @return updated result of the Expression or null if it cannot be evaluated
     */
    public String getValue() {
        if (head == null || head.number == null) {
            return "";
        }

        return head.number.toString();
    }

    public String toStringGroupingAsInputted() {
        StringBuilder stringBuilder = new StringBuilder();
        if (head == null){
            if (groupLevel > 0){
                for (int i = 0; i < groupLevel; i++) {
                    stringBuilder.append("(");
                }
            }
            return stringBuilder.toString();
        }
        for (int i = 0; i < head.nodeGroupLevel; i++ ) {
            stringBuilder.append(MathSymbol.LEFT_PARENTHESIS.toString());
        }
        foundPrevious = false;
        treeToStringGroupingAsInputted(head, stringBuilder);
        if (tail.nodeGroupLevel > groupLevel) {
            for (int i = tail.nodeGroupLevel; i > groupLevel; i--) {
                stringBuilder.append(MathSymbol.RIGHT_PARENTHESIS.toString());
            }
        } else if (tail.nodeGroupLevel < groupLevel){
            for (int i = tail.nodeGroupLevel; i < groupLevel; i++) {
                stringBuilder.append(MathSymbol.LEFT_PARENTHESIS.toString());
            }
        }
        String result = stringBuilder.toString();
        return result;
    }    /**
     * Generates a string representation of the Expression
     *
     * @return a string representation of the Expression
     */
    @Override
    public String toString() {
        if (head == null){
            return "";
        }
        return treeToString(head, new StringBuilder()).toString();
    }

    // TODO make a comment that maps tail to potential
    ExpressionNode createNode(ExpressionNode previous, MathSymbol symbol) {
        // This class returns the appropriate ExpressionNode subclass based on the
        // the input symbol and the tail ExpressionNode, which is needed for context.

        ExpressionNode expressionNode = null;

        if (previous == null) {
            // First expression node
            if (numerals.contains(symbol)) {
                if (symbol.equals(MathSymbol.DECIMAL)){
                    expressionNode = new NumberNode(new BigDecimal(MathSymbol.ZERO.toString() + symbol.toString(), mathContext));
                } else {
                    expressionNode = new NumberNode(new BigDecimal(symbol.toString(), mathContext));
                }
            } else if (preUnaryOperators.contains(symbol)) {
                expressionNode = new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
            }
        } else {
            switch (previous.type){
                case UNARY_PRE:
                    // If the node to be added is a number
                    if (numerals.contains(symbol)) {
                        if (symbol.equals(MathSymbol.DECIMAL)){
                            expressionNode = new NumberNode(new BigDecimal(MathSymbol.ZERO.toString() + symbol.toString(), mathContext));
                        } else {
                            expressionNode = new NumberNode(new BigDecimal(symbol.toString(), mathContext));
                        }
                    }
                    // If the node to be added is a PreUnary Operator that can be chained
                    else if (preUnaryOperators.contains(symbol) && !noChaining.contains(symbol)) {
                        expressionNode = new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
                    }
                    break;
                case UNARY_POST:
                    // If the node to be added is a Binary Operator
                    if (binaryOperators.contains(symbol)) {
                        expressionNode = new BinaryOperatorNode(new BinaryOperatorFactory().getBinaryOperator(symbol), mathContext);
                    }
                    // If the node to be added is a Post Unary Operator
                    else if (postUnaryOperators.contains(symbol) && !noChaining.contains(symbol)) {
                        expressionNode = new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
                    }
                    break;
                case BINARY:
                    // If the node to be added is a number
                    if (numerals.contains(symbol)) {
                        if (symbol.equals(MathSymbol.DECIMAL)){
                            expressionNode = new NumberNode(new BigDecimal(MathSymbol.ZERO.toString() + symbol.toString(), mathContext));
                        } else {
                            expressionNode = new NumberNode(new BigDecimal(symbol.toString(), mathContext));
                        }
                    }
                    // If the node to be added is a Pre Unary Operator
                    else if (preUnaryOperators.contains(symbol)) {
                        expressionNode = new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
                    }
                    break;
                case NUMBER:
                    // If the node to be added is a Post Unary Operator
                    if (postUnaryOperators.contains(symbol)) {
                        expressionNode = new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
                    }
                    // If the node to be added is a Binary Operator
                    else if (binaryOperators.contains(symbol)) {
                        expressionNode = new BinaryOperatorNode(new BinaryOperatorFactory().getBinaryOperator(symbol), mathContext);
                    }
                    break;
                default:
                    break;
            }
        }
        // Negation operator can always be created...
        if (expressionNode == null && negationOperators.contains(symbol)) {
            expressionNode = new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
        }
        if (expressionNode != null){
            expressionNode.nodeGroupLevel = groupLevel;
        }
        return expressionNode;
    }

    private void add(MathSymbol symbol, boolean notifyObservers, boolean evaluate) {
//        System.out.println("attempting to add symbol: " + symbol.toString());
//        try {
            // Handle special cases first before adding another node
            if (numerals.contains(symbol)) {
                addNumeral(symbol);
            } else if (groupingOperators.contains(symbol)){
                handleGroupingSymbol(symbol);
            } else if (negation.contains(symbol)){
                handleNegationSymbol(symbol);
            } else {
                // No special case so just add another node
                addNode(symbol);
            }
        /*} catch (RuntimeException e) {
            System.out.println("attempting to add symbol: " + symbol.toString());
            System.out.println("grouping level: " + groupLevel);
            System.out.println("head: " + head.toString());
            System.out.println("tail: " + tail.toString());
            System.out.println("expression: " + this.toString());
            e.printStackTrace();
        }*/

        if (evaluate){
            evaluateToRoot(tail);
        }
        if (notifyObservers){
            setChanged();
            notifyObservers();
        }
        /*System.out.println("grouping level: " + groupLevel);
        if (head != null) {
            System.out.println("head: " + head.toString());
        }
        if (tail != null) {
            System.out.println("tail: " + tail.toString());
        }
        System.out.println("expression: " + this.toString());*/
    }

    private void handleGroupingSymbol(MathSymbol symbol){
        if (!groupingOperators.contains(symbol)){
            throw new IllegalArgumentException("Argument not part of grouping symbols: " + symbol.toString());
        }
        if (tail == null){
            groupLevel++;
            return;
        }
        switch (tail.type){
            case BINARY:
            case UNARY_PRE:
                groupLevel++;
                break;
            case NUMBER:
                numberBuilder.setLength(0);
            case UNARY_POST:
                if (groupLevel > 0){
                    groupLevel--;
                } else {
                    add(MathSymbol.MULTIPLY, false, false);
                    groupLevel++;
                }
                break;
        }
    }

    private void handleNegationSymbol(MathSymbol symbol) {
        if (!negation.contains(symbol)) {
            throw new IllegalArgumentException("Argument not part of negation symbols: " + symbol.toString());
        }
        // Check edge cases first
        // No nodes yet
        if (head == null){
            addNode(symbol);
            return;
        }
        ExpressionNode prevNeg = negStack.peekFirst();
        // Check if tail is already a node
        if (tail == prevNeg){
            deleteNode(prevNeg);
            return;
        }
        // Favor subtraction over negation
        if (symbol.equals(MathSymbol.MINUS) && tail.type.equals(ExpressionNode.ExpressionType.NUMBER)) {
            addNode(symbol);
            return;
        }
        ExpressionNode posUp = findNegationPosUp();
        if (posUp == null){
            addNode(symbol);
            return;
        }
        if (posUp.parent == null){
            addNode(symbol);
            return;
        }
        if (prevNeg != null && negStack.contains(posUp.parent)){
            deleteNode(prevNeg);
        } else {
            addNode(symbol);
        }
    }

    /**
     * Adds a node to the expression tree
     *
     * @param symbol used to determine which node to add
     */
    private void addNode(MathSymbol symbol) {
        ExpressionNode node = createNode(tail, symbol);
        if (node == null){
            return;
        }
        // First node in expression tree
        if (tail == null) {
            head = node;
            tail = node;
            if (numerals.contains(symbol)) {
                numberBuilder.append(symbol);
            } else if (negationOperators.contains(symbol)) {
//                prevNeg = node;
                negStack.addFirst(node);
            }
            return;
        }
        switch (node.type){
            case BINARY:
            case UNARY_POST:
                insertOperatorNode(node, findPositionUp(node, tail));
                numberBuilder.setLength(0);
                tail = node;
                break;
            case UNARY_PRE:
                if (negationOperators.contains(symbol)) {
                    /*ExpressionNode refNode = findNegationPosUp();
                    if (refNode == null) {
                        refNode = tail;
                    }*/
                    insertNegationNode(node, findNegationPosUp());
//                    prevNeg = node;
                    negStack.addFirst(node);
//                    tail = findNewTail();
                } else {
                    insertOperatorNode(node, findPositionUp(node, tail));
                    tail = node;
                }
                numberBuilder.setLength(0);
                if (tail.type.equals(ExpressionNode.ExpressionType.NUMBER) && tail.nodeGroupLevel == node.nodeGroupLevel){
                    numberBuilder.append(tail.number.toPlainString());
                }
                break;
            case NUMBER:
                fillNodeBelow(node, tail);
                tail = node;
                numberBuilder.append(symbol);
                break;
        }
    }

    private void deleteNumeral(){
        // If numberBuilder wasn't reset, need to reset it
        if (numberBuilder.length() == 0){
            numberBuilder.append(tail.number.toPlainString()); // why plain string?
//            numberBuilder.append(tail.number.toString());
        }

        // Remove a numeral OR the entire node
        if (tail.type.equals(ExpressionNode.ExpressionType.NUMBER) && numberBuilder.length() > 1){
            numberBuilder.deleteCharAt(numberBuilder.length()-1);
            if (numberBuilder.charAt(numberBuilder.length()-1) == '.'){
                numberBuilder.deleteCharAt(numberBuilder.length()-1);
            }
            tail.number = new BigDecimal(numberBuilder.toString(), mathContext);
            evaluateToRoot(tail);
        } else {
            deleteNode(tail);
            // Reset numberBuilder
            if (tail != null && tail.type.equals(ExpressionNode.ExpressionType.NUMBER)){
                numberBuilder.append(tail.number.toPlainString());
                tail.number.round(mathContext);
//                numberBuilder.append(tail.number.toString());
            }
        }
    }

    /**
     * Rebuilds the Expression by traversing the Expression tree
     *
     * @param node
     * @param stringBuilder
     * @return
     */
    private StringBuilder treeToString(ExpressionNode node, StringBuilder stringBuilder) {
        if (node == null) {
            return null;
        }
        // In - order traversal of expression tree (LR)
        if (node.children[0] != null) {
            if (node.children[0].nodeGroupLevel > node.nodeGroupLevel){
                stringBuilder.append(MathSymbol.LEFT_PARENTHESIS.toString());
            }
            treeToString(node.children[0], stringBuilder);
        }
        stringBuilder.append(node.toString());
        if (node.children[1] != null) {
            if (node.children[1].nodeGroupLevel > node.nodeGroupLevel){
                stringBuilder.append(MathSymbol.LEFT_PARENTHESIS.toString());
            }
            treeToString(node.children[1], stringBuilder);
        }
        if (node.parent != null && node.parent.nodeGroupLevel < node.nodeGroupLevel){
            stringBuilder.append(MathSymbol.RIGHT_PARENTHESIS.toString());
        }
        return stringBuilder;
    }

    // TODO refactor with stack for better performance and so don't have to use non-local var
    private StringBuilder treeToStringGroupingAsInputted(ExpressionNode node, StringBuilder stringBuilder) {
        if (node == null) {
            return null;
        }
        // In - order traversal of expression tree (LR)
        if (node.children[0] != null) {
            for (int i = node.nodeGroupLevel; i < node.children[0].nodeGroupLevel; i++) {
                stringBuilder.append(MathSymbol.LEFT_PARENTHESIS.toString());
            }
            treeToStringGroupingAsInputted(node.children[0], stringBuilder);
        }
        stringBuilder.append(node.toString());
        if (node.children[1] != null) {
            for (int i = node.nodeGroupLevel; i < node.children[1].nodeGroupLevel; i++) {
                stringBuilder.append(MathSymbol.LEFT_PARENTHESIS.toString());
            }
            treeToStringGroupingAsInputted(node.children[1], stringBuilder);
        }
        if (node.equals(tail)){
            foundPrevious = true;
        }
        if (foundPrevious){
            return stringBuilder;
        }
        if (node.parent != null){
            for (int i = node.nodeGroupLevel; i > node.parent.nodeGroupLevel; i--) {
                stringBuilder.append(MathSymbol.RIGHT_PARENTHESIS.toString());
            }
        }
        return stringBuilder;
    }

    /**
     * Evaluates the Expression tree with a Post Order Traversal (LR)
     *
     * @param node starting node
     */
    private void forceEvaluate(ExpressionNode node) {
        if (node == null) {
            return;
        }
        // Post-order traversal of tree: Left child, Right child, self
        forceEvaluate(node.children[0]);
        forceEvaluate(node.children[1]);
        node.evaluate();
    }

    /**
     * Inserts a Unary or Binary Operator into the Expression Tree around the given reference node
     *
     * @param newNode    node to be inserted into the Expression Tree
     * @param pushedNode reference node that the new node is inserted around (above for Post Unary and Binary; below for Pre Unary)
     */
    private void insertOperatorNode(@NonNull ExpressionNode newNode, @NonNull ExpressionNode pushedNode) {

        switch (newNode.type){
            case BINARY:
            case UNARY_POST:
                newNode.children[0] = pushedNode;
                break;
            case UNARY_PRE:
                newNode.children[1] = pushedNode;
                break;
        }
        newNode.parent = pushedNode.parent;
        pushedNode.parent = newNode;
        if (newNode.parent == null){
            head = newNode;
        } else {
            int i;
            for(i = 0; i < newNode.parent.children.length; i++){
                if (newNode.parent.children[i] != null){
                    if (newNode.parent.children[i].equals(pushedNode)){
                        break;
                    }
                }
            }
            newNode.parent.children[i] = newNode;
        }
    }

    private void insertNegationNode(ExpressionNode newNode, ExpressionNode pushedNode){
        // At the tail
        if (pushedNode == null){
            fillNodeBelow(newNode, tail);
            tail = newNode;
        } else {
            insertOperatorNode(newNode, pushedNode);
        }
    }

    /**
     * Traverses up the expression tree to find a node with lower precedence than the given node
     *
     * @param node  given node to find appropriate position up the Expression Tree
     * @param start reference node of Expression Tree
     * @return the child of first node with a lower precedence than the given node or the root node, or null if the given node is a higher precedence than the starting node.
     */
    private ExpressionNode findPositionUp(@NonNull ExpressionNode node, @NonNull ExpressionNode start) {
        if (node.precedence.compareTo(start.precedence) > 0) {
            // Current node is higher precedence than parent node
            return null;
        }
        ExpressionNode runner = start;
        // runner is root
        if (runner.parent == null){
            return runner;
        }
        while (node.nodeGroupLevel < runner.nodeGroupLevel) {
            if (node.nodeGroupLevel >= runner.parent.nodeGroupLevel) {
                return runner;
            }
            runner = runner.parent;
            if (runner.parent == null) {
                return runner;
            }
        }
        while (node.precedence.compareTo(runner.parent.precedence) <= 0) {
            if (runner.nodeGroupLevel > runner.parent.nodeGroupLevel){
                break;
            }
            if (node.nodeGroupLevel > runner.nodeGroupLevel){
                break;
            }
            runner = runner.parent;
            if (runner.parent == null) {
                break;
            }
        }
        return runner;
    }

    // Returns child of most previous possible negation position
    private ExpressionNode findNegationPosUp(){
        ExpressionNode runner = tail;
        // runner is null - no first node yet
        if (runner == null){
            return null;
        }
        // tail is already negation node
        /*if (negationOperators.contains(MathSymbol.fromString(runner.toString()))){
            return null;
        }*/
        // runner is root
        /*if (runner.parent == null){
            return runner;
        }*/
        if (groupLevel < runner.nodeGroupLevel){
            if (runner.parent == null) {
                return runner;
            }
            while (groupLevel < runner.parent.nodeGroupLevel) {
                runner = runner.parent;
                if (runner.parent == null) {
                    return runner;
                }
            }
        } else {
            switch (runner.type){
                case NUMBER:
//                    break;
                case UNARY_POST:
//                    runner = runner.parent;
                    break;
                case BINARY:
                case UNARY_PRE:
                    runner = null;
                    break;
            }
        }
        return runner;
    }

    /**
     * Adds a new node as a child to a reference node if the right child is null for a Binary and Post Unary OperatorNode and the left child for a Pre Unary OperatorNode.
     *
     * @param newNode the node to be added below the reference node
     * @param refNode the reference node to have child node added
     * @throws IllegalArgumentException if the reference node already had a child
     */
    private void fillNodeBelow(@NonNull ExpressionNode newNode, ExpressionNode refNode) {
        /*if (refNode == null){
            refNode = tail;
        }*/
        switch (refNode.type){
            case BINARY:
            case UNARY_PRE:
                refNode.children[1] = newNode;
                break;
            case UNARY_POST:
                refNode.children[0] = newNode;
                break;
        }
        newNode.parent = refNode;
    }

    /**
     * Updates a number node by appending a new numeral
     *
     * @param symbol symbol to be appened
     */
    private void addNumeral(MathSymbol symbol) {
        if (!isValidNumeral(symbol)) {
            return;
        }
        if (numberBuilder.length() == 0){
          // Need a new node first
            addNode(symbol);
        } else {
            if (numberBuilder.length() < PRECISION) {
                numberBuilder.append(symbol);
                tail.number = new BigDecimal(numberBuilder.toString(), mathContext);
            }
        }
    }

    /**
     * Appends a numberal to the class's list of numerals
     *
     * @param symbol symbol to be added to the list of numerals
     * @return returns true if the numeral was added; otherwise, false
     * @throws IllegalArgumentException
     */
    private boolean isValidNumeral(MathSymbol symbol) {
        if (!numerals.contains(symbol)) {
            return false;
        }
        if (numberBuilder.length() > 0) {
            // If there is already a decimal in the number
            if (symbol.equals(MathSymbol.DECIMAL) && numberBuilder.lastIndexOf(MathSymbol.DECIMAL.toString()) > -1) {
                return false;
            }
            // If there was already a leading zero in the number
            if (symbol.equals(MathSymbol.ZERO) && numberBuilder.length() == 1 && numberBuilder.lastIndexOf(MathSymbol.ZERO.toString()) > -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deletes a given node, preserving the tree by stitching the child of the given node with its parent (left child only for a binary operator)
     *
     */
    private void deleteNode(@NonNull ExpressionNode node) {

        // If deleting the tail node, need to find next oldest tail node
        if (tail.equals(node)) {
            tail = findNextOldestNode(tail);
        }

        // Re-enter number node if that is new tail
        if (tail != null && tail.type.equals(ExpressionNode.ExpressionType.NUMBER)) {
            numberBuilder.setLength(0);
            numberBuilder.append(tail.number.toPlainString());
//            numberBuilder.append(tail.number.toString());
        }

        // Since no early escapes, this is safe here
        // Also, this is a safe call because the node is only
        // removed from the stack if the stack contains the node
        negStack.remove(node);

        boolean isRoot = node.parent == null;
        // Stitch surrounding nodes together
        int i = 0;
        if (!isRoot) {
            for (i = 0; i < node.children.length; i++) {
                if (node.parent.children[i] != null) {
                    if (node.parent.children[i].equals(node)) {
                        break;
                    }
                }
            }
        }

        switch (node.type){
            case UNARY_POST:
            case BINARY:
                if (isRoot) {
                    head = node.children[0];
                    if (head != null) {
                        head.parent = null;
                    }
                } else {
                    node.parent.children[i] = node.children[0];
                    if (node.children[0] != null) {
                        node.children[0].parent = node.parent;
                    }
                }
                break;
            case UNARY_PRE:
                if (isRoot) {
                    head = node.children[1];
                    if (head != null) {
                        head.parent = null;
                    }
                } else {
                    node.parent.children[i] = node.children[1];
                    if (node.children[1] != null) {
                        node.children[1].parent = node.parent;
                    }
                }
                break;
            case NUMBER:
                if (isRoot) {
                    head = node.children[0];
                    if (head != null) {
                        head.parent = null;
                    }
                } else {
                    if (node.number.precision() > 1){
                        String strNum = null;
                        strNum = node.number.unscaledValue().toString();
                        node.number = new BigDecimal(strNum.substring(0, strNum.length() - 1), mathContext);
                    } else {
                        node.parent.children[i] = null;
                    }
                }
                numberBuilder.setLength(0);
                break;
        }
        // delete node
        node = null;
        evaluateToRoot(tail);
    }

    /**
     * Finds the next oldest node in the expression tree given a runner node and a reference node
     *
     * @param runner  a mutable node that traverses the tree
     * @param refNode an unchanging node that serves as a reference
     * @return the next oldest node in the treee
     */
    private ExpressionNode findNextOldestNode(ExpressionNode runner, @NonNull ExpressionNode refNode) {
        // Reversed in-order traversal. So order is Right child, Self, Left Child, and parent.
        if (runner.children[1] != null) {
            return findNextOldestNode(runner.children[1], refNode);
        } else if (!runner.equals(refNode)) {
            return runner;
        } else if (runner.children[0] != null) {
            return findNextOldestNode(runner.children[0], refNode);
        } else if (runner.parent != null) {
            return runner.parent;
        } else {
            return null;
        }
    }

    /**
     * Finds the next oldest node in the expression tree from the given node
     *
     * @param start the starting node
     * @return the next oldest node
     */
    private ExpressionNode findNextOldestNode(@NonNull ExpressionNode start) {
        return findNextOldestNode(start, start);
    }

    /**
     * Finds and returns the head of the expression tree
     *
     * @param start starting node
     * @return the head of the expression tree
     */
    private ExpressionNode findHead(@NonNull ExpressionNode start) {
        while (start.parent != null) {
            start = start.parent;
        }
        return start;
    }

    private void evaluateToRoot(ExpressionNode node) {
        if (node == null) {
            return;
        }
        node.evaluate();
        evaluateToRoot(node.parent);
    }

    private ExpressionNode findNewTail() {
        return postOrderRLSearch(tail);
    }

    private ExpressionNode postOrderRLSearch(ExpressionNode start) {
        if (start.children[1] != null){
            return postOrderRLSearch(start.children[1]);
        } else if (start.children[0] != null) {
            return postOrderRLSearch(start.children[0]);
        } else {
            return start;
        }
    }
}