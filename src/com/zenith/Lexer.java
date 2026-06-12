
package com.zenith;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zenith.TokenType.*;

public class Lexer {
    private final String source; //entire source code as a string
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Lexer(String source){
        this.source = source;
    }

    private boolean isAtEnd(){
        return current >= source.length(); // helper function that tells us if we have consumed all the characters
    }

    List<Token> scanTokens(){
        while (!isAtEnd()){
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken(){  // reads one character and decides what token it starts
        char c = advance();
        switch(c){
            case '(' : addToken(LEFT_PAREN); break;
            case ')' : addToken(RIGHT_PAREN); break;
            case '{' : addToken(LEFT_BRACE); break;
            case '}' : addToken(RIGHT_BRACE); break;
            case ',' : addToken(COMMA); break;
            case '%' : addToken(PERCENT); break;
            case ';' : addToken(SEMICOLON); break;
            case '*' : addToken(STAR); break;
            case '!' : addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=' : addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<' : addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>' : addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '-' : addToken(match('>') ? ARROW : match('-') ? MINUS_MINUS : MINUS); break;
            case '+' : addToken(match('+') ? PLUS_PLUS : PLUS); break;
            case '/' : if(match('/')){
                // a comment goes until the end of the line
                while (peek() != '\n' && !isAtEnd()) advance();
            }else{
                addToken(SLASH);
            }
            break;

            case ' ':
            case '\r':
            case '\t':
                //ignore whitespace
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;

            default:
                if(isDigit(c)){
                    number();
                }else if(isAlpha(c)){
                    identifier();
                }else{
                    Main.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private char advance(){ // consumes the next character in the source file and returns it
        return source.charAt(current++);
    }

    private void addToken(TokenType type){ // adds token with no literal value
        addToken(type,null);
    }

    private void addToken(TokenType type, Object literal){ // adds a token with literal value
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected){
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }
        if(isAtEnd()){
            Main.error(line,"Unterminated string.");
            return;
            // the closing ".
        }
        advance();
    
        //trim the surrounding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    private void number(){
        while(isDigit(peek())) advance();

        //look for fractional part
        if(peek() == '.' && isDigit(peekNext())){
            //consume the "."
            advance();
            while(isDigit(peek())) advance();
            addToken(FLOAT, Double.parseDouble(source.substring(start,current)));
        }else{
            addToken(INTEGER, Integer.parseInt(source.substring(start,current)));
        }
    }

    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private void identifier(){
        while(isAlphaNumeric(peek())) advance();
        String text = source.substring(start,current);
        TokenType type = keywords.get(text);
        if(type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("or",      OR);
        keywords.put("if",      IF);
        keywords.put("else",    ELSE);
        keywords.put("true",    TRUE);
        keywords.put("false",   FALSE);
        keywords.put("fn",      FN);
        keywords.put("return",  RETURN);
        keywords.put("while",   WHILE);
        keywords.put("for",     FOR);
        keywords.put("int",     INT);
        keywords.put("float",   FLOAT_TYPE);
        keywords.put("string",  STRING_TYPE);
        keywords.put("bool",    BOOL);
        keywords.put("writeln", WRITELN);
    }
}
