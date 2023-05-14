from env import Environment

class LoxCallable:

    def arity(self):
        pass

    def call(self, interpreter, arguments):
        pass


class Return(Exception):

    def __init__(self, value):
        self.value = value


class LoxFunction(LoxCallable):

    def __init__(self, declaration, closure):
        self.declaration = declaration
        self.closure = closure

    def arity(self):
        return len(self.declaration.params)

    def call(self, interpreter, arguments):
        environment = Environment(self.closure)
        for i in range(len(self.declaration.params)):
            environment.define(self.declaration.params[i].lexeme, arguments[i])

        try:
            interpreter.execute_block(self.declaration.body, environment)
        except Return as r:
            return r.value

        return None

    def __str__(self):
        return '<fn {}>'.format(self.declaration.name.lexeme)
