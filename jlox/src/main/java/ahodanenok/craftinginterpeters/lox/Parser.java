package ahodanenok.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

final class Parser {

    private final List<Token> tokens;
    private int current;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statement> parse() {
        List<Statement> program = new ArrayList<>();
        while (hasMoreTokens()) {
            program.add(declaration());
        }

        return program;
    }

    Expression parseExpression() {
        try {
            return expression();
        } catch (ParseException e) {
            return null;
        }
    }

    private Statement declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            } else if (match(TokenType.FUN)) {
                return function("function");
            } else {
                return statement();
            }
        } catch (ParseException e) {
            synchronize();
            return null;
        }
    }

    private Statement varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");
        Expression initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");

        return new Statement.Var(name, initializer);
    }

    private Statement function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");

        consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name.");
        List<Token> params = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (params.size() > 254) {
                    error(peek(), "Can't have more than 255 parameters.");
                }

                params.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Statement> body = block();

        return new Statement.Function(name, params, body);
    }

    private Statement statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
        } else if (match(TokenType.LEFT_BRACE)) {
            return new Statement.Block(block());
        } else if (match(TokenType.IF)) {
            return ifStatement();
        } else if (match(TokenType.WHILE)) {
            return whileStatement();
        } else if (match(TokenType.FOR)) {
            return forStatement();
        } else if (match(TokenType.BREAK)) {
            return breakStatement();
        } else if (match(TokenType.RETURN)) {
            return returnStatement();
        } else {
            return expressionStatement();
        }
    }

    private Statement printStatement() {
        Expression expression = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Statement.Print(expression);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && hasMoreTokens()) {
            statements.add(declaration());
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");

        return statements;
    }

    private Statement ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
        Statement body = statement();
        return new Statement.While(condition, body);
    }

    private Statement forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");
        Statement initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expression condition;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        } else {
            condition = new Expression.Literal(true);
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        Expression increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

        Statement body = statement();
        if (increment != null) {
            body = new Statement.Block(List.of(
                body, new Statement.Expr(increment)));
        }
        body = new Statement.While(condition, body);
        if (initializer != null) {
            body = new Statement.Block(List.of(initializer, body));
        }

        return body;
    }

    private Statement breakStatement() {
        Statement statement = new Statement.Break(previous());
        consume(TokenType.SEMICOLON, "Expect ';' after 'break'.");
        return statement;
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expression expression = null;
        if (!check(TokenType.SEMICOLON)) {
            expression = comma();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after return value.");

        return new Statement.Return(keyword, expression);
    }

    private Statement expressionStatement() {
        Expression expression = comma();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expr(expression);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression left = ternary();
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression right = assignment();

            if (left instanceof Expression.Variable v) {
                return new Expression.Assign(v.name, right);
            }

            error(equals, "Invalid assignment target.");
        }

        return left;
    }

    private Expression ternary() {
        return ternary(or());
    }

    private Expression ternary(Expression condition) {
        Expression expression = condition;
        if (match(TokenType.QUESTION)) {
            Expression left = ternary();
            consume(TokenType.COLON, "Expect ':' after expression.");
            Expression right = ternary();
            expression = new Expression.Ternary(condition, left, ternary(right));
        }

        return expression;
    }

    private Expression comma() {
        Expression left = commaMissingLeft();
        while (match(TokenType.COMMA)) {
            Token operator = previous();
            Expression right = commaMissingLeft();
            left = new Expression.Binary(operator, left, right);
        }

        return left;
    }

    private Expression commaMissingLeft() {
        if (match(TokenType.COMMA)) {
            Token operator = previous();
            expression();
            throw error(operator, "Expect left-hand operand.");
        }

        return expression();
    }

    private Expression or() {
        Expression left = and();
        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = and();
            left = new Expression.Logical(operator, left, right);
        }

        return left;
    }

    private Expression and() {
        Expression left = equality();
        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            left = new Expression.Logical(operator, left, right);
        }

        return left;
    }

    private Expression equality() {
        Expression left = equalityMissingLeft();
        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = equalityMissingLeft();
            left = new Expression.Binary(operator, left, right);
        }

        return left;
    }

    private Expression equalityMissingLeft() {
        if (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            comparison();
            throw error(operator, "Expect left-hand operand.");
        }

        return comparison();
    }

    private Expression comparison() {
        Expression left = comparisonMissingLeft();
        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = comparisonMissingLeft();
            left = new Expression.Binary(operator, left, right);
        }

        return left;
    }

    private Expression comparisonMissingLeft() {
        if (match(TokenType.GREATER, TokenType.GREATER_EQUAL,
                TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            term();
            throw error(operator, "Expect left-hand operand.");
        }

        return term();
    }

    private Expression term() {
        Expression left = termMissingLeft();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = termMissingLeft();
            left = new Expression.Binary(operator, left, right);
        }

        return left;
    }

    private Expression termMissingLeft() {
        if (match(TokenType.PLUS)) {
            Token operator = previous();
            factor();
            throw error(operator, "Expect left-hand operand.");
        }

        return factor();
    }

    private Expression factor() {
        Expression left = factorMissingLeft();
        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expression right = factorMissingLeft();
            left = new Expression.Binary(operator, left, right);
        }

        return left;
    }

    private Expression factorMissingLeft() {
        if (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            unary();
            throw error(operator, "Expect left-hand operand.");
        }

        return unary();
    }

    private Expression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            return new Expression.Unary(previous(), unary());
        } else {
            return call();
        }
    }

    private Expression call() {
        Expression expression = primary();
        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expression = call(expression);
            } else {
                break;
            }
        }

        return expression;
    }

    private Expression call(Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() > 254) {
                    error(peek(), "Can't have more than 255 arguments.");
                }

                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }
        Token paren = consume(TokenType.RIGHT_PAREN,
             "Expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
    }

    private Expression primary() {
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.Literal(previous().literal);
        } else if (match(TokenType.TRUE)) {
            return new Expression.Literal(true);
        } else if (match(TokenType.FALSE)) {
            return new Expression.Literal(false);
        } else if (match(TokenType.NIL)) {
            return new Expression.Literal(null);
        } else if (match(TokenType.LEFT_PAREN)) {
            Expression expression = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        } else if (match(TokenType.IDENTIFIER)) {
            return new Expression.Variable(previous());
        } else {
            throw error(peek(), "Expect expression.");
        }
    }

    private boolean match(TokenType... tokenTypes) {
        for (TokenType tokenType : tokenTypes) {
            if (check(tokenType)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType tokenType) {
        return hasMoreTokens() && peek().type == tokenType;
    }

    private Token advance() {
        if (hasMoreTokens()) {
            current++;
        }

        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean hasMoreTokens() {
        return peek().type != TokenType.EOF;
    }

    private Token consume(TokenType tokenType, String msg) {
        if (check(tokenType)) {
            return advance();
        }

        throw error(peek(), msg);
    }

    private ParseException error(Token token, String msg) {
        Lox.error(token, msg);
        return new ParseException();
    }

    private void synchronize() {
        advance();
        while (hasMoreTokens()) {
            if (previous().type == TokenType.SEMICOLON) {
                return;
            }

            switch (peek().type) {
                case CLASS:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }

            advance();
        }
    }

    private static class ParseException extends RuntimeException { }
}
