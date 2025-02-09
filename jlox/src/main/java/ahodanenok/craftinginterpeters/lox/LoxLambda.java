package ahodanenok.craftinginterpreters.lox;

import java.util.List;

class LoxLambda implements LoxCallable {

    private final Expression.Lambda declaration;
    private final Environment closure;

    LoxLambda(Expression.Lambda declaration, Environment environment) {
        this.declaration = declaration;
        this.closure = environment;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return ret) {
            return ret.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<lambda>";
    }
}
