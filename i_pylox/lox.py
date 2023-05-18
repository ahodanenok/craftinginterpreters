import sys
import error
from scanner import Scanner
from parser import Parser
from resolver import Resolver
from interpreter import Interpreter
from ast_printer import AstPrinter

interpreter = Interpreter()

def runFile(fileName):
    with open(fileName, mode = 'rb') as f:
        program = f.read().decode()

    run(program)
    if error.hadError:
        sys.exit(65)
    if error.hadRuntimeError:
        sys.exit(70)

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
    statements = parser.parse()

    if error.hadError: return

    resolver = Resolver(interpreter)
    resolver.resolve(statements)

    if error.hadError: return

    interpreter.interpret(statements)

if len(sys.argv) > 2:
    print("Usage: lox [script]")
    sys.exit(64)
elif len(sys.argv) == 2:
    runFile(sys.argv[1])
else:
    runPrompt()
