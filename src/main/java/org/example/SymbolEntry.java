package org.example;

import java.util.ArrayList;
import java.util.List;

public class SymbolEntry {

    private final int index;
    private final TokenType atom;
    private final String lexeme;
    private int originalLength;
    private final int truncatedLength;
    private String symbolType;
    private final List<Integer> lines = new ArrayList<>();

    public SymbolEntry(
            int index,
            TokenType atom,
            String lexeme,
            int originalLength,
            String symbolType,
            int line) {

        this.index = index;
        this.atom = atom;
        this.lexeme = lexeme;
        this.originalLength = originalLength;
        this.truncatedLength = lexeme.length();
        this.symbolType = symbolType;

        AddOccurrence(line);
    }

    public void AddOccurrence(int line){
        if (lines.size() < 5){
            lines.add(line);
        }
    }
    public void updateLength(int length) {
        originalLength = length;
    }

    public int getIndex() {
        return index;
    }

    public TokenType getAtom() {
        return atom;
    }

    public String getLexeme() {
        return lexeme;
    }

    public int getOriginalLength() {
        return originalLength;
    }

    public int getTruncatedLength() {
        return truncatedLength;
    }

    public String getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(String symbolType) {
        this.symbolType = symbolType;
    }

    public List<Integer> getLines() {
        return List.copyOf(lines);
    }

    @Override
    public String toString() {
        return String.format(
                "[%03d] %-30s | Atomo: %-15s | Tam: %d/%d | Tipo: %-2s | Linhas: %s",
                index,
                lexeme,
                atom,
                originalLength,
                truncatedLength,
                symbolType == null ? "--" : symbolType,
                lines
        );
    }
}

