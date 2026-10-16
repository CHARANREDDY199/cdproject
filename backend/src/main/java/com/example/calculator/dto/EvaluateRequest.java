package com.example.calculator.dto;

import jakarta.validation.constraints.NotBlank;

public class EvaluateRequest {
    @NotBlank(message = "Expression cannot be empty.")
    private String expression;

    public EvaluateRequest() {}
    public String getExpression() { return expression; }
    public void setExpression(String expression) { this.expression = expression; }
}
