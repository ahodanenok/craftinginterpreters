package ahodanenok.craftinginterpreters.lox;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ParserTest {

    @Test
    public void testParseExpression_True() {
        List<Token> tokens = List.of(
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, expression);
        assertEquals(true, literal.value);
    }

    @Test
    public void testParseExpression_False() {
        List<Token> tokens = List.of(
            new Token(TokenType.FALSE, "false", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, expression);
        assertEquals(false, literal.value);
    }

    @Test
    public void testParseExpression_Null() {
        List<Token> tokens = List.of(
            new Token(TokenType.NIL, "nil", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, expression);
        assertEquals(null, literal.value);
    }

    @Test
    public void testParseExpression_String() {
        List<Token> tokens = List.of(
            new Token(TokenType.STRING, "Hello, world!", "Hello, world!", 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, expression);
        assertEquals("Hello, world!", literal.value);
    }

    @Test
    public void testParseExpression_Number() {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "123", 123.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, expression);
        assertEquals(123.0, literal.value);
    }

    @Test
    public void testParseExpression_Unary_Negation() {
        List<Token> tokens = List.of(
            new Token(TokenType.BANG, "!", null, 1),
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Unary unary = assertInstanceOf(
            Expression.Unary.class, expression);
        assertEquals(unary.operator.type, TokenType.BANG);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, unary.expression);
        assertEquals(true, literal.value);
    }

    @Test
    public void testParseExpression_Unary_Minus() {
        List<Token> tokens = List.of(
            new Token(TokenType.MINUS, "-", null, 1),
            new Token(TokenType.NUMBER, "5", 5.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Unary unary = assertInstanceOf(
            Expression.Unary.class, expression);
        assertEquals(unary.operator.type, TokenType.MINUS);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, unary.expression);
        assertEquals(5.0, literal.value);
    }

    @Test
    public void testParseExpression_Unary_Grouping() {
        List<Token> tokens = List.of(
            new Token(TokenType.MINUS, "-", null, 1),
            new Token(TokenType.LEFT_PAREN, "(", null, 1),
            new Token(TokenType.FALSE, "false", null, 1),
            new Token(TokenType.RIGHT_PAREN, ")", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Unary unary = assertInstanceOf(
            Expression.Unary.class, expression);
        assertEquals(unary.operator.type, TokenType.MINUS);
        Expression.Grouping group = assertInstanceOf(
            Expression.Grouping.class, unary.expression);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, group.expression);
        assertEquals(false, literal.value);
    }

    @Test
    public void testParseExpression_Factor_Division() {
        List<Token> tokens = List.of(
            new Token(TokenType.STRING, "foo", "foo", 1),
            new Token(TokenType.SLASH, "/", null, 1),
            new Token(TokenType.NUMBER, "12", 12.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(
            Expression.Binary.class, expression);
        assertEquals(binary.operator.type, TokenType.SLASH);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, binary.left);
        assertEquals("foo", literal.value);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals(12.0, literal.value);
    }

    @Test
    public void testParseExpression_Factor_Multiplication() {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "5", 5.0, 1),
            new Token(TokenType.STAR, "*", null, 1),
            new Token(TokenType.TRUE, "true", null, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(
            Expression.Binary.class, expression);
        assertEquals(binary.operator.type, TokenType.STAR);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, binary.left);
        assertEquals(5.0, literal.value);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals(true, literal.value);
    }

    @Test
    public void testParseExpression_Factor_Chain() {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(TokenType.STAR, "*", null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(TokenType.STAR, "*", null, 1),
            new Token(TokenType.NUMBER, "3", 3.0, 1),
            new Token(TokenType.STAR, "*", null, 1),
            new Token(TokenType.NUMBER, "4", 4.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(
            Expression.Binary.class, expression);
        assertEquals(binary.operator.type, TokenType.STAR);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals(4.0, literal.value);

        binary = assertInstanceOf(
            Expression.Binary.class, binary.left);
        assertEquals(binary.operator.type, TokenType.STAR);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals(3.0, literal.value);

        binary = assertInstanceOf(
            Expression.Binary.class, binary.left);
        assertEquals(binary.operator.type, TokenType.STAR);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.left);
        assertEquals(1.0, literal.value);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals(2.0, literal.value);
    }

     @Test
    public void testParseExpression_Term() {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "10", 10.0, 1),
            new Token(TokenType.PLUS, "+", null, 1),
            new Token(TokenType.NUMBER, "20", 20.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(
            Expression.Binary.class, expression);
        assertEquals(binary.operator.type, TokenType.PLUS);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, binary.left);
        assertEquals(10.0, literal.value);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals(20.0, literal.value);
    }

    @Test
    public void testParseExpression_Comparison() {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "x", "x", 1),
            new Token(TokenType.GREATER_EQUAL, ">=", null, 1),
            new Token(TokenType.NUMBER, "y", "y", 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(
            Expression.Binary.class, expression);
        assertEquals(binary.operator.type, TokenType.GREATER_EQUAL);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, binary.left);
        assertEquals("x", literal.value);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals("y", literal.value);
    }

    @Test
    public void testParseExpression_Equality() {
        List<Token> tokens = List.of(
            new Token(TokenType.NUMBER, "1", 1.0, 1),
            new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
            new Token(TokenType.NUMBER, "2", 2.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(
            Expression.Binary.class, expression);
        assertEquals(binary.operator.type, TokenType.EQUAL_EQUAL);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, binary.left);
        assertEquals(1.0, literal.value);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals(2.0, literal.value);
    }

    @Test
    public void testParseExpression_Comma() {
        List<Token> tokens = List.of(
            new Token(TokenType.STRING, "foo", "foo", 1),
            new Token(TokenType.COMMA, ",", null, 1),
            new Token(TokenType.NUMBER, "10", 10.0, 1),
            new Token(TokenType.EOF, "", null, 1));

        Expression expression = new Parser(tokens).parse();

        Expression.Binary binary = assertInstanceOf(
            Expression.Binary.class, expression);
        assertEquals(binary.operator.type, TokenType.COMMA);
        Expression.Literal literal = assertInstanceOf(
            Expression.Literal.class, binary.left);
        assertEquals("foo", literal.value);
        literal = assertInstanceOf(
            Expression.Literal.class, binary.right);
        assertEquals(10.0, literal.value);
    }
}
