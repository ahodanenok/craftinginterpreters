package ahodanenok.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Environment {

    private final Map<String, Object> values = new HashMap<>();
    private final Set<String> initialized = new HashSet<>();
    private final Environment parent;

    Environment() {
        parent = null;
    }

    Environment(Environment parent) {
        this.parent = parent;
    }

    void markInitialized(String name) {
        initialized.add(name);
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (parent != null) {
            parent.assign(name, value);
            return;
        }

        throw new RuntimeError(
            name, String.format("Undefined variable '%s'.", name.lexeme));
    }

    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            if (!initialized.contains(name.lexeme)) {
                // makes variable initialization required
                // conflicts with the test suite
                // throw new RuntimeError(
                //     name,
                //     String.format("Unitialized variable '%s'.", name.lexeme));
            }

            return values.get(name.lexeme);
        }

        if (parent != null) {
            return parent.get(name);
        }

        throw new RuntimeError(
            name, String.format("Undefined variable '%s'.", name.lexeme));
    }

    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.parent;
        }

        return environment;
    }
}
