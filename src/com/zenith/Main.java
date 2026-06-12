
package com.zenith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Main {
    static boolean hadError = false;
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
        if (hadError) System.exit(65);
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

        for(Token token : tokens){
            System.out.println(token);
        }
        
    }

    static void error(int line, String message){
        System.err.println("[line " + line + "] Error: " + message);
        hadError = true;
    }
}
