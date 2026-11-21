package org.example;

public record Token(TokenType type, String lexeme, int originalLength,
                    int line, int column) {
    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, lexeme.length(), line, column);
    }

    public String atomCode() {
        return type.getCode();
    }

    @Override
    public String toString() {
        return String.format(
                "[%-18s] Lexema: %-30s | Linha: %-3d | Coluna: %d",
                type, "'" + lexeme + "'", line + 1, column + 1
        );
    }
}
