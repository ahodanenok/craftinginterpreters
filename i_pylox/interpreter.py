from expr import ExprVisitor
from stmt import StmtVisitor
from token import TokenType
from env import Environment
from error import handleRuntimeError, RuntimeError
from callable import LoxCallable, LoxFunction, Return
from time import time


class ClockCallable(LoxCallable):

    def arity(self):
        return 0

    def call(self, interpreter, arguments):
        return time()

    def __str__(self):
        return '<native fn>'


class Interpreter(ExprVisitor, StmtVisitor):

    globals = Environment()
    environment = globals
    locals = {}

    def __init__(self):
        self.globals.define('clock', ClockCallable())

    def interpret(self, statements):
        try:
            for statement in statements:
                self.execute(statement)
        except RuntimeError as e:
            handleRuntimeError(e)

    def visitLiteralExpr(self, expr):
        return expr.value

    def visitLogicalExpr(self, expr):
        left = self.evaluate(expr.left)

        if expr.operator.type == TokenType.OR:
            if self.is_truthy(left): return left
        else:
            if not self.is_truthy(left): return left

        return self.evaluate(expr.right)

    def visitGroupingExpr(self, expr):
        return self.evaluate(expr.expression)

    def visitUnaryExpr(self, expr):
        right = self.evaluate(expr.right)

        match expr.operator.type:
            case TokenType.BANG: return not self.is_truthy(right)
            case TokenType.MINUS:
                self.check_number_operand(expr.operator, right)
                return -right
            case _: return None

    def visitVariableExpr(self, expr):
        return self.lookup_variable(expr.name, expr)

    def lookup_variable(self, name, expr):
        distance = self.locals.get(expr)
        if distance is not None:
            return self.environment.get_at(distance, name.lexeme)
        else:
            return self.globals.get(name)

    def check_number_operand(self, operator, operand):
        if isinstance(operand, float): return
        raise RuntimeError(operator, "Operand must be a number.")

    def check_number_operands(self, operator, left, right):
        if isinstance(left, float) and isinstance(right, float): return
        raise RuntimeError(operator, "Operands must be numbers.")

    def is_truthy(self, object):
        if object is None: return False
        if isinstance(object, bool): return object
        return True

    def is_equal(self, a, b):
        if a is None and b is None: return True
        if a is None: return False

        return a == b

    def stringify(self, object):
        if object is None: return 'nil'

        if isinstance(object, float):
            text = str(object)
            if text.endswith('.0'):
                text = text[0:-2]

            return text

        if isinstance(object, bool):
            return str(object).lower()

        return str(object)

    def evaluate(self, expr):
        return expr.accept(self)

    def execute(self, stmt):
        stmt.accept(self)

    def resolve(self, expr, depth):
        self.locals[expr] = depth

    def execute_block(self, statements, environment):
        previous = self.environment
        try:
            self.environment = environment

            for statement in statements:
                self.execute(statement)
        finally:
            self.environment = previous

    def visitBlockStmt(self, stmt):
        self.execute_block(stmt.statements, Environment(self.environment))

    def visitExpressionStmt(self, stmt):
        self.evaluate(stmt.expression)

    def visitFunctionStmt(self, stmt):
        function = LoxFunction(stmt, self.environment)
        self.environment.define(stmt.name.lexeme, function)
        return None

    def visitIfStmt(self, stmt):
        if self.is_truthy(self.evaluate(stmt.condition)):
            self.execute(stmt.then_branch)
        elif stmt.else_branch is not None:
            self.execute(stmt.else_branch)
        return None

    def visitPrintStmt(self, stmt):
        value = self.evaluate(stmt.expression)
        print(self.stringify(value))

    def visitReturnStmt(self, stmt):
        value = None
        if stmt.value is not None:
            value = self.evaluate(stmt.value)

        raise Return(value)

    def visitVarStmt(self, stmt):
        value = None
        if stmt.initializer is not None:
            value = self.evaluate(stmt.initializer)

        self.environment.define(stmt.name.lexeme, value)

    def visitWhileStmt(self, stmt):
        while self.is_truthy(self.evaluate(stmt.condition)):
            self.execute(stmt.body)

        return None

    def visitAssignExpr(self, expr):
        value = self.evaluate(expr.value)

        distance = self.locals.get(expr)
        if distance is not None:
            self.environment.assign_at(distance, expr.name, value)
        else:
            self.globals.assign(expr.name, value)

        return value

    def visitBinaryExpr(self, expr):
        left = self.evaluate(expr.left)
        right = self.evaluate(expr.right)

        match expr.operator.type:
            case TokenType.GREATER:
                self.check_number_operands(expr.operator, left, right)
                return left > right
            case TokenType.GREATER_EQUAL:
                self.check_number_operands(expr.operator, left, right)
                return left >= right
            case TokenType.LESS:
                self.check_number_operands(expr.operator, left, right)
                return left < right
            case TokenType.LESS_EQUAL:
                self.check_number_operands(expr.operator, left, right)
                return left <= right
            case TokenType.BANG_EQUAL: return not self.is_equal(left, right)
            case TokenType.EQUAL_EQUAL: return self.is_equal(left, right)
            case TokenType.MINUS:
                self.check_number_operands(expr.operator, left, right)
                return left - right
            case TokenType.PLUS:
                if isinstance(left, float) and isinstance(right, float):
                    return left + right

                if isinstance(left, str) and isinstance(right, str):
                    return left + right

                raise RuntimeError(expr.operator,
                    "Operands must be two numbers or two strings.")
            case TokenType.SLASH:
                self.check_number_operands(expr.operator, left, right)
                return left / right
            case TokenType.STAR:
                self.check_number_operands(expr.operator, left, right)
                return left * right

        return None

    def visitCallExpr(self, expr):
        callee = self.evaluate(expr.callee)

        arguments = []
        for argument in expr.arguments:
            arguments.append(self.evaluate(argument))

        if not isinstance(callee, LoxCallable):
            raise RuntimeError(expr.paren, "Can only call functions and classes.")

        if len(arguments) != callee.arity():
            raise RuntimeError(expr.paren,
                "Expected {} arguments but got {}.".format(callee.arity(), len(arguments)))

        return callee.call(self, arguments)
