package com.zenith.ast;
import com.zenith.Token;
import java.util.List;

public abstract class Expr {
    public static class Binary extends Expr{
        public final Expr left;
        public final Token operator;
        public final Expr right;

        public Binary(Expr left, Token operator, Expr right){
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }
    //unary : OP right -> !x, -x
    public static class Unary extends Expr{
        public final Token operator;
        public final Expr right;

        public Unary(Token operator, Expr right){
            this.operator = operator;
            this.right = right;
        }
    }

    //literal : a raw value -> 10, 3.14, "hello", true
    public static class Literal extends Expr{
        public final Object value;
        public Literal(Object value){
            this.value = value;
        }
    }

    // Variable : a variable reference -> x, name
    public static class Variable extends Expr{
        public final Token name;
        public Variable(Token name){
            this.name = name;
        }
    }

    //assign : variable assignment -> x = 5
    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    // Call: function call → add(1, 2)
    public static class Call extends Expr {
        public final Expr callee;
        public final List<Expr> arguments;

        public Call(Expr callee, List<Expr> arguments) {
            this.callee = callee;
            this.arguments = arguments;
        }
    }

    // Grouping: parenthesised expression → (x + 5)
    public static class Grouping extends Expr {
        public final Expr expression;

        public Grouping(Expr expression) {
            this.expression = expression;
        }
    }
}
