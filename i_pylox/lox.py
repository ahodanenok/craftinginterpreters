import sys
import error
from scanner import Scanner
from parser import Parser
from ast_printer import AstPrinter

def runFile(fileName):
    with open(fileName, mode = 'rb') as f:
        program = f.read().decode()

    run(program)
    if error.hadError:
        sys.exit(65)

def runPrompt():
    try:
        while True:
            line = input("> ")
            run(line)
            error.hadError = False
    except EOFError:
        pass

def run(program):
    scanner = Scanner(program)
    tokens = scanner.scan_tokens()
    parser = Parser(tokens)
    expression = parser.parse()

    if error.hadError: return

    print(AstPrinter().print(expression))

if len(sys.argv) > 2:
    print("Usage: lox [script]")
    sys.exit(64)
elif len(sys.argv) == 2:
    runFile(sys.argv[1])
else:
    runPrompt()
