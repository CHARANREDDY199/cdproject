package com.example.calculator.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ExpressionEngine {
    private enum Type { NUMBER, PLUS, MINUS, STAR, SLASH, CARET, LPAREN, RPAREN, EOF }

    private record Token(Type type, String lexeme, int position) {
        Map<String, String> asMap() {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("type", type.name());
            m.put("lexeme", lexeme);
            m.put("position", String.valueOf(position));
            return m;
        }
    }

    private static class Node {
        String kind;
        String value;
        Node left;
        Node right;

        Node(String kind, String value, Node left, Node right) {
            this.kind = kind; this.value = value; this.left = left; this.right = right;
        }

        Map<String, Object> asMap() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kind", kind);
            m.put("value", value);
            if (left != null) m.put("left", left.asMap());
            if (right != null) m.put("right", right.asMap());
            return m;
        }
    }

    private List<Token> tokens;
    private int current;

    public Map<String, Object> evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new ExpressionException("Please enter a mathematical expression.");
        }
        tokens = tokenize(expression);
        current = 0;
        Node root = expression();
        if (!check(Type.EOF)) {
            Token t = peek();
            throw error("Unexpected token '" + t.lexeme + "'", t.position);
        }
        double result = calculate(root);
        if (!Double.isFinite(result)) {
            throw new ExpressionException("The result is outside the supported numeric range.");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("expression", expression);
        response.put("result", result);
        List<Map<String, String>> tokenMaps = new ArrayList<>();
        for (Token t : tokens) {
            if (t.type != Type.EOF) tokenMaps.add(t.asMap());
        }
        response.put("tokens", tokenMaps);
        response.put("ast", root.asMap());
        return response;
    }

    private List<Token> tokenize(String input) {
        List<Token> out = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }
            int start = i;
            if (Character.isDigit(c) || (c == '.' && i + 1 < input.length()
                    && Character.isDigit(input.charAt(i + 1)))) {
                boolean dot = false;
                while (i < input.length()) {
                    char n = input.charAt(i);
                    if (Character.isDigit(n)) { i++; }
                    else if (n == '.' && !dot) { dot = true; i++; }
                    else break;
                }
                String number = input.substring(start, i);
                try {
                    double value = Double.parseDouble(number);
                    if (!Double.isFinite(value)) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    throw error("Invalid number '" + number + "'", start);
                }
                out.add(new Token(Type.NUMBER, number, start));
                continue;
            }
            Type type = switch (c) {
                case '+' -> Type.PLUS;
                case '-' -> Type.MINUS;
                case '*' -> Type.STAR;
                case '/' -> Type.SLASH;
                case '^' -> Type.CARET;
                case '(' -> Type.LPAREN;
                case ')' -> Type.RPAREN;
                default -> null;
            };
            if (type == null) throw error("Invalid character '" + c + "'", i);
            out.add(new Token(type, String.valueOf(c), i));
            i++;
        }
        out.add(new Token(Type.EOF, "", input.length()));
        return out;
    }

    // Grammar:
    // expression -> term (('+' | '-') term)*
    // term       -> unary (('*' | '/') unary)*
    // unary      -> ('+' | '-') unary | power
    // power      -> primary ('^' unary)?
    // primary    -> NUMBER | '(' expression ')'
    private Node expression() {
        Node node = term();
        while (match(Type.PLUS, Type.MINUS)) {
            Token op = previous();
            Node right = term();
            node = new Node("BinaryExpression", op.lexeme, node, right);
        }
        return node;
    }

    private Node term() {
        Node node = unary();
        while (match(Type.STAR, Type.SLASH)) {
            Token op = previous();
            Node right = unary();
            node = new Node("BinaryExpression", op.lexeme, node, right);
        }
        return node;
    }

    private Node unary() {
        if (match(Type.PLUS, Type.MINUS)) {
            Token op = previous();
            return new Node("UnaryExpression", op.lexeme, null, unary());
        }
        return power();
    }

    private Node power() {
        Node node = primary();
        if (match(Type.CARET)) {
            Token op = previous();
            node = new Node("BinaryExpression", op.lexeme, node, unary());
        }
        return node;
    }

    private Node primary() {
        if (match(Type.NUMBER)) return new Node("NumberLiteral", previous().lexeme, null, null);
        if (match(Type.LPAREN)) {
            Node node = expression();
            if (!match(Type.RPAREN)) {
                throw error("Missing closing parenthesis ')'", peek().position);
            }
            return node;
        }
        if (check(Type.RPAREN)) throw error("Unexpected closing parenthesis ')'", peek().position);
        if (check(Type.EOF)) throw error("Expression ended unexpectedly", peek().position);
        throw error("Expected a number or '('", peek().position);
    }

    private double calculate(Node n) {
        if (n.kind.equals("NumberLiteral")) return Double.parseDouble(n.value);
        if (n.kind.equals("UnaryExpression")) {
            double v = calculate(n.right);
            return n.value.equals("-") ? -v : v;
        }
        double a = calculate(n.left), b = calculate(n.right);
        return switch (n.value) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (b == 0.0) throw new ExpressionException("Division by zero is not allowed.");
                yield a / b;
            }
            case "^" -> Math.pow(a, b);
            default -> throw new ExpressionException("Unsupported operator: " + n.value);
        };
    }

    private boolean match(Type... types) {
        for (Type type : types) {
            if (check(type)) { advance(); return true; }
        }
        return false;
    }
    private boolean check(Type type) { return peek().type == type; }
    private Token advance() { if (!check(Type.EOF)) current++; return previous(); }
    private Token peek() { return tokens.get(current); }
    private Token previous() { return tokens.get(current - 1); }
    private ExpressionException error(String message, int position) {
        return new ExpressionException(message + " at position " + (position + 1) + ".");
    }
}
