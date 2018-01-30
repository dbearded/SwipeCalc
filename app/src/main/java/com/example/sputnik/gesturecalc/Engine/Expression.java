package com.example.sputnik.gesturecalc.Engine;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.EnumSet;

/**
 * Created by Sputnik on 1/23/2018.
 */

public class Expression {
    public static EnumSet<MathSymbol> numerals = EnumSet.range(MathSymbol.ZERO, MathSymbol.DECIMAL);
    private static EnumSet<MathSymbol> preUnaryOperators = EnumSet.of(MathSymbol.NEGATE, MathSymbol.MINUS);
    private static EnumSet<MathSymbol> postUnaryOperators = EnumSet.of(MathSymbol.PERCENT, MathSymbol.NEGATE);
    private static EnumSet<MathSymbol> binaryOperators = EnumSet.range(MathSymbol.PLUS, MathSymbol.DIVIDE);
    private static EnumSet<MathSymbol> noChaining = EnumSet.of(MathSymbol.MINUS, MathSymbol.NEGATE);
    private static EnumSet<MathSymbol> toggle = EnumSet.of(MathSymbol.NEGATE, MathSymbol.MINUS);
    private ExpressionNode head;
    private ExpressionNode previous;
    private StringBuilder numberBuilder = new StringBuilder();
    private MathContext mathContext = new MathContext(14, RoundingMode.HALF_EVEN);
    private DecimalFormat decimalFormat = new DecimalFormat("0.##############");

    private static abstract class ExpressionNode {
        enum ExpressionType {
            UNARY_PRE, UNARY_POST, BINARY, NUMBER
        }
        protected ExpressionNode[] children;
        protected ExpressionNode parent;
        protected BigDecimal number;
        protected ExpressionPrecedence precedence;
        protected ExpressionType type;

        ExpressionNode(ExpressionType type){
            this.type = type;
        }

        abstract void evaluate();
    }

    private static class BinaryOperatorNode extends ExpressionNode {

        private BinaryOperator operator;

        BinaryOperatorNode(BinaryOperator binaryOperator) {
            super(ExpressionType.BINARY);
            this.operator = binaryOperator;
            children = new ExpressionNode[2];
            precedence = operator.precedence;
        }

        BinaryOperatorNode(BinaryOperator binaryOperator, MathContext mathContext){
            this(binaryOperator);
            this.operator.setMathContext(mathContext);
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

        UnaryOperatorNode(UnaryOperator operator, MathContext mathContext){
            this(operator);
            this.operator.setMathContext(mathContext);
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
        private DecimalFormat decimalFormat;

        NumberNode(BigDecimal number) {
            super(ExpressionType.NUMBER);
            this.number = number;
            precedence = ExpressionPrecedence.NUMBER;
            children = new ExpressionNode[2];
        }

        NumberNode(BigDecimal number, DecimalFormat format){
            this(number);
            this.decimalFormat = format;
        }

        @Override
        void evaluate() {
        }

        @Override
        public String toString() {
            return decimalFormat.format(number);
        }
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
            add(symbol, false, true); // For testing
//            add(symbol, false, false); // When testing is finished, use this instead
        }
//        forceEvaluate();
    }

    public void add(MathSymbol symbol){
        add(symbol, true, true);
    }

    public void add(MathSymbol symbol, boolean notifyObservers){
        add(symbol, notifyObservers, true);
    }

    private void add(MathSymbol symbol, boolean notifyObservers, boolean evaluate) {

        // If the symbol is a numeral and the previous node is a number, then need to add
        // numeral to previous node, not create a new number node.
        try {
            if (numerals.contains(symbol) && (numberBuilder.length() > 0)) {
                appendToNumberNode(symbol, (NumberNode) previous);
            } else {
                // If the symbol undoes the previous symbol (negate)
                if (head !=null && toggle.contains(symbol) && (previous.type.equals(ExpressionNode.ExpressionType.UNARY_PRE))) {
                    deleteNode(previous);
                    return;
                }
                // Generate a new node.
                addNode(symbol);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        if (evaluate){
            evaluateToRoot(previous);
        }
        if (notifyObservers){
            // TODO notifyObservers
        }
    }

    public void delete() {
        // Remove a numeral OR the entire node
        if (previous.type.equals(ExpressionNode.ExpressionType.NUMBER) && numberBuilder.length() > 1){
            numberBuilder.deleteCharAt(numberBuilder.length()-1);
            if (numberBuilder.charAt(numberBuilder.length()-1) == '.'){
                numberBuilder.deleteCharAt(numberBuilder.length()-1);
            }
            previous.number = new BigDecimal(numberBuilder.toString(), mathContext);
            evaluateToRoot(previous);
        } else {
            deleteNode(previous);
            // Reset numberBuilder
            if (previous != null && previous.type.equals(ExpressionNode.ExpressionType.NUMBER)){
                numberBuilder.append(previous.number.toPlainString());
            }
        }
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
        return decimalFormat.format(head.number);
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
        return decimalFormat.format(head.number);
    }

    /**
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

    // TODO make a comment that maps previous to potential
    ExpressionNode createNode(ExpressionNode previous, MathSymbol symbol) {
        // This class returns the appropriate ExpressionNode subclass based on the
        // the input symbol and the previous ExpressionNode, which is needed for context.

        // First expression node
        if (previous == null) {
            if (numerals.contains(symbol)) {
                if (symbol.equals(MathSymbol.DECIMAL)){
                    return new NumberNode(new BigDecimal(MathSymbol.ZERO.toString() + symbol.toString(), mathContext), decimalFormat);
                }
                return new NumberNode(new BigDecimal(symbol.toString(), mathContext), decimalFormat);
            } else if (preUnaryOperators.contains(symbol)) {
                return new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
            }
        } else {
            switch (previous.type){
                case UNARY_PRE:
                    // If the node to be added is a number
                    if (numerals.contains(symbol)) {
                        if (symbol.equals(MathSymbol.DECIMAL)){
                            return new NumberNode(new BigDecimal(MathSymbol.ZERO.toString() + symbol.toString(), mathContext), decimalFormat);
                        }
                        return new NumberNode(new BigDecimal(symbol.toString(), mathContext), decimalFormat);
                    }
                    // If the node to be added is a PreUnary Operator that can be chained
                    else if (preUnaryOperators.contains(symbol) && !noChaining.contains(symbol)) {
                        return new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
                    }
                    break;
                case UNARY_POST:
                    // If the node to be added is a Binary Operator
                    if (binaryOperators.contains(symbol)) {
                        return new BinaryOperatorNode(new BinaryOperatorFactory().getBinaryOperator(symbol), mathContext);
                    }
                    // If the node to be added is a Post Unary Operator
                    else if (postUnaryOperators.contains(symbol) && !noChaining.contains(symbol)) {
                        return new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
                    }
                    break;
                case BINARY:
                    // If the node to be added is a number
                    if (numerals.contains(symbol)) {
                        if (symbol.equals(MathSymbol.DECIMAL)){
                            return new NumberNode(new BigDecimal(MathSymbol.ZERO.toString() + symbol.toString(), mathContext), decimalFormat);
                        }
                        return new NumberNode(new BigDecimal(symbol.toString(), mathContext), decimalFormat);
                    }
                    // If the node to be added is a Pre Unary Operator
                    else if (preUnaryOperators.contains(symbol)) {
                        return new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
                    }
                    break;
                case NUMBER:
                    // If the node to be added is a Post Unary Operator
                    if (postUnaryOperators.contains(symbol)) {
                        return new UnaryOperatorNode(new UnaryOperatorFactory().getUnaryOperator(symbol), mathContext);
                    }
                    // If the node to be added is a Binary Operator
                    else if (binaryOperators.contains(symbol)) {
                        return new BinaryOperatorNode(new BinaryOperatorFactory().getBinaryOperator(symbol), mathContext);
                    }
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * Adds a node to the expression tree
     *
     * @param symbol used to determine which node to add
     */
    private void addNode(MathSymbol symbol) {
        ExpressionNode node = createNode(previous, symbol);
        // First node in expression tree
        if (previous == null) {
            head = node;
            previous = node;
            if (numerals.contains(symbol)){
                numberBuilder.append(symbol);
            }
            return;
        }
        switch (node.type){
            case BINARY:
            case UNARY_PRE:
            case UNARY_POST:
                ExpressionNode refNode;
                refNode = findPositionUp(node, previous);
                insertOperatorNode(node, refNode);
                previous = node;
                numberBuilder.setLength(0);
                break;
            case NUMBER:
                fillNodeBelow(node, previous);
                previous = node;
                numberBuilder.append(symbol);
                break;
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
            treeToString(node.children[0], stringBuilder);
        }
        stringBuilder.append(node.toString());
        if (node.children[1] != null) {
            treeToString(node.children[1], stringBuilder);
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
                newNode.parent = pushedNode.parent;
                pushedNode.parent = newNode;
                if (newNode.parent == null){
                    head = newNode;
                } else {
                    int i = 0;
                    while (!newNode.parent.children[i].equals(pushedNode)){
                        i++;
                    }
                    newNode.parent.children[i] = newNode;
                }
                break;
            case UNARY_PRE:
                fillNodeBelow(newNode, pushedNode);
                break;
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
        while (node.precedence.compareTo(runner.parent.precedence) <= 0) {
            runner = runner.parent;
            if (runner.parent == null) {
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
        if (refNode == null){
            refNode = previous;
        }
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
     * @param node   number node to be updated
     */
    private void appendToNumberNode(MathSymbol symbol, @NonNull NumberNode node) {
        appendNumeral(symbol);
        node.number = new BigDecimal(numberBuilder.toString(), mathContext);
    }

    /**
     * Appends a numberal to the class's list of numerals
     *
     * @param symbol symbol to be added to the list of numerals
     * @return returns true if the numeral was added; otherwise, false
     * @throws IllegalArgumentException
     */
    private void appendNumeral(MathSymbol symbol) {
        if (!numerals.contains(symbol)) {
            throw new IllegalArgumentException(symbol.toString() + " is not a numeral");
        }
        if (numberBuilder.length() > 0) {
            // If there is already a decimal in the number
            if (symbol.equals(MathSymbol.DECIMAL) && numberBuilder.lastIndexOf(MathSymbol.DECIMAL.toString()) > -1) {
                throw new IllegalArgumentException(symbol.toString() + " there is already a decimal in the number");
            }
            if (symbol.equals(MathSymbol.ZERO)) {
            }
            // If there was already a leading zero in the number
            if (symbol.equals(MathSymbol.ZERO) && numberBuilder.length() == 1 && numberBuilder.lastIndexOf(MathSymbol.ZERO.toString()) > -1) {
                throw new IllegalArgumentException(symbol.toString() + " there is already a leading zero in the number");
            }
        }
        numberBuilder.append(symbol);
    }

    /**
     * Deletes a given node, preserving the tree by stitching the child of the given node with its parent (left child only for a binary operator)
     *
     * @param node node to be deleted
     */
    private void deleteNode(@NonNull ExpressionNode node) {

        // If deleting the previous node, need to find next oldest previous node
        if (previous.equals(node)) {
            previous = findNextOldestNode(previous);
        }

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
        evaluateToRoot(previous);
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
}