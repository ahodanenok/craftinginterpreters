import sys

hadError = False

def error(line, message):
    report(line, "", message)

def report(line, where, message):
    print("[line {}] Error{}: {}".format(line, where, message), file=sys.stderr)
    global hadError
    hadError = True
