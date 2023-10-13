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
 * Lox interpreter written in Java. It's written in a sort of procedural style.
 *
 * @Author Nicolay Caspersen Roness
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
        while (!isDone()) {
            start = curr;

            char c = next();
            switch (c) {
                case ',' -> addToken(TokenType.COMMA);
                case '.' -> addToken(TokenType.DOT);
                case '+' -> addToken(TokenType.PLUS);
                case '-' -> addToken(TokenType.MINUS);
                case '*' -> addToken(TokenType.STAR);
                case '/' -> {
                    if (match('/')) {
                        while (peek() != '\n' && !isDone()) next();
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
                    while (peek() != '"' && !isDone()) {
                        if (peek() == '\n') line++;
                        next();
                    }

                    if (!isDone()) {
                        next(); // Include ending '"'
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
                        while (isDigit(peek())) next();

                        if (peek() == '.' && isDigit(peekNext())) {
                            next();
                            while (isDigit(peek())) next();
                        }

                        Double number = Double.parseDouble(source.substring(start, curr));
                        addToken(TokenType.NUMBER, number);
                    } else if(isAlpha(c)) {
                        while (isAlphaOrNumeric(peek())) next();

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

    private char next() {
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
        if (isDone()) {
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

    private boolean isDone() {
        return curr >= source.length();
    }

    private boolean match(char expected) {
        if (isDone() || source.charAt(curr) != expected) {
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

    private boolean isAlphaOrNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}

public class Jlox {
    static boolean hadError = false;

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

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String location, String message) {
        System.err.printf(
                "[line %d] Error %s: %s%n", line, location, message
        );
        hadError = true;
    }

}
