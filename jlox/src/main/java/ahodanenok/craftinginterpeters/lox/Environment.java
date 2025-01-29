package ahodanenok.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

class Environment {

    private final Map<String, Object> values = new HashMap<>();
    private final Environment parent;

    Environment() {
        parent = null;
    }

    Environment(Environment parent) {
        this.parent = parent;
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

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (parent != null) {
            return parent.get(name);
        }

        throw new RuntimeError(
            name, String.format("Undefined variable '%s'.", name.lexeme));
    }
}
