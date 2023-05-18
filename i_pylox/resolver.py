from expr import ExprVisitor
from stmt import StmtVisitor
from error import error
from enum import Enum, auto

class FunctionType(Enum):
    NONE = auto()
    FUNCTION = auto()

class Resolver(ExprVisitor, StmtVisitor):

    def __init__(self, interpreter):
        self.interpreter = interpreter
        self.scopes = []
        self.current_function = FunctionType.NONE

    def resolve(self, statements):
        for statement in statements:
            self.resolve_single(statement)

    def resolve_function(self, function, type):
        enclosing_function = self.current_function
        self.current_function = type

        self.begin_scope()
        for param in function.params:
            self.declare(param)
            self.define(param)
        self.resolve(function.body)
        self.end_scope()
        self.current_function = enclosing_function

    def resolve_single(self, node):
        node.accept(self)

    def begin_scope(self):
        self.scopes.append({})

    def end_scope(self):
        self.scopes.pop()

    def declare(self, name):
        if len(self.scopes) == 0: return

        scope = self.scopes[-1]
        if name.lexeme in scope:
            error(name, 'Already a variable with this name in this scope.')

        scope[name.lexeme] = False

    def define(self, name):
        if len(self.scopes) == 0: return
        self.scopes[-1][name.lexeme] = True

    def resolve_local(self, expr, name):
        for i in range(len(self.scopes) - 1, -1, -1):
            if name.lexeme in self.scopes[i]:
                self.interpreter.resolve(expr, len(self.scopes) - 1 - i)
                return

    def visitBlockStmt(self, stmt):
        self.begin_scope()
        self.resolve(stmt.statements)
        self.end_scope()

    def visitExpressionStmt(self, stmt):
        self.resolve_single(stmt.expression)

    def visitFunctionStmt(self, stmt):
        self.declare(stmt.name)
        self.define(stmt.name)
        self.resolve_function(stmt, FunctionType.FUNCTION)

    def visitIfStmt(self, stmt):
        self.resolve_single(stmt.condition)
        self.resolve_single(stmt.then_branch)
        if stmt.else_branch is not None: self.resolve(stmt.else_branch)

    def visitPrintStmt(self, stmt):
        self.resolve_single(stmt.expression)

    def visitReturnStmt(self, stmt):
        if self.current_function == FunctionType.NONE:
            error(stmt.keyword, "Can't return from top-level code.")

        if stmt.value is not None:
            self.resolve_single(stmt.value)

    def visitVarStmt(self, stmt):
        self.declare(stmt.name)
        if stmt.initializer is not None:
            self.resolve_single(stmt.initializer)
        self.define(stmt.name)

    def visitWhileStmt(self, stmt):
        self.resolve_single(stmt.condition)
        self.resolve_single(stmt.body)

    def visitAssignExpr(self, expr):
        self.resolve_single(expr.value)
        self.resolve_local(expr, expr.name)

    def visitBinaryExpr(self, expr):
        self.resolve_single(expr.left)
        self.resolve_single(expr.right)

    def visitCallExpr(self, expr):
        self.resolve_single(expr.callee)
        for argument in expr.arguments:
            self.resolve_single(argument)

    def visitGroupingExpr(self, expr):
        self.resolve_single(expr.expression)

    def visitLiteralExpr(self, expr):
        pass

    def visitLogicalExpr(self, expr):
        self.resolve_single(expr.left)
        self.resolve_single(expr.right)

    def visitUnaryExpr(self, expr):
        self.resolve_single(expr.right)

    def visitVariableExpr(self, expr):
        if len(self.scopes) > 0 and self.scopes[-1].get(expr.name.lexeme) is False:
            error(expr.name, "Can't read local variable in its own initializer.")

        self.resolve_local(expr, expr.name)