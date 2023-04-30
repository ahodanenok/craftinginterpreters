import sys
from token import TokenType

hadError = False
hadRuntimeError = False

class RuntimeError(Exception):

    def __init__(self, token, message):
        self.token = token
        self.message = message

def error(line, message):
    report(line, "", message)

def report(line, where, message):
    print("[line {}] Error{}: {}".format(line, where, message), file=sys.stderr)
    global hadError
    hadError = True

def errorToken(token, message):
    if token.type == TokenType.EOF:
        report(token.line, ' at end', message)
    else:
        report(token.line, " at '" + token.lexeme + "'", message)

def handleRuntimeError(error):
    print('{}\n[line {}]'.format(error.message, error.token.line))
    global hadRuntimeError
    hadRuntimeError = True
