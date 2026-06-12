
package com.zenith.ast;

import com.zenith.Token;
import java.util.List;

public abstract class Stmt {

    // A bare expression used as a statement → x + 5;
    public static class Expression extends Stmt {
        public final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }
    }

    // Variable declaration → int x = 10;
    public static class VarDecl extends Stmt {
        public final Token type;
        public final Token name;
        public final Expr initializer;

        public VarDecl(Token type, Token name, Expr initializer) {
            this.type = type;
            this.name = name;
            this.initializer = initializer;
        }
    }

    // Print statement → writeln(x);
    public static class Writeln extends Stmt {
        public final Expr expression;

        public Writeln(Expr expression) {
            this.expression = expression;
        }
    }

    // If/else → if (condition) { } else { }
    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch; // null if no else

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }

    // While loop → while (condition) { }
    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }
    }

    // Block of statements → { stmt1; stmt2; }
    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }
    }

    // Function declaration → fn add(int a, int b) -> int { }
    public static class Function extends Stmt {
        public final Token name;
        public final List<Token> params;
        public final List<Token> paramTypes;
        public final Token returnType;
        public final List<Stmt> body;

        public Function(Token name, List<Token> params, List<Token> paramTypes, Token returnType, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.paramTypes = paramTypes;
            this.returnType = returnType;
            this.body = body;
        }
    }

    // Return statement → return x + 1;
    public static class Return extends Stmt {
        public final Token keyword;
        public final Expr value; // null if bare return

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }
    }
}