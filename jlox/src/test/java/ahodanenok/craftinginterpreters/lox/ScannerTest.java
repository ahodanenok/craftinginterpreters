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
    public void testInlineCommentIgnored_NoContent_OneLine() {
        List<Token> tokens = new Scanner("// some comment").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(0));
    }

    @Test
    public void testInlineCommentIgnored_NoContent_MutiLine() {
        List<Token> tokens = new Scanner(
            """
            // some comment
            // another \
            """
        ).scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 2), tokens.get(0));
    }

    @Test
    public void testInlineCommentsIgnored() {
        List<Token> tokens = new Scanner(
            """
            // abc
            var x = 10;//comment
            // 12345
            return;
            // if
            """
        ).scan();
        assertEquals(8, tokens.size());
        assertTokenEquals(new Token(TokenType.VAR, "var", null, 2), tokens.get(0));
        assertTokenEquals(new Token(TokenType.IDENTIFIER, "x", "x", 2), tokens.get(1));
        assertTokenEquals(new Token(TokenType.EQUAL, "=", null, 2), tokens.get(2));
        assertTokenEquals(new Token(TokenType.NUMBER, "10", 10.0, 2), tokens.get(3));
        assertTokenEquals(new Token(TokenType.SEMICOLON, ";", null, 2), tokens.get(4));
        assertTokenEquals(new Token(TokenType.RETURN, "return", null, 4), tokens.get(5));
        assertTokenEquals(new Token(TokenType.SEMICOLON, ";", null, 4), tokens.get(6));
        assertTokenEquals(new Token(TokenType.EOF, "", null, 6), tokens.get(7));
    }

    @Test
    public void testBlockCommentIgnored_NoContent_OneLine() {
        List<Token> tokens = new Scanner("/* block */").scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(0));
    }

    @Test
    public void testBlockCommentIgnored_NoContent_MultiLine() {
        List<Token> tokens = new Scanner("""
        /* a
         * bb
         * ccc
         */
        """).scan();
        assertEquals(1, tokens.size());
        assertTokenEquals(new Token(TokenType.EOF, "", null, 5), tokens.get(0));
    }

    @Test
    public void testBlockCommentsIgnored() {
        List<Token> tokens = new Scanner("""
        /* hello
         * world
         */
        fun x
        a/* + *//3 /* - y */
        /*
         test */
        """).scan();
        assertEquals(6, tokens.size());
        assertTokenEquals(new Token(TokenType.FUN, "fun", null, 4), tokens.get(0));
        assertTokenEquals(new Token(TokenType.IDENTIFIER, "x", "x", 4), tokens.get(1));
        assertTokenEquals(new Token(TokenType.IDENTIFIER, "a", "a", 5), tokens.get(2));
        assertTokenEquals(new Token(TokenType.SLASH, "/", null, 5), tokens.get(3));
        assertTokenEquals(new Token(TokenType.NUMBER, "3", 3.0, 5), tokens.get(4));
        assertTokenEquals(new Token(TokenType.EOF, "", null, 8), tokens.get(5));
    }

    @Test
    public void testNestedBlockCommentsIgnored() {
        List<Token> tokens = new Scanner("""
        /*
          /* 1 */
          /* 2 /* 2.1 */

            /* 2.2 /* 2.2.1 */ */
         */
        */
        x; /* y
        /* y / * z * /
        */*/
        """).scan();
        assertEquals(3, tokens.size());
        assertTokenEquals(new Token(TokenType.IDENTIFIER, "x", "x", 8), tokens.get(0));
        assertTokenEquals(new Token(TokenType.SEMICOLON, ";", null, 8), tokens.get(1));
        assertTokenEquals(new Token(TokenType.EOF, "", null, 11), tokens.get(2));
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

    @Test
    public void testDontSkipSymbolAfterSlash() {
        List<Token> tokens = new Scanner("a/b").scan();
        assertEquals(4, tokens.size());
        assertTokenEquals(new Token(TokenType.IDENTIFIER, "a", "a", 1), tokens.get(0));
        assertTokenEquals(new Token(TokenType.SLASH, "/", null, 1), tokens.get(1));
        assertTokenEquals(new Token(TokenType.IDENTIFIER, "b", "b", 1), tokens.get(2));
        assertTokenEquals(new Token(TokenType.EOF, "", null, 1), tokens.get(3));
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
