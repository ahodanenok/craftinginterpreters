package ahodanenok.craftinginterpreters.lox;

abstract class Expression {

    abstract <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {

        R visitLiteralExpression(Literal expression);

        R visitUnaryExpression(Unary expression);

        R visitBinaryExpression(Binary expression);

        R visitGroupingExpression(Grouping expression);
    }

    final static class Literal extends Expression {

        final Object value;

        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    final static class Unary extends Expression {

        final Token operator;
        final Expression expression;

        Unary(Token operator, Expression expression) {
            this.operator = operator;
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpression(this);
        }
    }

    final static class Binary extends Expression {

        final Token operator;
        final Expression left;
        final Expression right;

        Binary(Token operator, Expression left, Expression right) {
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpression(this);
        }
    }

    final static class Grouping extends Expression {

        final Expression expression;

        Grouping(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpression(this);
        }
    }
}
