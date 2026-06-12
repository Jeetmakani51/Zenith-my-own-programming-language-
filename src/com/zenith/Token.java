package com.zenith;
/**
 * Token.java — Zenith Language
 *
 * Represents a single token produced by the Lexer.
 * Each token has a type (what it is), a lexeme (raw source text),
 * a literal value (for numbers/strings, otherwise null),
 * and a line number (for error reporting).
 *
 * Example: integer literal 42 on line 3 →
 *   Token(INTEGER, "42", 42, 3)
 */



public class Token{
    final TokenType type;
    final String lexeme;
    final Object literal;
    final int line;

    Token(TokenType type, String lexeme, Object literal, int line){
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString(){
        return type + " " + lexeme + " " + literal;
    }
}