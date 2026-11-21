
package org.example;

import com.sun.jdi.InvalidTypeException;
import java.io.File;
import java.io.IOException;

public class CheckerController {

    private final SourceReader sourceReader;
    private final SymbolTable symbolTable;

    public CheckerController() {
        sourceReader = new SourceReader();
        symbolTable = new SymbolTable();
    }

    public void StartAnalisys(File file)
            throws InvalidTypeException, IOException {

        symbolTable.clear();
        var path = file.getAbsolutePath();

        sourceReader.GenerateFileCharsMetadata(path);
        var fileChars = sourceReader.GetTokenList();

        var lexer = new Lexer(fileChars);
        var scopeController = new ScopeController(symbolTable);
        scopeController.startAnalysis();
        Token token;
        do {
            token = scopeController.nextToken(lexer);
            if (token.type() != TokenType.EOF) {
                System.out.println(token);
            }
        } while (token.type() != TokenType.EOF);

        System.out.println("\nTABELA DE SIMBOLOS:");
        System.out.println(symbolTable);
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}

