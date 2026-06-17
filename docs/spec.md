## Main.java

Entry point for the Zenith interpreter.

- File mode: `zenith file.zn` — reads and runs a source file
- REPL mode: `zenith` — interactive prompt, line by line
- Error format: `[line N] Error: <message>`
- Exit codes: 64 = bad usage, 65 = runtime/compile error

## TokenType.java

Enum of all token categories in Zenith.
Groups: single-char, one/two-char, literals, keywords, special (EOF).
Used by the Lexer to label tokens and by the Parser to make decisions.

## Token.java

Represents a single token produced by the Lexer.
Fields: type (TokenType), lexeme (String), literal (Object), line (int)
toString() format: "TYPE lexeme literal"
Example: Token(INTEGER, "42", 42, 3)

## Lexer — Status: COMPLETE ✅

Tested against examples/hello.zn — all tokens produced correctly.
Token format: TYPE lexeme literal

Verified tokens:

- Keywords: int, string, writeln → correct types
- Identifiers: x, name → IDENTIFIER
- Literals: 10 → INTEGER, "Zenith" → STRING with quotes stripped
- Operators: = → EQUAL
- Punctuation: ; → SEMICOLON, () → LEFT_PAREN/RIGHT_PAREN
- EOF always last

## AST Nodes — Status: COMPLETE ✅

### Expr nodes (produce values)

Binary, Unary, Literal, Variable, Assign, Call, Grouping

### Stmt nodes (perform actions)

Expression, VarDecl, Writeln, If, While, Block, Function, Return

Stmts contain Exprs. Neither has any logic — pure data for the Interpreter to walk.

## Parser.java — Status: COMPLETE ✅

### What it does

Takes the flat List<Token> produced by the Lexer and builds an
Abstract Syntax Tree (AST) made of Expr and Stmt nodes.
Uses recursive descent parsing — every grammar rule is a method.

### Entry point

parse() → returns List<Stmt> representing the entire program

### Grammar (in order of precedence, lowest to highest)

### Methods

| Method                | Purpose                                  |
| --------------------- | ---------------------------------------- |
| parse()               | Entry point, returns List<Stmt>          |
| declaration()         | Top level router — var, fn, or statement |
| varDeclaration()      | Parses int/float/string/bool x = expr;   |
| function()            | Parses fn name(params) -> type { body }  |
| statement()           | Routes to correct statement parser       |
| ifStatement()         | Parses if (cond) { } else { }            |
| whileStatement()      | Parses while (cond) { }                  |
| forStatement()        | Parses for loop, desugars into while     |
| returnStatement()     | Parses return expr;                      |
| writelnStatement()    | Parses writeln(expr);                    |
| block()               | Parses { declarations }                  |
| expressionStatement() | Parses expr;                             |
| expression()          | Entry to expression chain                |
| assignment()          | Handles x = value                        |
| equality()            | Handles == and !=                        |
| comparison()          | Handles < > <= >=                        |
| term()                | Handles + and -                          |
| factor()              | Handles \* / %                           |
| unary()               | Handles ! and - (negation)               |
| primary()             | Handles literals, variables, grouping    |

### Helper methods

| Method                     | Purpose                                |
| -------------------------- | -------------------------------------- |
| match(TokenType...)        | Consume token if it matches any type   |
| check(TokenType)           | Peek at current token type             |
| advance()                  | Consume and return current token       |
| peek()                     | Return current token without consuming |
| previous()                 | Return last consumed token             |
| isAtEnd()                  | True if current token is EOF           |
| consume(TokenType, String) | Consume expected token or throw error  |
| synchronize()              | Recover after parse error              |

### Key design decisions

- for loops are DESUGARED into while loops by the Parser
  → the Interpreter never sees a for loop, only a while
- else branch in if statements is null when absent
- return value is null for bare return statements
- assignment is right-associative: a = b = 5 works correctly
- ParseError is caught in declaration() allowing error recovery
  via synchronize() so multiple errors can be reported at once

### Error format

[line N] Error at 'token': message
[line N] Error at end: message ← for EOF errors
