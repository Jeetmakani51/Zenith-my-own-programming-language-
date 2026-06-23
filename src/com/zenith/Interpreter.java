package com.zenith;

import static com.zenith.TokenType.BANG_EQUAL;
import static com.zenith.TokenType.EQUAL_EQUAL;
import static com.zenith.TokenType.GREATER;
import static com.zenith.TokenType.GREATER_EQUAL;
import static com.zenith.TokenType.LESS;
import static com.zenith.TokenType.LESS_EQUAL;
import static com.zenith.TokenType.MINUS;
import static com.zenith.TokenType.PERCENT;
import static com.zenith.TokenType.PLUS;
import static com.zenith.TokenType.SLASH;
import static com.zenith.TokenType.STAR;

import java.util.ArrayList;
import java.util.List;

import com.zenith.ast.Expr;
import com.zenith.ast.Stmt;

public class Interpreter {
    private Environment environment = new Environment();
    // routes Expr nodes to the right method
    private Object evaluate(Expr expr) {
        if (expr instanceof Expr.Literal)   return evaluateLiteral((Expr.Literal) expr);
        if (expr instanceof Expr.Grouping)  return evaluateGrouping((Expr.Grouping) expr);
        if (expr instanceof Expr.Unary)     return evaluateUnary((Expr.Unary) expr);
        if (expr instanceof Expr.Binary)    return evaluateBinary((Expr.Binary) expr);
        if (expr instanceof Expr.Variable)  return evaluateVariable((Expr.Variable) expr);
        if (expr instanceof Expr.Assign)    return evaluateAssign((Expr.Assign) expr);
        if (expr instanceof Expr.Call)      return evaluateCall((Expr.Call) expr);
        throw new Environment.RuntimeError(null, "Unknown expression type.");
    }

    // routes Stmt nodes to the right method
    private void execute(Stmt stmt) {
        if (stmt instanceof Stmt.Expression)  executeExpression((Stmt.Expression) stmt);
        else if (stmt instanceof Stmt.VarDecl)     executeVarDecl((Stmt.VarDecl) stmt);
        else if (stmt instanceof Stmt.Writeln)     executeWriteln((Stmt.Writeln) stmt);
        else if (stmt instanceof Stmt.If)          executeIf((Stmt.If) stmt);
        else if (stmt instanceof Stmt.While)       executeWhile((Stmt.While) stmt);
        else if (stmt instanceof Stmt.Block)       executeBlock((Stmt.Block) stmt, new Environment(environment));
        else if (stmt instanceof Stmt.Function)    executeFunction((Stmt.Function) stmt);
        else if (stmt instanceof Stmt.Return)      executeReturn((Stmt.Return) stmt);
    }

    //the entry point

    public void interpret(List<Stmt> statements){
        try{
            for(Stmt stmt : statements){
                execute(stmt);
            }
        }catch(Environment.RuntimeError error){
            Main.runtimeError(error);
        }
    }

    private Object evaluateLiteral(Expr.Literal expr) {
        return expr.value;
    }

    private Object evaluateGrouping(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    private Object evaluateUnary(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                if (right instanceof Integer) return -(int) right;
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                break;
        }
        return null;
    }

    private Object evaluateBinary(Expr.Binary expr) {
        Object left  = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            // arithmetic
            case PLUS:
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left + (int) right;
                if (left instanceof Double || right instanceof Double)
                    return toDouble(left) + toDouble(right);
                if (left instanceof String || right instanceof String)
                    return stringify(left) + stringify(right);
                throw new Environment.RuntimeError(expr.operator, "Operands must be numbers or strings.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left - (int) right;
                return toDouble(left) - toDouble(right);
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left * (int) right;
                return toDouble(left) * toDouble(right);
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if (right instanceof Integer && (int) right == 0)
                    throw new Environment.RuntimeError(expr.operator, "Division by zero.");
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left / (int) right;
                return toDouble(left) / toDouble(right);
            case PERCENT:
                checkNumberOperands(expr.operator, left, right);
                if (left instanceof Integer && right instanceof Integer)
                    return (int) left % (int) right;
                return toDouble(left) % toDouble(right);
            // comparison
            case GREATER:       return toDouble(left) > toDouble(right);
            case GREATER_EQUAL: return toDouble(left) >= toDouble(right);
            case LESS:          return toDouble(left) < toDouble(right);
            case LESS_EQUAL:    return toDouble(left) <= toDouble(right);
            // equality
            case EQUAL_EQUAL:   return isEqual(left, right);
            case BANG_EQUAL:    return !isEqual(left, right);
            default: break;
        }
        return null;
    }

    // is a value truthy? false and null are falsy, everything else is truthy
private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    // equality check — handles null safely
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    // convert int or double to double for mixed arithmetic
    private double toDouble(Object obj) {
        if (obj instanceof Integer) return ((Integer) obj).doubleValue();
        return (double) obj;
    }

    // convert any value to a printable string
    private String stringify(Object object) {
        if (object == null) return "null";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) text = text.substring(0, text.length() - 2);
            return text;
        }
        return object.toString();
    }

    // check single operand is a number
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Integer || operand instanceof Double) return;
        throw new Environment.RuntimeError(operator, "Operand must be a number.");
    }

    // check both operands are numbers
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if ((left instanceof Integer || left instanceof Double) &&
            (right instanceof Integer || right instanceof Double)) return;
        throw new Environment.RuntimeError(operator, "Operands must be numbers.");
    }

    // variable lookup
    private Object evaluateVariable(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    //Assignment
    private Object evaluateAssign(Expr.Assign expr){
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    //variable declaration
    private void executeVarDecl(Stmt.VarDecl stmt) {
        Object value = evaluate(stmt.initializer);
        environment.define(stmt.name.lexeme, value);
    }

    //writeln
    private void executeWriteln(Stmt.Writeln stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
    }

    //expression statement
    private void executeExpression(Stmt.Expression stmt) {
        evaluate(stmt.expression);
    }

    //if condition
    private void executeIf(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
    }

    //while loop
    private void executeWhile(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
    }

    //block - creates a new child scope
    void executeBlock(Stmt.Block stmt, Environment blockEnv) {
        Environment previous = this.environment;
        try {
            this.environment = blockEnv;
            for (Stmt statement : stmt.statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous; // always restore parent scope
        }
    }

    private void executeFunction(Stmt.Function stmt) {
        ZenithFunction function = new ZenithFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
    }

    private void executeReturn(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        throw new ReturnException(value);
    }

    private Object evaluateCall(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof ZenithFunction)) {
            throw new Environment.RuntimeError(null, "Can only call functions.");
        }

        ZenithFunction function = (ZenithFunction) callee;

        if (arguments.size() != function.arity()) {
            throw new Environment.RuntimeError(null,
                "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    // in Interpreter.java — add this inner class
    static class ReturnException extends RuntimeException {
        final Object value;
        ReturnException(Object value) {
            super(null, null, true, false); // disable stack trace for performance
            this.value = value;
        }
    }
}
