package com.zenith;
import static com.zenith.TokenType.*;

import java.util.ArrayList;
import java.util.List;

import com.zenith.ast.Expr;
import com.zenith.ast.Stmt;

public class Parser {
    private static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0; //point to the next token eagerly waiting to be parse

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }
    private Expr expression(){
        return assignment();
    }
    private Expr equality(){
        Expr expr = comparison();
        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    private Expr assignment() {
        Expr expr = equality();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private boolean match(TokenType... types){
        for(TokenType type : types){
            if(check(type)){  //checks if current token has any of the given types, if yes then it consumes the token and returns true
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type){ // return true if current token is of given type
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() { // checks if you have run out of tokens to parse
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() { // returns most recently consumed token
        return tokens.get(current - 1);
    }
    private Expr comparison(){
        Expr expr = term();

        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
        Token operator = previous();
        Expr right = unary();
        expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary(){
        if(match(BANG, MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator,right);
        }
        return primary();
    }
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE))  return new Expr.Literal(true);

        if (match(INTEGER, FLOAT, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            Expr expr = new Expr.Variable(previous());

            // function call
            if (match(LEFT_PAREN)) {
                List<Expr> arguments = new ArrayList<>();
                if (!check(RIGHT_PAREN)) {
                    do {
                        arguments.add(expression());
                    } while (match(COMMA));
                }
                consume(RIGHT_PAREN, "Expect ')' after arguments.");
                return new Expr.Call(expr, arguments);
            }

            return expr;
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

            throw error(peek(), message);
    }

    private ParseError error(Token token, String message){
        Main.error(token, message);
        return new ParseError();
    }

    // static void error(Token token, String message) {
    //     if (token.type == TokenType.EOF) {
    //         report(token.line, " at end", message);
    //     } else {
    //         report(token.line, " at '" + token.lexeme + "'", message);
    //     }
    // }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case FN:
                case FOR:
                case IF:
                case WHILE:
                case WRITELN:
                case RETURN:
                return;
                default:
                    break;
            }

            advance();
        }
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(INT, FLOAT_TYPE, STRING_TYPE, BOOL)) return varDeclaration();
            if (match(FN)) return function();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token type = previous();
        Token name = consume(IDENTIFIER, "Expect variable name.");
        consume(EQUAL, "Expect '=' after variable name.");
        Expr initializer = expression();
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.VarDecl(type, name, initializer);
    }

    private Stmt statement() {
        if (match(IF))      return ifStatement();
        if (match(WHILE))   return whileStatement();
        if (match(FOR))     return forStatement();
        if (match(RETURN))  return returnStatement();
        if (match(WRITELN)) return writelnStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt writelnStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'writeln'.");
        Expr value = expression();
        consume(RIGHT_PAREN, "Expect ')' after value.");
        consume(SEMICOLON, "Expect ';' after writeln.");
        return new Stmt.Writeln(value);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after while condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        // initializer
        Stmt initializer;
        if (match(INT, FLOAT_TYPE, STRING_TYPE, BOOL)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        // condition
        Expr condition = expression();
        consume(SEMICOLON, "Expect ';' after for condition.");

        // increment
        Expr increment = expression();
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = statement();

        // desugar: wrap increment into body
        body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));

        // desugar: wrap into while loop
        body = new Stmt.While(condition, body);

        // desugar: wrap initializer around while
        body = new Stmt.Block(List.of(initializer, body));

        return body;
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt.Function function() {
        Token name = consume(IDENTIFIER, "Expect function name.");
        consume(LEFT_PAREN, "Expect '(' after function name.");

        List<Token> params = new ArrayList<>();
        List<Token> paramTypes = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                Token paramType = advance(); // int, float, etc.
                Token paramName = consume(IDENTIFIER, "Expect parameter name.");
                paramTypes.add(paramType);
                params.add(paramName);
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect ')' after parameters.");
        consume(ARROW, "Expect '->' before return type.");
        Token returnType = advance(); // return type token
        consume(LEFT_BRACE, "Expect '{' before function body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, params, paramTypes, returnType, body);
    }
}
