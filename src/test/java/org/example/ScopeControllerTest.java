package org.example;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScopeControllerTest {
    private Lexer lexer(String source) {
        List<CharStatus> chars = new ArrayList<>();
        int line = 0;
        int column = 0;
        for (char c : source.toCharArray()) {
            chars.add(new CharStatus(c, line, column));
            if (c == '\n') { line++; column = 0; }
            else { column++; }
        }
        return new Lexer(chars);
    }

    private void scan(ScopeController scope, Lexer lexer) {
        Token token;
        do { token = scope.nextToken(lexer); }
        while (token.type() != TokenType.EOF);
    }

    @Test
    void assignsScalarAndArrayTypesAndRegistersProgramName() {
        SymbolTable table = new SymbolTable();
        ScopeController scope = new ScopeController(table);
        scope.startAnalysis();
        scan(scope, lexer("program app declarations "
                + "varType integer: count; varType integer[]: values[10] "
                + "endDeclarations endProgram"));

        assertEquals(TokenType.PROGRAM_NAME, table.find("app").getAtom());
        assertEquals("IN", table.find("count").getSymbolType());
        assertEquals("AI", table.find("values").getSymbolType());
    }

    @Test
    void classifiesFunctionAndParametersAndAppliesTypes() {
        SymbolTable table = new SymbolTable();
        ScopeController scope = new ScopeController(table);
        scope.startAnalysis();
        scan(scope, lexer("program app declarations endDeclarations functions "
                + "funcType real: compute(paramType integer: input) "
                + "return input endFunction endFunctions endProgram"));

        assertEquals(TokenType.FUNCTION_NAME, table.find("compute").getAtom());
        assertEquals("FP", table.find("compute").getSymbolType());
        assertEquals(TokenType.VARIABLE, table.find("input").getAtom());
        assertEquals("IN", table.find("input").getSymbolType());
        assertEquals(2, table.find("input").getLines().size());
    }
}
