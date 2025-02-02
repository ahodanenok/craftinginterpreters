package ahodanenok.craftinginterpreters.lox;

public class AstPrinter implements Expression.Visitor<String> {

    public static void main(String... args) {
        Expression expression =
            new Expression.Binary(
                new Token(TokenType.STAR, "*", null, 1),
                new Expression.Unary(
                    new Token(TokenType.MINUS, "-", null, 1),
                    new Expression.Literal(123)),
                new Expression.Grouping(
                    new Expression.Literal(45.67)));
        System.out.println(new AstPrinter().print(expression));
    }

    String print(Expression expression) {
        return expression.accept(this);
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        if (expression.value != null) {
            return expression.value.toString();
        } else {
            return "nil";
        }
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parethesize(expression.operator.lexeme, expression.expression);
    }

    @Override
    public String visitBinaryExpression(Expression.Binary expression) {
        return parethesize(expression.operator.lexeme, expression.left, expression.right);
    }

    @Override
    public String visitTernaryExpression(Expression.Ternary expression) {
        return parethesize("?:", expression.condition, expression.left, expression.right);
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expression) {
        return parethesize("group", expression.expression);
    }

    @Override
    public String visitVariableExpression(Expression.Variable expression) {
        return String.format("(variable %s)", expression.name.lexeme);
    }

    @Override
    public String visitAssignExpression(Expression.Assign expression) {
        return String.format("(assign %s %s)", expression.name.lexeme, expression.expression.accept(this));
    }

    @Override
    public String visitLogicalExpression(Expression.Logical expression) {
        return parethesize(expression.operator.lexeme, expression.left, expression.right);
    }

    private String parethesize(String type, Expression... expressions) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(type);
        for (Expression expression : expressions) {
            sb.append(" ").append(expression.accept(this));
        }
        sb.append(")");

        return sb.toString();
    }
}
