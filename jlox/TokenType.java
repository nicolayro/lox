package jlox;

/**
 * Lox interpreter written in Java. The entire implementation is contained within this one file, to give a overview of
 * everything an interpreter contains.
 * <p>
 * Written by Nicolay Caspersen Roness
 * October 2023
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
