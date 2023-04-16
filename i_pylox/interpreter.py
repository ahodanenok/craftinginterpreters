from expr import Visitor
from token import TokenType
import error

class RuntimeError(Exception):

    def __init__(self, token, message):
        self.token = token
        self.message = message


class Interpreter(Visitor):

    def interpret(self, expression):
        try:
            value = self.evaluate(expression)
            print(self.stringify(value))
        except RuntimeError as e:
            error.runtimeError(e)

    def visitLiteralExpr(self, expr):
        return expr.value

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

    def check_number_operand(self, operator, operand):
        if isinstance(operand, float): return
        raise RuntimeError(operator, "Operand must be a number.")

    def check_number_operands(self, operator, left, right):
        if isinstance(left, float) and isinstance(right, float): return
        raise RuntimeError(operator, "Operands must be numbers.")

    def is_truthy(self, object):
        if object is None: return False
        if isinstance(object, bool): return True
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
