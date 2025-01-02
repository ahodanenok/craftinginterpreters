package ahodanenok.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class Lox {

    private static boolean hadError;

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
    }

    private static void runPrompt() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        String line;
        while (true) {
            System.out.print("> ");
            line = reader.readLine();
            if (line == null) {
                break;
            }

            run(line);
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static void error(int line, String msg) {
        hadError = true;
        System.out.println(String.format("line %d: %s", line, msg));
    }
}
