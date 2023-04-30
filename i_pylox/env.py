from error import RuntimeError

class Environment:

    def __init__(self, enclosing = None):
        self.values = {}
        self.enclosing = enclosing

    def define(self, name, value):
        self.values[name] = value

    def get(self, name):
        if name.lexeme in self.values:
            return self.values[name.lexeme]

        if self.enclosing is not None:
            return self.enclosing.get(name)

        raise RuntimeError(name, "Undefine variable '{}'.".format(name.lexeme))

    def assign(self, name, value):
        if name.lexeme in values:
            self.values[name.lexeme] = value
            return

        if self.enclosing is not None:
            self.enclosing.assign(name, value)
            return

        raise RuntimeError(name, "Undefine variable '{}'.".format(name.lexeme))