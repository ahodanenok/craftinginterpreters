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

        R visitBreakStatement(Break statement);

        R visitFunctionStatement(Function statement);

        R visitReturnStatement(Return statement);
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

    final static class Break extends Statement {

        final Token keyword;

        Break(Token keyword) {
            this.keyword = keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBreakStatement(this);
        }
    }

    final static class Function extends Statement {

        final Token name;
        final java.util.List<Token> params;
        final java.util.List<Statement> body;

        Function(Token name, java.util.List<Token> params, java.util.List<Statement> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStatement(this);
        }
    }

    final static class Return extends Statement {

        final Token keyword;
        final Expression expression;

        Return(Token keyword, Expression expression) {
            this.keyword = keyword;
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStatement(this);
        }
    }
}
