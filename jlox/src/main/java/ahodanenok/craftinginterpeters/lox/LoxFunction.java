package ahodanenok.craftinginterpreters.lox;

import java.util.List;

class LoxFunction implements LoxCallable {

    private final Statement.Function declaration;
    private final Environment closure;

    LoxFunction(Statement.Function declaration, Environment environment) {
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
        return "<fn " + declaration.name.lexeme + ">";
    }
}
