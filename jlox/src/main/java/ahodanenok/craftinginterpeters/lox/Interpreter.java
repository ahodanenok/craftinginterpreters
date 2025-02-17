package ahodanenok.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    Environment globals = new Environment();
    private Environment environment = globals;
    private Map<Expression, Integer> locals = new HashMap<>();
    private LinkedList<Boolean> loopBroken = new LinkedList<>();

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void interpret(List<Statement> program) {
        try {
            for (Statement statement : program) {
                execute(statement);
            }
        } catch (RuntimeError e) {
            Lox.runtimeError(e.token, e.getMessage());
        }
    }

    void resolve(Expression expression, int distance) {
        locals.put(expression, distance);
    }

    @Override
    public Void visitExprStatement(Statement.Expr statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        Object value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var statement) {
        Object value = null;
        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }
        environment.define(statement.name.lexeme, value);
        if (statement.initializer != null) {
            environment.markInitialized(statement.name.lexeme);
        }

        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Statement> statements, Environment currentEnvironment) {
        Environment previousEnvironment = this.environment;
        try {
            this.environment = currentEnvironment;
            for (Statement statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previousEnvironment;
        }
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.thenBranch);
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        loopBroken.push(false);
        try {
            while (isTruthy(evaluate(statement.condition)) && !loopBroken.peek()) {
                execute(statement.body);
            }
        } finally {
            loopBroken.pop();
        }
        return null;
    }

    @Override
    public Void visitBreakStatement(Statement.Break statement) {
        if (loopBroken.size() == 0) {
            throw new RuntimeError(statement.keyword, "No enclosing loop.");
        }
        loopBroken.set(0, true);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        LoxFunction function = new LoxFunction(statement, environment, false);
        environment.define(statement.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        Object value = null;
        if (statement.expression != null) {
            value = evaluate(statement.expression);
        }

        throw new Return(value);
    }

    @Override
    public Void visitClassStatement(Statement.Class statement) {
        Object parent = null;
        if (statement.parent != null) {
            parent = evaluate(statement.parent);
            if (!(parent instanceof LoxClass)) {
                throw new RuntimeError(statement.parent.name,
                    "Superclass must be a class.");
            }
        }

        environment.define(statement.name.lexeme, null);
        if (statement.parent != null) {
            environment = new Environment(environment);
            environment.define("super", parent);
        }

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Statement.Function method : statement.methods) {
            methods.put(method.name.lexeme,
                new LoxFunction(method, environment, method.name.lexeme.equals("init")));
        }

        if (statement.parent != null) {
            environment = environment.parent;
        }

        LoxClass klass = new LoxClass(statement.name.lexeme, (LoxClass) parent, methods);
        environment.assign(statement.name, klass);
        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object value = evaluate(expression.expression);
        return switch (expression.operator.type) {
            case MINUS -> {
                checkNumberOperand(expression.operator, value);
                yield -(double) value;
            }
            case BANG -> !isTruthy(value);
            default -> null;
        };
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);
        return switch (expression.operator.type) {
            case GREATER_EQUAL -> {
                checkNumberOperands(expression.operator, left, right);
                yield (double) left >= (double) right;
            }
            case GREATER -> {
                checkNumberOperands(expression.operator, left, right);
                yield (double) left > (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expression.operator, left, right);
                yield (double) left <= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expression.operator, left, right);
                yield (double) left < (double) right;
            }
            case BANG_EQUAL -> !isEqual(left, right);
            case EQUAL_EQUAL -> isEqual(left, right);
            case MINUS -> {
                checkNumberOperands(expression.operator, left, right);
                yield (double) left - (double) right;
            }
            case PLUS -> {
                if (left instanceof Double a && right instanceof Double b) {
                    yield a + b;
                }

                if (left instanceof String a && right instanceof String b) {
                    yield a + b;
                }
                // conflicts with the test suite
                // else if (left instanceof String || right instanceof String) {
                //     yield stringify(left) + stringify(right);
                // }

                throw new RuntimeError(
                    expression.operator,
                    "Operands must be two numbers or two strings.");
            }
            case STAR -> {
                checkNumberOperands(expression.operator, left, right);
                yield (double) left * (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expression.operator, left, right);
                yield (double) left / (double) right;
            }
            default -> null;
        };
    }

    @Override
    public Object visitTernaryExpression(Expression.Ternary expression) {
        Object value = evaluate(expression.condition);
        if (isTruthy(value)) {
            return evaluate(expression.left);
        } else {
            return evaluate(expression.right);
        }
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return lookupVariable(expression.name, expression);
    }

    private Object lookupVariable(Token name, Expression expression) {
        Integer distance = locals.get(expression);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expression) {
        Object value = evaluate(expression.expression);
        Integer distance = locals.get(expression);
        if (distance != null) {
            environment.assignAt(distance, expression.name, value);
        } else {
            globals.assign(expression.name, value);
        }
        environment.markInitialized(expression.name.lexeme);
        return value;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        Object value = evaluate(expression.left);
        if (expression.operator.type == TokenType.OR) {
            if (isTruthy(value)) {
                return value;
            }
        } else if (expression.operator.type == TokenType.AND) {
            if (!isTruthy(value)) {
                return value;
            }
        } else {
            throw new RuntimeError(expression.operator, "Expect logical operator.");
        }

        return evaluate(expression.right);
    }

    @Override
    public Object visitCallExpression(Expression.Call expression) {
        Object callee = evaluate(expression.callee);
        List<Object> arguments = new ArrayList<>();
        for (Expression argument : expression.arguments) {
            arguments.add(evaluate(argument));
        }

        if (callee instanceof LoxCallable callable) {
            if (callable.arity() != arguments.size()) {
                throw new RuntimeError(expression.paren,
                    String.format(
                        "Expected %d arguments but got %d.",
                        callable.arity(),
                        arguments.size()));
            }

            return callable.call(this, arguments);
        }

        throw new RuntimeError(expression.paren,
            "Can only call functions and classes.");
    }

    @Override
    public Object visitLambdaExpression(Expression.Lambda expression) {
        return new LoxLambda(expression, environment);
    }

    @Override
    public Object visitGetExpression(Expression.Get expression) {
        Object object = evaluate(expression.object);
        if (object instanceof LoxInstance instance) {
            return instance.get(expression.name);
        }

        throw new RuntimeError(expression.name,
            "Only instances have properties.");
    }

    @Override
    public Object visitSetExpression(Expression.Set expression) {
        Object object = evaluate(expression.object);
        if (object instanceof LoxInstance instance) {
            Object value = evaluate(expression.value);
            instance.set(expression.name, value);
            return value;
        }

        throw new RuntimeError(expression.name, "Only instances have fields.");
    }

    @Override
    public Object visitThisExpression(Expression.This expression) {
        return lookupVariable(expression.keyword, expression);
    }

    @Override
    public Object visitSuperExpression(Expression.Super expression) {
        int distance = locals.get(expression);
        LoxClass parent = (LoxClass) environment.getAt(distance, "super");
        LoxInstance object =
            (LoxInstance) environment.getAt(distance - 1, "this");
        LoxFunction method = parent.findMethod(expression.method.lexeme);
        if (method == null) {
            throw new RuntimeError(expression.method,
                "Undefined property '" + expression.method.lexeme + "'.");
        }

        return method.bind(object);
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private void execute(Statement statement) {
        if (loopBroken.size() > 0 && loopBroken.peek()) {
            return;
        }

        statement.accept(this);
    }

    private boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof Boolean) {
            return (boolean) value;
        } else {
            return true;
        }
    }

    private boolean isEqual(Object a, Object b) {
        return Objects.equals(a, b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (!(operand instanceof Double)) {
            throw new RuntimeError(operator, "Operand must be a number.");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Double) || !(right instanceof Double)) {
            throw new RuntimeError(operator, "Operands must be numbers.");
        }
    }

    private String stringify(Object value) {
        if (value == null) {
            return "nil";
        }

        if (value instanceof Double n) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                return text.substring(0, text.length() - 2);
            } else {
                return text;
            }
        }

        return value.toString();
    }
}
