package ahodanenok.craftinginterpreters.lox;

import java.util.Objects;

class Interpreter implements Expression.Visitor<Object> {

    void interpret(Expression expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError e) {
            Lox.runtimeError(e.token, e.getMessage());
        }
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
                } else if (left instanceof String || right instanceof String) {
                    yield stringify(left) + stringify(right);
                }

                throw new RuntimeError(
                    expression.operator,
                    "Operands must be two numbers or two strings");
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

    private Object evaluate(Expression expression) {
        return expression.accept(this);
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
            throw new RuntimeError(operator, "Operand must be a number");
        }
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (!(left instanceof Double) || !(right instanceof Double)) {
            throw new RuntimeError(operator, "Operands must be numbers");
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
