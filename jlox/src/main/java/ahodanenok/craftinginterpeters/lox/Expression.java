package ahodanenok.craftinginterpreters.lox;

abstract class Expression {

    final static class Literal extends Expression {

        final Object value;

        Literal(Object value) {
            this.value = value;
        }
    }

    final static class Unary extends Expression {

        final Token operator;
        final Expression expression;

        Unary(Token operator, Expression expression) {
            this.operator = operator;
            this.expression = expression;
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
    }

    final static class Grouping extends Expression {

        final Expression expression;

        Grouping(Expression expression) {
            this.expression = expression;
        }
    }
}
