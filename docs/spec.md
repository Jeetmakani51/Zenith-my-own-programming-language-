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