package ahodanenok.craftinginterpreters.lox;

abstract class Statement {

    abstract <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {

        R visitExprStatement(Expr statement);

        R visitPrintStatement(Print statement);
    }

    final static class Expr extends Statement {

        final Expression expression;

        Expr(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExprStatement(this);
        }
    }

    final static class Print extends Statement {

        final Expression expression;

        Print(Expression expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }
}
