package com.zenith;
import java.util.HashMap;
import java.util.Map;

public class Environment{
    final Environment enclosing;

    //stores variable name -> value for this scope
    private final Map<String, Object> values = new HashMap<>();

    Environment(){
        this.enclosing = null;
    }

    // block scope constructor - has parent
    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }

    public void define(String name, Object value){
        values.put(name,value);
    }

    public Object get(Token name){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        if(enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    //assign to existing variable - walks up scope chain to find it

    public void assign(Token name, Object value){
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme,value);
            return;
        }
        if(enclosing != null){
            enclosing.assign(name,value);
            return;
        }
        throw new RuntimeError(name,"Undefined variable '" + name.lexeme + "'.");
    }

    //thown when a variable is accessed or assigned but not defined

    public static class RuntimeError extends RuntimeException {
        public final Token token;

        public RuntimeError(Token token, String message) {
            super(message);
            this.token = token;
        }
    }
}