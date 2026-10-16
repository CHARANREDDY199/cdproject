# Expression Evaluator / Calculator Compiler

A full-stack Compiler Design project using **React + Vite** for the frontend and **Java Spring Boot** for the backend.

## Features
- Lexical analysis: displays tokens
- Syntax analysis: recursive-descent parser with operator precedence
- Abstract Syntax Tree (AST): returned as JSON and shown in the UI
- Semantic/runtime checks: invalid expressions, mismatched parentheses, division by zero
- Operators: `+`, `-`, `*`, `/`, `^`, unary `+` and `-`
- Parentheses and decimal numbers
- Recent expression history in the browser
- Clear error messages

## Requirements
- Java 17 or newer
- Maven 3.8+ (or use the Maven wrapper if you add one)
- Node.js 18+ and npm

## 1. Start the backend
Open a terminal in `backend`:

```bash
mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

Health check: `http://localhost:8080/api/health`

## 2. Start the frontend
Open a second terminal in `frontend`:

```bash
npm install
npm run dev
```

Open the URL printed by Vite, usually `http://localhost:5173`.

## Example
Input:
```text
(2 + 3) * 4 ^ 2
```
Expected result: `80`

The exponent operator is right-associative, so `2^3^2` means `2^(3^2)`.

## API
`POST http://localhost:8080/api/evaluate`

Request:
```json
{ "expression": "(2 + 3) * 4" }
```

Response includes `expression`, `result`, `tokens`, and `ast`.

## Project structure
```text
Expression-Evaluator-Calculator-Compiler/
├── backend/
│   ├── pom.xml
│   └── src/main/java/com/example/calculator/
│       ├── CalculatorApplication.java
│       ├── controller/CalculatorController.java
│       ├── dto/EvaluateRequest.java
│       ├── dto/EvaluateResponse.java
│       ├── service/ExpressionEngine.java
│       └── service/ExpressionException.java
└── frontend/
    ├── package.json
    ├── index.html
    ├── vite.config.js
    └── src/
        ├── main.jsx
        ├── App.jsx
        └── style.css
```

## Troubleshooting
- If `mvn` is not recognized, install Maven or open the backend in IntelliJ IDEA and run `CalculatorApplication`.
- If port 8080 is busy, change `server.port` in `backend/src/main/resources/application.properties` and update the frontend API URL.
- If PowerShell blocks `npm.ps1`, use `npm.cmd install` and `npm.cmd run dev`.
