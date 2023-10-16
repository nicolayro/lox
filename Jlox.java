import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lox interpreter written in Java. The entire implementation is contained within this one file, to give a overview of
 * everything an interpreter contains.
 *
 * Written by Nicolay Caspersen Roness
 *      October 2023
 */

enum TokenType {
    // Literals
    IDENTIFIER, STRING, NUMBER,
    // Keywords
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,
    // Single-character tokens
    SEMICOLON, COMMA, DOT, SLASH, STAR, MINUS, PLUS,
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    // One or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    EOF
}

class Token {
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}

class Lexer {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("and",    TokenType.AND);
        KEYWORDS.put("class",  TokenType.CLASS);
        KEYWORDS.put("else",   TokenType.ELSE);
        KEYWORDS.put("false",  TokenType.FALSE);
        KEYWORDS.put("for",    TokenType.FOR);
        KEYWORDS.put("fun",    TokenType.FUN);
        KEYWORDS.put("if",     TokenType.IF);
        KEYWORDS.put("nil",    TokenType.NIL);
        KEYWORDS.put("or",     TokenType.OR);
        KEYWORDS.put("print",  TokenType.PRINT);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("super",  TokenType.SUPER);
        KEYWORDS.put("this",   TokenType.THIS);
        KEYWORDS.put("true",   TokenType.TRUE);
        KEYWORDS.put("var",    TokenType.VAR);
        KEYWORDS.put("while",  TokenType.WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int curr = 0;
    private int line = 1;

    Lexer(String source) {
        this.source = source;
    }

    List<Token> lexTokens() {
        while (!isAtEnd()) {
            start = curr;

            char c = nextToken();
            switch (c) {
                case ',' -> addToken(TokenType.COMMA);
                case '.' -> addToken(TokenType.DOT);
                case '+' -> addToken(TokenType.PLUS);
                case '-' -> addToken(TokenType.MINUS);
                case '*' -> addToken(TokenType.STAR);
                case '/' -> {
                    if (match('/')) {
                        while (peek() != '\n' && !isAtEnd()) nextToken();
                    } else {
                        addToken(TokenType.SLASH);
                    }
                }
                case ';' -> addToken(TokenType.SEMICOLON);
                case '(' -> addToken(TokenType.LEFT_PAREN);
                case ')' -> addToken(TokenType.RIGHT_PAREN);
                case '{' -> addToken(TokenType.LEFT_BRACE);
                case '}' -> addToken(TokenType.RIGHT_BRACE);
                case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL: TokenType.GREATER);
                case '"' -> {
                    while (peek() != '"' && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        nextToken();
                    }

                    if (!isAtEnd()) {
                        nextToken(); // Include ending '"'
                        String str = source.substring(start + 1, curr - 1);
                        addToken(TokenType.STRING, str);
                    } else {
                        Jlox.error(line, "Unterminated string.");
                    }
                }
                case '\n' -> line++;
                case ' ', '\r', '\t' -> {} // Ignore whitespace
                default -> {
                    // After lexing all tokens, we are left with a number, keyword, variable definition or error
                    if (isDigit(c)) {
                        while (isDigit(peek())) nextToken();

                        if (peek() == '.' && isDigit(peekNext())) {
                            nextToken();
                            while (isDigit(peek())) nextToken();
                        }

                        Double number = Double.parseDouble(source.substring(start, curr));
                        addToken(TokenType.NUMBER, number);
                    } else if(isAlpha(c)) {
                        while (isAlphaNumeric(peek())) nextToken();

                        String str = source.substring(start, curr);
                        TokenType type = KEYWORDS.getOrDefault(str, TokenType.IDENTIFIER);
                        addToken(type);
                    } else {
                        Jlox.error(line, "Unexpected character " + c + ".");
                    }
                }
            }
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private char nextToken() {
        return source.charAt(curr++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, curr);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(curr);
    }

    private char peekNext() {
        if (curr + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(curr + 1);
    }

    private boolean isAtEnd() {
        return curr >= source.length();
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(curr) != expected) {
            return false;
        }
        curr++;
        return true;
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}

abstract class Expr {
    abstract <R> R accept(Visitor<R> visitor);
    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitUnaryExpr(Unary expr);
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
    }

    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }

    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }
}

class RuntimeError extends RuntimeException {
    final Token token;
    public RuntimeError(Token token, String msg) {
        super(msg);
        this.token = token;
    }
}

class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int curr = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    private Expr expression() {
        return equality();
    }

    private Stmt statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        }

        return expressionStatement();
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr equality () {
        Expr expr = comparison();

        while (match(TokenType.BANG, TokenType.BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression.");
    }

    private Token consume(TokenType type, String msg) {
        if (isCurrType(type)) {
            return next();
        }

        throw error(peek(), msg);
    }

    private ParseError error(Token token, String msg) {
        Jlox.error(token, msg);
        return new ParseError();
    }

    private void synchronize() {
        next();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> {
                    return;
                }
            }

            next();
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (isCurrType(type)) {
                next();
                return true;
            }
        }
        return false;
    }

    private Token next() {
        if (!isAtEnd()) {
            curr++;
        }
        return previous();
    }

    private boolean isCurrType(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(curr);
    }

    private Token previous() {
        return tokens.get(curr - 1);
    }

}


abstract class Stmt {
    abstract <R> R accept(Visitor<R> visitor);
    interface Visitor<R> {
        R visitExpressionStmt(Expression stmt);
        R visitPrintStmt(Print stmt);
    }

    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }

    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        final Expr expression;
    }

}

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Jlox.runtimeError(error);
        }
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr);

        return switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperand(expr.operator, right);
                yield -(double) right;
            }
            case BANG -> !isTruthy(right);
            default -> null;
        };
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left * (double) right;
            }
            case PLUS -> {
                if (left instanceof Double && right instanceof Double) {
                    yield (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    yield (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            }
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left <= (double) right;
            }
            case BANG_EQUAL -> !isEqual(left, right);
            case EQUAL_EQUAL -> isEqual(left, right);
            default -> null;
        };
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (boolean) o;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }
}

public class Jlox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: jlox <script>");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }

        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.lexTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) {
            return;
        }

        interpreter.interpret(statements);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }


    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error) {
        System.err.printf("%s\n[line %d]", error.getMessage(), error.token.line);
        hadRuntimeError = true;
    }

    private static void report(int line, String location, String message) {
        System.err.printf(
                "[line %d] Error %d: %s%n", line, location, message
        );
        hadError = true;
    }

}
