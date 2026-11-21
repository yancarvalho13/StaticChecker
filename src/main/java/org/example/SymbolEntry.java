package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SymbolEntry {
    private static final Set<String> SYMBOL_TYPES = Set.of(
            "FP", "IN", "ST", "CH", "BL", "VD", "AF", "AI", "AS", "AC", "AB");

    private final int index;
    private final String atomCode;
    private final TokenType atom;
    private final String lexeme;
    private int originalLength;
    private final int truncatedLength;
    private String symbolType;
    private final List<Integer> lines = new ArrayList<>();

    public SymbolEntry(int index, TokenType atom, String lexeme, int originalLength,
                       String symbolType, int line) {
        this(index, atom.getCode(), atom, lexeme, originalLength, symbolType, line);
    }

    public SymbolEntry(int index, String atomCode, TokenType atom, String lexeme,
                       int originalLength, String symbolType, int line) {
        this.index = index;
        this.atomCode = atomCode;
        this.atom = atom;
        this.lexeme = lexeme;
        this.originalLength = originalLength;
        this.truncatedLength = lexeme.length();
        setSymbolType(symbolType);
        AddOccurrence(line);
    }

    public void AddOccurrence(int line) {
        if (lines.size() < 5) {
            lines.add(line);
        }
    }

    public void updateLength(int length) {
        originalLength = Math.max(originalLength, length);
    }

    public int getIndex() { return index; }
    public String getAtomCode() { return atomCode; }
    public TokenType getAtom() { return atom; }
    public String getLexeme() { return lexeme; }
    public int getOriginalLength() { return originalLength; }
    public int getTruncatedLength() { return truncatedLength; }
    public String getSymbolType() { return symbolType; }

    public void setSymbolType(String symbolType) {
        if (symbolType != null && !SYMBOL_TYPES.contains(symbolType)) {
            throw new IllegalArgumentException("Tipo de simbolo invalido: " + symbolType);
        }
        this.symbolType = symbolType;
    }

    public List<Integer> getLines() { return List.copyOf(lines); }

    @Override
    public String toString() {
        return String.format(
                "[%03d] Código: %-3s | %-30s | Atomo: %-15s | Tam: %d/%d | Tipo: %-2s | Linhas: %s",
                index, atomCode, lexeme, atom, originalLength, truncatedLength,
                symbolType == null ? "--" : symbolType, lines);
    }
}
