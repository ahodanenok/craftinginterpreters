package ahodanenok.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {

    private final Statement.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Statement.Function declaration,
            Environment environment, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = environment;
        this.isInitializer = isInitializer;
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
            if (isInitializer) {
                return closure.getAt(0, "this");
            }

            return ret.value;
        }

        if (isInitializer) {
            return closure.getAt(0, "this");
        }

        return null;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
