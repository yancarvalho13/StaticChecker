package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SymbolTableTest {
    @Test
    void registersOnlyIdentifiersWithOfficialCodesAndSequentialIndexes() {
        SymbolTable table = new SymbolTable();
        SymbolEntry first = table.register(TokenType.VARIABLE, "alpha", 5, null, 1);
        SymbolEntry second = table.register(TokenType.FUNCTION_NAME, "work", 4, "IN", 2);
        SymbolEntry third = table.register(TokenType.PROGRAM_NAME, "test", 4, null, 3);

        assertEquals(1, first.getIndex());
        assertEquals("C01", first.getAtomCode());
        assertEquals(2, second.getIndex());
        assertEquals("C02", second.getAtomCode());
        assertEquals(3, third.getIndex());
        assertEquals("C03", third.getAtomCode());
        assertThrows(IllegalArgumentException.class,
                () -> table.register(TokenType.INT_CONST, "10", 2, "IN", 4));
    }

    @Test
    void duplicateNamesShareEntryIgnoreCaseAndKeepFirstFiveOccurrences() {
        SymbolTable table = new SymbolTable();
        SymbolEntry first = table.register(TokenType.VARIABLE, "value", 5, null, 1);
        SymbolEntry duplicate = table.register(TokenType.VARIABLE, "VALUE", 5, "IN", 2);
        table.register(TokenType.VARIABLE, "Value", 5, null, 2);
        table.register(TokenType.VARIABLE, "value", 5, null, 3);
        table.register(TokenType.VARIABLE, "value", 5, null, 4);
        table.register(TokenType.VARIABLE, "value", 5, null, 5);
        table.register(TokenType.VARIABLE, "value", 5, null, 6);

        assertSame(first, duplicate);
        assertEquals(1, table.getEntries().size());
        assertEquals(java.util.List.of(1, 2, 2, 3, 4), first.getLines());
        assertEquals("IN", first.getSymbolType());
    }

    @Test
    void preservesTruncatedAndOriginalLengthsAcrossTokenRegistration() {
        String stored = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234";
        Token token = new Token(TokenType.VARIABLE, stored, 35, 0, 0);
        SymbolEntry entry = new SymbolTable().register(token, null, 1);

        assertEquals(35, entry.getOriginalLength());
        assertEquals(30, entry.getTruncatedLength());
    }

    @Test
    void acceptsOnlySpecifiedSymbolTypesAndCanBeClearedForAnotherAnalysis() {
        SymbolTable table = new SymbolTable();
        SymbolEntry entry = table.register(TokenType.VARIABLE, "items", 5, "AI", 1);
        assertEquals("AI", entry.getSymbolType());
        assertThrows(IllegalArgumentException.class, () -> entry.setSymbolType("ARRAY"));
        table.clear();
        assertTrue(table.getEntries().isEmpty());
    }
}
