
/**
 * ZenithFunction.java — Zenith Language
*
* Represents a callable function at runtime.
* Wraps a Stmt.Function AST node and the Environment
* where the function was defined (its closure).
*
* Called by Interpreter.evaluateCall()
*/

package com.zenith;

import java.util.List;
import com.zenith.ast.Stmt;

public class ZenithFunction {
    private final Stmt.Function declaration;
    private final Environment closure; // scope where fn was defined

    public ZenithFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    public Object call(Interpreter interpreter, List<Object> arguments) {
        // create a new scope for this function call
        Environment fnEnv = new Environment(closure);

        // bind each argument to its parameter name
        for (int i = 0; i < declaration.params.size(); i++) {
            fnEnv.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        // execute the function body
        try {
            interpreter.executeBlock(
                new com.zenith.ast.Stmt.Block(declaration.body), fnEnv
            );
        } catch (Interpreter.ReturnException returnValue) {
            return returnValue.value; // catch the return value
        }

        return null; // no return statement → return null
    }

    public int arity() {
        return declaration.params.size(); // number of expected arguments
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}

