package ahodanenok.craftinginterpreters.lox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpreterTest {

    @Test
    public void testEvaluate_Literal() {
        Expression.Literal literal = new Expression.Literal("test");
        Object value = new Interpreter().visitLiteralExpression(literal);
        assertEquals("test", value);
    }

    @Test
    public void testEvaluate_Unary() {
        Expression.Unary unary = new Expression.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expression.Literal(10.0));
        Object value = new Interpreter().visitUnaryExpression(unary);
        assertEquals(-10.0, value);
    }

    @Test
    public void testEvaluate_Binary() {
        Expression.Binary binary = new Expression.Binary(
            new Token(TokenType.PLUS, "+", null, 1),
            new Expression.Literal("a"),
            new Expression.Literal("b"));
        Object value = new Interpreter().visitBinaryExpression(binary);
        assertEquals("ab", value);
    }

    @Test
    public void testEvaluate_Ternary() {
        Expression.Ternary ternary = new Expression.Ternary(
            new Expression.Literal(true),
            new Expression.Literal(1.0),
            new Expression.Literal(2.0));
        Object value = new Interpreter().visitTernaryExpression(ternary);
        assertEquals(1.0, value);
    }

    @Test
    public void testEvaluate_Grouping() {
        Expression.Grouping grouping =
            new Expression.Grouping(new Expression.Literal(false));
        Object value = new Interpreter().visitGroupingExpression(grouping);
        assertEquals(false, value);
    }
}
