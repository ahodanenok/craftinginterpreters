package ahodanenok.craftinginterpreters.lox;

abstract class Statement {

    abstract <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {

        R visitExprStatement(Expr statement);

        R visitPrintStatement(Print statement);

        R visitVarStatement(Var statement);

        R visitBlockStatement(Block statement);

        R visitIfStatement(If statement);

        R visitWhileStatement(While statement);
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

    final static class Var extends Statement {

        final Token name;
        final Expression initializer;

        Var(Token name, Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStatement(this);
        }
    }

    final static class Block extends Statement {

        final java.util.List<Statement> statements;

        Block(java.util.List<Statement> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    final static class If extends Statement {

        final Expression condition;
        final Statement thenBranch;
        final Statement elseBranch;

        If(Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }
    }

    final static class While extends Statement {

        final Expression condition;
        final Statement body;

        While(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }
}
