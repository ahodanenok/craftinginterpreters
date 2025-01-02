package ahodanenok.craftinginterpreters.lox;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScannerTest {

    @Test
    public void testEmptyStringNoTokens() {
        List<Token> tokens = new Scanner("").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(0));
    }

    @Test
    public void testSpaceIgnored() {
        List<Token> tokens = new Scanner(" ").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(0));
    }

    @Test
    public void testTabIgnored() {
        List<Token> tokens = new Scanner("\t").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(0));
    }

    @Test
    public void testNewLineIgnored() {
        List<Token> tokens = new Scanner("\n").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 2), tokens.get(0));
    }

    @Test
    public void testCarriageReturnIgnored() {
        List<Token> tokens = new Scanner("\r").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(0));
    }

    @Test
    public void testWhitespacesIgnored() {
        List<Token> tokens = new Scanner(" \r\n\r \t\n  \t ").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 3), tokens.get(0));
    }

    @Test
    public void testInlineCommentIgnored() {
        List<Token> tokens = new Scanner("// some comment").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(0));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        (      , LEFT_PAREN
        )      , RIGHT_PAREN
        {      , LEFT_BRACE
        }      , RIGHT_BRACE
        ','    , COMMA
        .      , DOT
        -      , MINUS
        +      , PLUS
        /      , SLASH
        *      , STAR
        ;      , SEMICOLON
        !      , BANG
        !=     , BANG_EQUAL
        =      , EQUAL
        ==     , EQUAL_EQUAL
        >      , GREATER
        >=     , GREATER_EQUAL
        <      , LESS
        <=     , LESS_EQUAL
        and    , AND
        class  , CLASS
        else   , ELSE
        false  , FALSE
        fun    , FUN
        for    , FOR
        if     , IF
        nil    , NIL
        or     , OR
        print  , PRINT
        return , RETURN
        super  , SUPER
        this   , THIS
        true   , TRUE
        var    , VAR
        while  , WHILE
    """)
    public void testRecognizeStaticToken(String str, TokenType tokenType) {
        List<Token> tokens = new Scanner(str).scan();
        assertEquals(2, tokens.size());
        assertTokenEquals(new Token(tokenType, str, null, 1), tokens.get(0));
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0", "0.0", "0.000005123", "1", "2.000", "23.4567", "782420.29581203"
    })
    public void testRecognizeNumber(String str) {
        double n = Double.parseDouble(str);
        List<Token> tokens = new Scanner(str).scan();
        assertEquals(2, tokens.size());
        assertTokenEquals(new Token(TokenType.NUMBER, str, n, 1), tokens.get(0));
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(1));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        "", 1
        "A", 1
        "123", 1
        "hello world", 1
        '"   "', 1
        '"text\nwith\nnew lines"', 3
        """)
    public void testRecognizeString(String str, int line) {
        List<Token> tokens = new Scanner(str).scan();
        assertEquals(2, tokens.size());
        assertTokenEquals(new Token(TokenType.STRING, str, str.substring(1, str.length() - 1), line), tokens.get(0));
        assertTokenEquals(new Token(TokenType.EOF, "", null, line), tokens.get(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "a", "b2", "foo_123", "andy", "funny", "isDone", "_protected"
    })
    public void testRecognizeIdentifier(String str) {
        List<Token> tokens = new Scanner(str).scan();
        assertEquals(2, tokens.size());
        assertTokenEquals(new Token(TokenType.IDENTIFIER, str, str, 1), tokens.get(0));
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(1));
    }

    private void assertTokenEquals(Token expected, Token actual) {
        assertEquals(expected.type, actual.type, "Token types doesn't match");
        assertEquals(expected.lexeme, actual.lexeme, "Token lexemes doesn't match");
        assertEquals(expected.literal, actual.literal, "Token literal doesn't match");
        assertEquals(expected.line, actual.line, "Token lines doesn't match");
    }
}
