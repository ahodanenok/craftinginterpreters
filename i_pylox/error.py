import sys
from token import TokenType

hadError = False

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
