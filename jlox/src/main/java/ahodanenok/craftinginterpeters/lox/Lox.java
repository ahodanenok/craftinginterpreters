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
        run(new String(content, Charset.defaultCharset()));

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
            run(line);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();
        if (hadError) {
            return;
        }

        Parser parser = new Parser(tokens);
        List<Statement> program = parser.parse();
        if (hadError) {
            return;
        }

        interpreter.interpret(program);
    }

    private static void report(int line, String where, String msg) {
        hadError = true;
        System.err.println(String.format(
            "line %d | error %s: %s", line, where, msg));
    }

    static void error(int line, String msg) {
        report(line, "", msg);
    }

    static void error(Token token, String msg) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at the end", msg);
        } else {
            report(token.line, " at '" + token.lexeme + "'", msg);
        }
    }

    static void runtimeError(Token token, String msg) {
        System.err.println(String.format(
            "line %d | %s: %s", token.line, token.lexeme, msg));
        hadRuntimeError = true;
    }
}
