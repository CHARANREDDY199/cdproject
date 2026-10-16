package com.example.calculator.controller;

import com.example.calculator.dto.EvaluateRequest;
import com.example.calculator.service.ExpressionEngine;
import com.example.calculator.service.ExpressionException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class CalculatorController {
    private final ExpressionEngine engine;

    public CalculatorController(ExpressionEngine engine) {
        this.engine = engine;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "application", "Expression Evaluator / Calculator Compiler");
    }

    @PostMapping("/evaluate")
    public Map<String, Object> evaluate(@Valid @RequestBody EvaluateRequest request) {
        return engine.evaluate(request.getExpression());
    }

    @ExceptionHandler(ExpressionException.class)
    public ResponseEntity<Map<String, String>> handleExpression(ExpressionException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Please enter a mathematical expression."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOther(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unexpected server error. Check the backend terminal."));
    }
}
