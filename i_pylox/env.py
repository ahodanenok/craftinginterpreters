from error import RuntimeError

class Environment:

    def __init__(self, enclosing = None):
        self.values = {}
        self.enclosing = enclosing

    def define(self, name, value):
        self.values[name] = value

    def ancestor(self, distance):
        environment = self
        for i in range(distance):
            environment = environment.enclosing

        return environment

    def get_at(self, distance, name):
        return self.ancestor(distance).values.get(name)

    def assign_at(self, distance, name, value):
        self.ancestor(distance).values[name.lexeme] = value

    def get(self, name):
        if name.lexeme in self.values:
            return self.values[name.lexeme]

        if self.enclosing is not None:
            return self.enclosing.get(name)

        raise RuntimeError(name, "Undefine variable '{}'.".format(name.lexeme))

    def assign(self, name, value):
        if name.lexeme in self.values:
            self.values[name.lexeme] = value
            return

        if self.enclosing is not None:
            self.enclosing.assign(name, value)
            return

        raise RuntimeError(name, "Undefine variable '{}'.".format(name.lexeme))
