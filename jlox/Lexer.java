package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Lexer {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("and", TokenType.AND);
        KEYWORDS.put("class", TokenType.CLASS);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("fun", TokenType.FUN);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("nil", TokenType.NIL);
        KEYWORDS.put("or", TokenType.OR);
        KEYWORDS.put("print", TokenType.PRINT);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("super", TokenType.SUPER);
        KEYWORDS.put("this", TokenType.THIS);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("var", TokenType.VAR);
        KEYWORDS.put("while", TokenType.WHILE);
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
                case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
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
                case ' ', '\r', '\t' -> {
                } // Ignore whitespace
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
                    } else if (isAlpha(c)) {
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
