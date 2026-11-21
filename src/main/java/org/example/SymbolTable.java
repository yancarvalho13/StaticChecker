
package org.example;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SymbolTable {

    private final Map<String, SymbolEntry> symbols =
            new LinkedHashMap<>();

    public SymbolEntry register(
            TokenType atom,
            String lexeme,
            int originalLength,
            String symbolType,
            int line) {

        if (!isSymbol(atom)) {
            throw new IllegalArgumentException(
                    "Atomo nao pertence a tabela de simbolos: " + atom
            );
        }

        String key = lexeme.toUpperCase(Locale.ROOT);

        if (key.length() > 30) {
            throw new IllegalArgumentException(
                    "Lexema deve estar truncado em 30 caracteres"
            );
        }

        if (originalLength < key.length()) {
            throw new IllegalArgumentException(
                    "Comprimento original menor que o armazenado"
            );
        }

        SymbolEntry existing = symbols.get(key);

        if (existing != null) {
            existing.updateLength(originalLength);
            existing.AddOccurrence(line);
            return existing;
        }

        int index = symbols.size() + 1;

        SymbolEntry entry = new SymbolEntry(
                index,
                atom,
                key,
                originalLength,
                symbolType,
                line
        );

        symbols.put(key, entry);

        return entry;
    }

    public SymbolEntry find(String lexeme) {
        return symbols.get(lexeme.toUpperCase(Locale.ROOT));
    }

    public void update(
            String lexeme,
            int originalLength,
            int line) {

        SymbolEntry entry = find(lexeme);

        if (entry == null) {
            throw new IllegalArgumentException(
                    "Simbolo nao encontrado: " + lexeme
            );
        }

        if (originalLength < entry.getTruncatedLength()) {
            throw new IllegalArgumentException(
                    "Comprimento original invalido"
            );
        }

        entry.updateLength(originalLength);
        entry.AddOccurrence(line);
    }

    public List<SymbolEntry> getEntries() {
        return List.copyOf(symbols.values());
    }

    private boolean isSymbol(TokenType atom) {
        return switch (atom) {
            case PROGRAM_NAME, VARIABLE, FUNCTION_NAME,
                 INT_CONST, REAL_CONST, STRING_CONST,
                 CHAR_CONST -> true;
            default -> false;
        };
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
