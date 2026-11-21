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

    @Test
    void classifiesVectorParametersAndMultipleDeclarations() {
        SymbolTable table = new SymbolTable();
        ScopeController scope = new ScopeController(table);
        scope.startAnalysis();
        scan(scope, lexer("program app declarations endDeclarations functions "
                + "funcType void: run(paramType integer: values[10], count) "
                + "return endFunction endFunctions endProgram"));

        assertEquals(TokenType.FUNCTION_NAME, table.find("run").getAtom());
        assertEquals("VD", table.find("run").getSymbolType());
        assertEquals("AI", table.find("values").getSymbolType());
        assertEquals("IN", table.find("count").getSymbolType());
    }

    @Test
    void mapsEverySupportedScalarAndArrayType() {
        SymbolTable table = new SymbolTable();
        ScopeController scope = new ScopeController(table);
        scope.startAnalysis();
        scan(scope, lexer("program app declarations "
                + "varType real: r; varType real[]: ra[1]; "
                + "varType string: s; varType string[]: sa[1]; "
                + "varType boolean: b; varType boolean[]: ba[1]; "
                + "varType character: c; varType character[]: ca[1]; "
                + "endDeclarations endProgram"));

        assertEquals("FP", table.find("r").getSymbolType());
        assertEquals("AF", table.find("ra").getSymbolType());
        assertEquals("ST", table.find("s").getSymbolType());
        assertEquals("AS", table.find("sa").getSymbolType());
        assertEquals("BL", table.find("b").getSymbolType());
        assertEquals("AB", table.find("ba").getSymbolType());
        assertEquals("CH", table.find("c").getSymbolType());
        assertEquals("AC", table.find("ca").getSymbolType());
    }

    @Test
    void returnsSymbolIndexOnEachIdentifierToken() {
        SymbolTable table = new SymbolTable();
        ScopeController scope = new ScopeController(table);
        scope.startAnalysis();
        Lexer lexer = lexer("program app");
        Token token;
        do { token = scope.nextToken(lexer); }
        while (token.type() != TokenType.PROGRAM_NAME);

        assertEquals(1, token.symbolIndex());
        assertEquals("C03", token.atomCode());
    }
}
