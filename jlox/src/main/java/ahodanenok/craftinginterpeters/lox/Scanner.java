package ahodanenok.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class Scanner {

    private static final Map<String, TokenType> KEYWORDS;
    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("and", TokenType.AND);
        KEYWORDS.put("class", TokenType.CLASS);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("fun", TokenType.FUN);
        KEYWORDS.put("for", TokenType.FOR);
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
        KEYWORDS.put("break", TokenType.BREAK);
    }

    private final String source;
    private final List<Token> tokens;
    private int current;
    private int start;
    private int line;

    public Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
        this.current = 0;
        this.start = 0;
        this.line = 1;
    }

    public List<Token> scan() {
        while (!isEnded()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));

        return tokens;
    }

    private void scanToken() {
        char ch = advance();
        switch (ch) {
            case ' ' -> {}
            case '\r' -> {}
            case '\t' -> {}
            case '\n' -> line++;
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case '/' -> {
                if (match('/')) {
                    while (!isEnded() && peek() != '\n') {
                        advance();
                    }
                } else if (match('*')) {
                    skipBlockComment();
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            case '*' -> addToken(TokenType.STAR);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '?' -> addToken(TokenType.QUESTION);
            case ':' -> addToken(TokenType.COLON);
            case '"' -> string();
            default -> {
                if (isDigit(ch)) {
                    number();
                } else if (isLetter(ch)) {
                    identifier();
                } else {
                    Lox.error(line, String.format("Unexpected character '%c'", ch));
                }
            }
        }
    }

    private boolean isEnded() {
        return current >= source.length();
    }

    private char advance() {
        if (current >= source.length()) {
            return '\0';
        }

        return source.charAt(current++);
    }

    private boolean match(char ch) {
        if (isEnded()) {
            return false;
        }

        if (source.charAt(current) != ch) {
            return false;
        }

        current++;
        return true;
    }

    private char peek() {
        if (isEnded()) {
            return '\0';
        }

        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }

        return source.charAt(current + 1);
    }

    private String currentLexeme() {
        return source.substring(start, current);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, currentLexeme(), literal, line));
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isLetter(char ch) {
        return (ch >= 'a' && ch <= 'z')
            || (ch >= 'A' && ch <= 'Z')
            || ch == '_';
    }

    private void number() {
        while (isDigit(peek())) {
            advance(); // consume digit
        }

        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume .

            while (isDigit(peek())) {
                advance(); // consume digit
            }
        }

        addToken(TokenType.NUMBER, Double.parseDouble(currentLexeme()));
    }

    private void string() {
        while (!isEnded() && peek() != '"') {
            if (peek() == '\n') {
                line++;
            }

            advance();
        }

        if (peek() != '"') {
            Lox.error(line, "Unterminated string.");
            return;
        }
        advance(); // consume "

        addToken(TokenType.STRING, source.substring(start + 1, current - 1));
    }

    private void identifier() {
        while (isLetter(peek()) || isDigit(peek())) {
            advance(); // consume letter or digit
        }

        String lexeme = currentLexeme();
        if (KEYWORDS.containsKey(lexeme)) {
            addToken(KEYWORDS.get(lexeme));
        } else {
            addToken(TokenType.IDENTIFIER);
        }
    }

    private void skipBlockComment() {
        int level = 0;
        while (!isEnded()) {
            if (peek() == '\n') {
                line++;
            }

            if (peek() == '/' && peekNext() == '*') {
                advance();
                advance();
                level++;
            } else if (peek() == '*' && peekNext() == '/') {
                advance(); // consume *
                advance(); // consume /
                if (level == 0) {
                    break;
                } else {
                    level--;
                }
            } else {
                advance();
            }
        }
    }
}
