package org.example;

public record Token(TokenType type, String lexeme, int originalLength,
                    int line, int column, int symbolIndex) {
    public Token(TokenType type, String lexeme, int originalLength, int line, int column) {
        this(type, lexeme, originalLength, line, column, -1);
    }

    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, lexeme.length(), line, column, -1);
    }

    public String atomCode() {
        return type.getCode();
    }

    public Token withSymbolIndex(int index) {
        return new Token(type, lexeme, originalLength, line, column, index);
    }

    @Override
    public String toString() {
        return String.format(
                "[%-18s] Lexema: %-30s | Linha: %-3d | Coluna: %d",
                type, "'" + lexeme + "'", line + 1, column + 1
        );
    }
}
