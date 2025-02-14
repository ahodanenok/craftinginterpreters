package ahodanenok.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class Lox {

    private static final Interpreter interpreter = new Interpreter();

    private static boolean hadError;
    private static boolean hadRuntimeError;
    private static boolean suppressErrorMessages;

    public static void main(String... args) throws Exception {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String filePath) throws IOException {
        byte[] content = Files.readAllBytes(Paths.get(filePath));
        run(new String(content, "UTF-8"));

        if (hadError) {
            System.exit(65);
        } else if (hadRuntimeError) {
            System.exit(70);
        }
    }

    private static void runPrompt() throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(System.in));

        String line;
        while (true) {
            System.out.print("> ");
            line = reader.readLine();
            if (line == null) {
                break;
            }

            hadError = false;
            runPrompt(line);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();
        Parser parser = new Parser(tokens);
        List<Statement> program = parser.parse();
        if (hadError) {
            return;
        }

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(program);
        if (hadError) {
            return;
        }

        interpreter.interpret(program);
    }

    private static void runPrompt(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();
        List<Statement> program = null;
        Parser parser = new Parser(tokens);

        Expression expression;
        try {
            suppressErrorMessages = true;
            expression = parser.parseExpression();
        } finally {
            suppressErrorMessages = false;
        }

        if (!hadError) {
            program = List.of(new Statement.Print(expression));
        } else {
            hadError = false;
            program = parser.parse();
        }

        if (program.size() == 1 && program.get(0) instanceof Statement.Expr expr) {
            program = List.of(new Statement.Print(expr.expression));
        }

        if (hadError) {
            return;
        }

        interpreter.interpret(program);
    }

    private static void report(int line, String where, String msg) {
        hadError = true;
        if (!suppressErrorMessages) {
            System.err.println(String.format(
                "[line %d] Error%s: %s", line, where, msg));
        }
    }

    static void error(int line, String msg) {
        report(line, "", msg);
    }

    static void error(Token token, String msg) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", msg);
        } else {
            report(token.line, " at '" + token.lexeme + "'", msg);
        }
    }

    static void runtimeError(Token token, String msg) {
        if (!suppressErrorMessages) {
            System.err.println(msg + "\n[line " + token.line + "]");
        }
        hadRuntimeError = true;
    }
}
