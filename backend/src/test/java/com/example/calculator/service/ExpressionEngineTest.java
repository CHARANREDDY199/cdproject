package com.example.calculator.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExpressionEngineTest {
    private final ExpressionEngine engine = new ExpressionEngine();

    @Test void respectsPrecedence() {
        assertEquals(14.0, (double) engine.evaluate("2 + 3 * 4").get("result"), 0.000001);
    }

    @Test void supportsParenthesesAndPower() {
        assertEquals(80.0, (double) engine.evaluate("(2 + 3) * 4 ^ 2").get("result"), 0.000001);
    }

    @Test void powerIsRightAssociative() {
        assertEquals(512.0, (double) engine.evaluate("2^3^2").get("result"), 0.000001);
    }

    @Test void rejectsDivisionByZero() {
        assertThrows(ExpressionException.class, () -> engine.evaluate("4 / 0"));
    }

    @Test void rejectsMismatchedParentheses() {
        assertThrows(ExpressionException.class, () -> engine.evaluate("(2 + 3"));
    }
}
