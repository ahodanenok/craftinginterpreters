class StmtVisitor:

    def visitBlockStmt(self, stmt):
        pass

    def visitExpressionStmt(self, stmt):
        pass

    def visitPrintStmt(self, stmt):
        pass

    def visitVarStmt(self, stmt):
        pass

class Stmt:
    pass

class Block(Stmt):

    def __init__(self, statements):
        self.statements = statements

    def accept(self, visitor):
        return visitor.visitBlockStmt(self)

class Expression(Stmt):

    def __init__(self, expression):
        self.expression = expression

    def accept(self, visitor):
        return visitor.visitExpressionStmt(self)

class Print(Stmt):

    def __init__(self, expression):
        self.expression = expression

    def accept(self, visitor):
        return visitor.visitPrintStmt(self)

class Var(Stmt):

    def __init__(self, name, initializer):
        self.name = name
        self.initializer = initializer

    def accept(self, visitor):
        return visitor.visitVarStmt(self)

