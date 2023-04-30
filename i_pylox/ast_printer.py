import expr
from token import Token, TokenType

class AstPrinter(expr.ExprVisitor):

    def print(self, expr):
        return expr.accept(self)

    def visitBinaryExpr(self, expr):
        return self.parenthesize(expr.operator.lexeme, expr.left, expr.right)

    def visitGroupingExpr(self, expr):
        return self.parenthesize("group", expr.expression)

    def visitLiteralExpr(self, expr):
        if expr.value is None: return "nil"
        return str(expr.value)

    def visitUnaryExpr(self, expr):
        return self.parenthesize(expr.operator.lexeme, expr.right)

    def parenthesize(self, name, *exprs):
        s = '(' + name
        for expr in exprs:
            s += ' '
            s += expr.accept(self)
        s += ')'

        return s

if __name__ == '__main__':
    e = expr.Binary(
        expr.Unary(
            Token(TokenType.MINUS, '-', None, 1),
            expr.Literal(123)),
        Token(TokenType.STAR, '*', None, 1),
        expr.Grouping(expr.Literal(45.67)))

    print(AstPrinter().print(e))
