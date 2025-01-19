package ahodanenok.craftinginterpreters.lox;

public final class RpnPrinter implements Expression.Visitor<Void> {

    public static void main(String... args) {
        Expression expression = new Expression.Binary(
            new Token(TokenType.STAR, "*", null, 1),
            new Expression.Grouping(
                new Expression.Binary(
                    new Token(TokenType.PLUS, "+", null, 1),
                    new Expression.Literal(1),
                    new Expression.Literal(2)
                )
            ),
            new Expression.Grouping(
                new Expression.Binary(
                    new Token(TokenType.SLASH, "/", null, 1),
                    new Expression.Literal(3),
                    new Expression.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expression.Literal(4)
                    )
                )
            )
        );
        System.out.println(new RpnPrinter().print(expression));
    }

    private final StringBuilder sb = new StringBuilder();

    public String print(Expression expression) {
        expression.accept(this);
        return sb.toString();
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        if (expression.value != null) {
            sb.append(expression.value.toString());
        } else {
            sb.append("nil");
        }

        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        rpn(expression.operator.lexeme, expression.expression);
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        rpn(expression.operator.lexeme, expression.left, expression.right);
        return null;
    }

    @Override
    public Void visitTernaryExpression(Expression.Ternary expression) {
        rpn("?:", expression.condition, expression.left, expression.right);
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        expression.expression.accept(this);
        return null;
    }

    private void rpn(String action, Expression... expressions) {
        for (int i = 0; i < expressions.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            expressions[i].accept(this);
        }
        sb.append(" ").append(action);
    }
}
