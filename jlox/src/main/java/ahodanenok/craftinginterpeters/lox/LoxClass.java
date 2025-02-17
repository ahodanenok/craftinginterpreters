package ahodanenok.craftinginterpreters.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {

    final String name;
    final LoxClass parent;
    final Map<String, LoxFunction> methods;

    LoxClass(String name, LoxClass parent, Map<String, LoxFunction> methods) {
        this.name = name;
        this.parent = parent;
        this.methods = methods;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        return initializer != null ? initializer.arity() : 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        if (parent != null) {
            return parent.findMethod(name);
        }

        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}