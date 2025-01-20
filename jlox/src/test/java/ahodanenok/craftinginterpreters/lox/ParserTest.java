package ahodanenok.craftinginterpreters.lox;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ParserTest {

    @Test
    public void testParseExpression_True() {
        List<Token> tokens = List.of(
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(true, assertInstanceOf(Expression.Literal.class, expression).value);
    }

    @Test
    public void testParseExpression_False() {
        List<Token> tokens = List.of(
            new Token(TokenType.FALSE, "false", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(false, assertInstanceOf(Expression.Literal.class, expression).value);
    }

    @Test
    public void testParseExpression_Null() {
        List<Token> tokens = List.of(
            new Token(TokenType.NIL, "nil", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(null, assertInstanceOf(Expression.Literal.class, expression).value);
    }

    @Test
    public void testParseExpression_String() {
        List<Token> tokens = List.of(
            new Token(TokenType.STRING, "Hello, world!", "Hello, world!", 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals("Hello, world!", assertInstanceOf(Expression.Literal.class, expression).value);
    }

    @Test
    public void testParseExpression_Number() {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "123", 123.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(123.0, assertInstanceOf(Expression.Literal.class, expression).value);
    }

    @Test
    public void testParseExpression_Grouping() {
        List<Token> tokens = List.of(
            new Token(TokenType.LEFT_PAREN, "(", null, 1),
            new Token(TokenType.NUMBER, "123", 123.0, 1),
            new Token(TokenType.RIGHT_PAREN, ")", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Grouping grouping = assertInstanceOf(Expression.Grouping.class, expression);
        assertEquals(123.0, assertInstanceOf(Expression.Literal.class, grouping.expression).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        BANG,   !
        MINUS,  -
    """)
    public void testParseExpression_Unary(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Unary unary = assertInstanceOf(Expression.Unary.class, expression);
        assertEquals(unary.operator.type, tokenType);
        assertEquals(true, assertInstanceOf(Expression.Literal.class, unary.expression).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        BANG,   !
        MINUS,  -
    """)
    public void testParseExpression_Unary_Chain(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(tokenType, lexeme, null, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Unary unary_1 = assertInstanceOf(Expression.Unary.class, expression);
        assertEquals(unary_1.operator.type, tokenType);

        Expression.Unary unary_2 = assertInstanceOf(Expression.Unary.class, unary_1.expression);
        assertEquals(unary_2.operator.type, tokenType);

        Expression.Unary unary_3 = assertInstanceOf(Expression.Unary.class, unary_2.expression);
        assertEquals(unary_3.operator.type, tokenType);

        Expression.Unary unary_4 = assertInstanceOf(Expression.Unary.class, unary_3.expression);
        assertEquals(unary_4.operator.type, tokenType);
        assertEquals(true, assertInstanceOf(Expression.Literal.class, unary_4.expression).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        STAR,   *
        SLASH,  /
    """)
    public void testParseExpression_Factor(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "5", 5.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary.operator.type, tokenType);
        assertEquals(5.0, assertInstanceOf(Expression.Literal.class, binary.left).value);
        assertEquals(true, assertInstanceOf(Expression.Literal.class, binary.right).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        STAR,   *
        SLASH,  /
    """)
    public void testParseExpression_Factor_Chain(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "3", 3.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "4", 4.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary_1 = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary_1.operator.type, tokenType);
        assertEquals(4.0, assertInstanceOf(Expression.Literal.class, binary_1.right).value);

        Expression.Binary binary_2 = assertInstanceOf(Expression.Binary.class, binary_1.left);
        assertEquals(binary_2.operator.type, tokenType);
        assertEquals(3.0, assertInstanceOf(Expression.Literal.class, binary_2.right).value);

        Expression.Binary binary_3 = assertInstanceOf(Expression.Binary.class, binary_2.left);
        assertEquals(binary_3.operator.type, tokenType);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, binary_3.left).value);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, binary_3.right).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        STAR,   *
        SLASH,  /
    """)
    public void testParseExpression_Factor_MissingLeft(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(null, expression);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        MINUS, -
        PLUS,  +
    """)
    public void testParseExpression_Term(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "10", 10.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "20", 20.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary.operator.type, tokenType);
        assertEquals(10.0, assertInstanceOf(Expression.Literal.class, binary.left).value);
        assertEquals(20.0, assertInstanceOf(Expression.Literal.class, binary.right).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        MINUS, -
        PLUS,  +
    """)
    public void testParseExpression_Term_Chain(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "3", 3.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "4", 4.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary_1 = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary_1.operator.type, tokenType);
        assertEquals(4.0, assertInstanceOf(Expression.Literal.class, binary_1.right).value);

        Expression.Binary binary_2 = assertInstanceOf(Expression.Binary.class, binary_1.left);
        assertEquals(binary_2.operator.type, tokenType);
        assertEquals(3.0, assertInstanceOf(Expression.Literal.class, binary_2.right).value);

        Expression.Binary binary_3 = assertInstanceOf(Expression.Binary.class, binary_2.left);
        assertEquals(binary_3.operator.type, tokenType);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, binary_3.left).value);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, binary_3.right).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        PLUS,  +
    """)
    public void testParseExpression_Term_MissingLeft(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "20", 20.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(null, expression);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        GREATER,        >
        GREATER_EQUAL,  >=
        LESS,           <
        LESS_EQUAL,     <=
    """)
    public void testParseExpression_Comparison(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(TokenType.STRING, "x", "x", 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.STRING, "y", "y", 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary.operator.type, tokenType);
        assertEquals("x", assertInstanceOf(Expression.Literal.class, binary.left).value);
        assertEquals("y", assertInstanceOf(Expression.Literal.class, binary.right).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        GREATER,        >
        GREATER_EQUAL,  >=
        LESS,           <
        LESS_EQUAL,     <=
    """)
    public void testParseExpression_Comparison_Chain(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "3", 3.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "4", 4.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary_1 = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary_1.operator.type, tokenType);
        assertEquals(4.0, assertInstanceOf(Expression.Literal.class, binary_1.right).value);

        Expression.Binary binary_2 = assertInstanceOf(Expression.Binary.class, binary_1.left);
        assertEquals(binary_2.operator.type, tokenType);
        assertEquals(3.0, assertInstanceOf(Expression.Literal.class, binary_2.right).value);

        Expression.Binary binary_3 = assertInstanceOf(Expression.Binary.class, binary_2.left);
        assertEquals(binary_3.operator.type, tokenType);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, binary_3.left).value);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, binary_3.right).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        GREATER,        >
        GREATER_EQUAL,  >=
        LESS,           <
        LESS_EQUAL,     <=
    """)
    public void testParseExpression_Comparison_MissingLeft(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.STRING, "y", "y", 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(null, expression);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        EQUAL_EQUAL,  ==
        BANG_EQUAL,   !=
    """)
    public void testParseExpression_Equality(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary.operator.type, tokenType);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, binary.left).value);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, binary.right).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        EQUAL_EQUAL,  ==
        BANG_EQUAL,   !=
    """)
    public void testParseExpression_Equality_Chain(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "3", 3.0, 1),
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "4", 4.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary_1 = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary_1.operator.type, tokenType);
        assertEquals(4.0, assertInstanceOf(Expression.Literal.class, binary_1.right).value);

        Expression.Binary binary_2 = assertInstanceOf(Expression.Binary.class, binary_1.left);
        assertEquals(binary_2.operator.type, tokenType);
        assertEquals(3.0, assertInstanceOf(Expression.Literal.class, binary_2.right).value);

        Expression.Binary binary_3 = assertInstanceOf(Expression.Binary.class, binary_2.left);
        assertEquals(binary_3.operator.type, tokenType);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, binary_3.left).value);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, binary_3.right).value);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        EQUAL_EQUAL,  ==
        BANG_EQUAL,   !=
    """)
    public void testParseExpression_Equality_MissingLeft(TokenType tokenType, String lexeme) {
        List<Token> tokens = List.of(
            new Token(tokenType, lexeme, null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(null, expression);
    }

    @Test
    public void testParseExpression_Comma() {
        List<Token> tokens = List.of(
            new Token(TokenType.STRING, "foo", "foo", 1),
            new Token(TokenType.COMMA, ",", null, 1),
            new Token(TokenType.NUMBER, "10", 10.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary.operator.type, TokenType.COMMA);
        assertEquals("foo", assertInstanceOf(Expression.Literal.class, binary.left).value);
        assertEquals(10.0, assertInstanceOf(Expression.Literal.class, binary.right).value);
    }

    @Test
    public void testParseExpression_Comma_Chain() {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(TokenType.COMMA, ",", null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(TokenType.COMMA, ",", null, 1),
            new Token(TokenType.NUMBER, "3", 3.0, 1),
            new Token(TokenType.COMMA, ",", null, 1),
            new Token(TokenType.NUMBER, "4", 4.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary_1 = assertInstanceOf(Expression.Binary.class, expression);
        assertEquals(binary_1.operator.type, TokenType.COMMA);
        assertEquals(4.0, assertInstanceOf(Expression.Literal.class, binary_1.right).value);

        Expression.Binary binary_2 = assertInstanceOf(Expression.Binary.class, binary_1.left);
        assertEquals(binary_2.operator.type, TokenType.COMMA);
        assertEquals(3.0, assertInstanceOf(Expression.Literal.class, binary_2.right).value);

        Expression.Binary binary_3 = assertInstanceOf(Expression.Binary.class, binary_2.left);
        assertEquals(binary_3.operator.type, TokenType.COMMA);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, binary_3.left).value);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, binary_3.right).value);
    }

    @Test
    public void testParseExpression_Comma_MissingLeft() {
        List<Token> tokens = List.of(
            new Token(TokenType.COMMA, ",", null, 1),
            new Token(TokenType.NUMBER, "10", 10.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        assertEquals(null, expression);
    }

    @Test
    public void testExpression_Ternary() {
        List<Token> tokens = List.of(
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.QUESTION, "?", null, 1),
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Ternary ternary = assertInstanceOf(Expression.Ternary.class, expression);
        assertEquals(true, assertInstanceOf(Expression.Literal.class, ternary.condition).value);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, ternary.left).value);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, ternary.right).value);
    }

    @Test
    public void testExpression_Ternary_Chain() {
        // 1 ? 2 :  3 ? 4 :  5 ? 6 : 7
        // 1 ? 2 : (3 ? 4 : (5 ? 6 : 7)))
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(TokenType.QUESTION, "?", null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.NUMBER, "3", 3.0, 1),
            new Token(TokenType.QUESTION, "?", null, 1),
            new Token(TokenType.NUMBER, "4", 4.0, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.NUMBER, "5", 5.0, 1),
            new Token(TokenType.QUESTION, "?", null, 1),
            new Token(TokenType.NUMBER, "6", 6.0, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.NUMBER, "7", 7.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Ternary ternary = assertInstanceOf(Expression.Ternary.class, expression);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, ternary.condition).value);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, ternary.left).value);

        ternary = assertInstanceOf(Expression.Ternary.class, ternary.right);
        assertEquals(3.0, assertInstanceOf(Expression.Literal.class, ternary.condition).value);
        assertEquals(4.0, assertInstanceOf(Expression.Literal.class, ternary.left).value);

        ternary = assertInstanceOf(Expression.Ternary.class, ternary.right);
        assertEquals(5.0, assertInstanceOf(Expression.Literal.class, ternary.condition).value);
        assertEquals(6.0, assertInstanceOf(Expression.Literal.class, ternary.left).value);
        assertEquals(7.0, assertInstanceOf(Expression.Literal.class, ternary.right).value);
    }

    @Test
    public void testExpression_Ternary_Nested() {
        // 1 ?  2 ?  3 ? 4 : 5  : 6  :  7 ? 8 : 9
        // 1 ? (2 ? (3 ? 4 : 5) : 6) : (7 ? 8 : 9)
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(TokenType.QUESTION, "?", null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(TokenType.QUESTION, "?", null, 1),
            new Token(TokenType.NUMBER, "3", 3.0, 1),
            new Token(TokenType.QUESTION, "?", null, 1),
            new Token(TokenType.NUMBER, "4", 4.0, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.NUMBER, "5", 5.0, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.NUMBER, "6", 6.0, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.NUMBER, "7", 7.0, 1),
            new Token(TokenType.QUESTION, "?", null, 1),
            new Token(TokenType.NUMBER, "8", 8.0, 1),
            new Token(TokenType.COLON, ":", null, 1),
            new Token(TokenType.NUMBER, "9", 9.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Ternary ternary1 = assertInstanceOf(Expression.Ternary.class, expression);
        assertEquals(1.0, assertInstanceOf(Expression.Literal.class, ternary1.condition).value);

        Expression.Ternary ternary2 = assertInstanceOf(Expression.Ternary.class, ternary1.left);
        assertEquals(2.0, assertInstanceOf(Expression.Literal.class, ternary2.condition).value);
        assertEquals(6.0, assertInstanceOf(Expression.Literal.class, ternary2.right).value);

        Expression.Ternary ternary3 = assertInstanceOf(Expression.Ternary.class, ternary2.left);
        assertEquals(3.0, assertInstanceOf(Expression.Literal.class, ternary3.condition).value);
        assertEquals(4.0, assertInstanceOf(Expression.Literal.class, ternary3.left).value);
        assertEquals(5.0, assertInstanceOf(Expression.Literal.class, ternary3.right).value);

        Expression.Ternary ternary4 = assertInstanceOf(Expression.Ternary.class, ternary1.right);
        assertEquals(7.0, assertInstanceOf(Expression.Literal.class, ternary4.condition).value);
        assertEquals(8.0, assertInstanceOf(Expression.Literal.class, ternary4.left).value);
        assertEquals(9.0, assertInstanceOf(Expression.Literal.class, ternary4.right).value);
    }
}
