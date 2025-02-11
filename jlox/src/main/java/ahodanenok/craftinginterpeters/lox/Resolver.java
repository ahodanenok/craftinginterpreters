package ahodanenok.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expression.Visitor<Void>, Statement.Visitor<Void> {

    private enum FunctionType {

        NONE,
        FUNCTION,
        LAMBDA;
    }

    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes;
    private FunctionType currentFunction;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
        this.scopes = new Stack<>();
        this.currentFunction = FunctionType.NONE;
    }

    void resolve(List<Statement> statements) {
        statements.forEach(s -> s.accept(this));
    }

    @Override
    public Void visitBlockStatement(Statement.Block block) {
        beginScope();
        resolve(block.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var variable) {
        declare(variable.name);
        if (variable.initializer != null) {
            resolve(variable.initializer);
        }
        define(variable.name);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function function) {
        declare(function.name);
        define(function.name);
        resolveFunction(function, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        resolve(statement.condition);
        resolve(statement.thenBranch);
        if (statement.elseBranch != null) {
            resolve(statement.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(statement.keyword, "Can't return from top-level code.");
        }

        if (statement.expression != null) {
            resolve(statement.expression);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        resolve(statement.condition);
        resolve(statement.body);
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break statement) {
        return null;
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement) {
        resolve(statement.expression);
        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.Variable variable) {
        if (!scopes.isEmpty()
                && scopes.peek().get(variable.name.lexeme) == Boolean.FALSE) {
            Lox.error(variable.name, "Can't read local variable in its own initializer.");
        }
        resolveLocal(variable, variable.name);
        return null;
    }

    @Override
    public Void visitAssignExpression(Expression.Assign assignment) {
        resolve(assignment.expression);
        resolveLocal(assignment, assignment.name);
        return null;
    }

    @Override
    public Void visitLambdaExpression(Expression.Lambda lambda) {
        FunctionType prevFunction = currentFunction;
        currentFunction = FunctionType.LAMBDA;
        beginScope();
        for (Token param : lambda.params) {
            declare(param);
            define(param);
        }
        resolve(lambda.body);
        endScope();
        currentFunction = prevFunction;
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expression) {
        resolve(expression.callee);
        for (Expression argument : expression.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.expression);
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.left);
        resolve(expression.right);
        return null;
    }

    @Override
    public Void visitTernaryExpression(Expression.Ternary expression) {
        resolve(expression.condition);
        resolve(expression.left);
        resolve(expression.right);
        return null;

    }

    private void declare(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) {
            return;
        }

        scopes.peek().put(name.lexeme, true);
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void resolveLocal(Expression expression, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expression, scopes.size() - i - 1);
                return;
            }
        }
    }

    private void resolveFunction(Statement.Function function, FunctionType type) {
        FunctionType prevFunction = currentFunction;
        currentFunction = type;
        beginScope();
        for (Token param : function.params) {
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = prevFunction;
    }
}