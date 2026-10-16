package com.example.calculator.dto;

import java.util.List;
import java.util.Map;

public class EvaluateResponse {
    private String expression;
    private double result;
    private List<Map<String, String>> tokens;
    private Map<String, Object> ast;

    public EvaluateResponse(String expression, double result,
                            List<Map<String, String>> tokens,
                            Map<String, Object> ast) {
        this.expression = expression;
        this.result = result;
        this.tokens = tokens;
        this.ast = ast;
    }

    public String getExpression() { return expression; }
    public double getResult() { return result; }
    public List<Map<String, String>> getTokens() { return tokens; }
    public Map<String, Object> getAst() { return ast; }
}
