
package com.zenith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.zenith.ast.Stmt;

public class Main {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    public static void main(String[] args) throws IOException{
        if(args.length > 1){
            System.err.println("Usage: zenith [file.zn]");
            System.exit(64);
        }else if(args.length == 1){
            runFile(args[0]);
        }else{
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path)); //give path to a file, it reads it and executes it
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(70);
    }

    //REPL
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in); //read a line
        BufferedReader reader = new BufferedReader(input);//evaluate it
        for(;;){ // infinite loop
            System.out.print("> ");//print it and loop
            String line = reader.readLine();
            if(line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source){
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();
        if(hadError) return;

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if(hadError) return;

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements);
        
    }

    static void error(int line, String message){
        System.err.println("[line " + line + "] Error: " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(Environment.RuntimeError error) {
        System.err.println("[line " + error.token.line + "] Runtime Error: " + error.getMessage());
        hadRuntimeError = true;
    }
}
