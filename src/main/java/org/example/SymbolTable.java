package org.example;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SymbolTable {
    private final Map<String, SymbolEntry> symbols = new LinkedHashMap<>();

    public SymbolEntry register(Token token, String symbolType, int line) {
        return register(token.type(), token.lexeme(), token.originalLength(), symbolType, line);
    }

    public SymbolEntry register(TokenType atom, String lexeme, int originalLength,
                                String symbolType, int line) {
        if (!isSymbol(atom)) {
            throw new IllegalArgumentException("Atomo nao pertence a tabela de simbolos: " + atom);
        }
        String key = lexeme.toUpperCase(Locale.ROOT);
        if (key.length() > 30) {
            throw new IllegalArgumentException("Lexema deve estar truncado em 30 caracteres");
        }
        if (originalLength < key.length()) {
            throw new IllegalArgumentException("Comprimento original menor que o armazenado");
        }

        SymbolEntry existing = symbols.get(key);
        if (existing != null) {
            existing.updateLength(originalLength);
            existing.AddOccurrence(line);
            if (existing.getSymbolType() == null && symbolType != null) {
                existing.setSymbolType(symbolType);
            }
            return existing;
        }

        SymbolEntry entry = new SymbolEntry(symbols.size() + 1, atom, key,
                originalLength, symbolType, line);
        symbols.put(key, entry);
        return entry;
    }

    public SymbolEntry find(String lexeme) {
        return symbols.get(lexeme.toUpperCase(Locale.ROOT));
    }

    public void update(String lexeme, int originalLength, int line) {
        SymbolEntry entry = find(lexeme);
        if (entry == null) {
            throw new IllegalArgumentException("Simbolo nao encontrado: " + lexeme);
        }
        if (originalLength < entry.getTruncatedLength()) {
            throw new IllegalArgumentException("Comprimento original invalido");
        }
        entry.updateLength(originalLength);
        entry.AddOccurrence(line);
    }

    public List<SymbolEntry> getEntries() {
        return List.copyOf(symbols.values());
    }

    public void clear() {
        symbols.clear();
    }

    private boolean isSymbol(TokenType atom) {
        return atom == TokenType.PROGRAM_NAME || atom == TokenType.VARIABLE
                || atom == TokenType.FUNCTION_NAME;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (SymbolEntry entry : symbols.values()) {
            result.append(entry).append("\n");
        }
        return result.toString();
    }
}
