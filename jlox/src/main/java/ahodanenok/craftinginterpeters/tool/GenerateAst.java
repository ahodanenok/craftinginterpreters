package ahodanenok.craftinginterpreters.tool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;

public class GenerateAst {

    public static void main(String... args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output_directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expression", Arrays.asList(
            "Literal : Object value",
            "Unary : Token operator, Expression expression" ,
            "Binary : Token operator, Expression left, Expression right",
            "Ternary : Expression condition, Expression left, Expression right",
            "Grouping : Expression expression",
            "Variable : Token name",
            "Assign : Token name, Expression expression"
        ));
        defineAst(outputDir, "Statement", Arrays.asList(
            "Expr : Expression expression",
            "Print : Expression expression",
            "Var : Token name, Expression initializer",
            "Block : java.util.List<Statement> statements"
        ));
    }

    private static void defineAst(
            String outputDir, String baseClassName, List<String> types) throws IOException {
        File file = new File(outputDir, baseClassName + ".java");
        file.createNewFile();
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            writer.println("package ahodanenok.craftinginterpreters.lox;");
            writer.println();
            writer.println("abstract class " + baseClassName + " {");
            writer.println();
            writer.println("    abstract <R> R accept(Visitor<R> visitor);");

            writer.println();
            writer.println("    interface Visitor<R> {");
            for (String type : types) {
                String typeClassName = type.split(":")[0].trim();

                writer.println();
                writer.println("        R visit" + typeClassName + baseClassName
                    + "(" + typeClassName + " " + baseClassName.toLowerCase() + ");");
            }
            writer.println("    }");

            for (String type : types) {
                String[] parts = type.split(":");
                String typeClassName = parts[0].trim();
                String[] fields = parts[1].split(",");

                writer.println();
                writer.println("    final static class " + typeClassName + " extends " + baseClassName + " {");

                writer.println();
                for (String field : fields) {
                    writer.println("        final " + field.trim() + ";");
                }

                writer.println();
                writer.print("        " + typeClassName + "(");
                writer.print(Arrays.stream(fields).map(f -> f.trim()).collect(Collectors.joining(", ")));
                writer.println(") {");
                for (String field : fields) {
                    String fieldName = field.trim().split("\\s+")[1].trim();
                    writer.println("            this." + fieldName + " = " + fieldName + ";");
                }
                writer.println("        }");

                writer.println();
                writer.println("        @Override");
                writer.println("        <R> R accept(Visitor<R> visitor) {");
                writer.println("            return visitor.visit" + typeClassName + baseClassName + "(this);");
                writer.println("        }");

                writer.println("    }");
            }

            writer.println("}");
        }
    }
}
