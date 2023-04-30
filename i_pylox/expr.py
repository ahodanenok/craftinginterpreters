class ExprVisitor:

    def visitAssignExpr(self, expr):
        pass

    def visitBinaryExpr(self, expr):
        pass

    def visitGroupingExpr(self, expr):
        pass

    def visitLiteralExpr(self, expr):
        pass

    def visitUnaryExpr(self, expr):
        pass

    def visitVariableExpr(self, expr):
        pass

class Expr:
    pass

class Assign(Expr):

    def __init__(self, name, value):
        self.name = name
        self.value = value

    def accept(self, visitor):
        return visitor.visitAssignExpr(self)

class Binary(Expr):

    def __init__(self, left, operator, right):
        self.left = left
        self.operator = operator
        self.right = right

    def accept(self, visitor):
        return visitor.visitBinaryExpr(self)

class Grouping(Expr):

    def __init__(self, expression):
        self.expression = expression

    def accept(self, visitor):
        return visitor.visitGroupingExpr(self)

class Literal(Expr):

    def __init__(self, value):
        self.value = value

    def accept(self, visitor):
        return visitor.visitLiteralExpr(self)

class Unary(Expr):

    def __init__(self, operator, right):
        self.operator = operator
        self.right = right

    def accept(self, visitor):
        return visitor.visitUnaryExpr(self)

class Variable(Expr):

    def __init__(self, name):
        self.name = name

    def accept(self, visitor):
        return visitor.visitVariableExpr(self)

