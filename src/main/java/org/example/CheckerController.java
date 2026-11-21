
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

        var path = file.getAbsolutePath();

        sourceReader.GenerateFileCharsMetadata(path);
        var fileChars = sourceReader.GetTokenList();

        var lexer = new Lexer(fileChars);
        var tokens = lexer.Tokenize();

        tokens.forEach(System.out::println);

        for (Token token : tokens) {

            switch (token.type()) {

                case PROGRAM_NAME, VARIABLE, FUNCTION_NAME,
                     INT_CONST, REAL_CONST, STRING_CONST,
                     CHAR_CONST -> {

                    symbolTable.register(
                            token.type(),
                            token.lexeme(),
                            token.lexeme().length(),
                            getSymbolType(token.type()),
                            token.line() + 1
                    );
                }

                default -> {
                }
            }
        }

        System.out.println("\nTABELA DE SIMBOLOS:");
        System.out.println(symbolTable);
    }

    private String getSymbolType(TokenType type) {
        return switch (type) {
            case INT_CONST -> "IN";
            case REAL_CONST -> "FP";
            case STRING_CONST -> "ST";
            case CHAR_CONST -> "CH";
            default -> null;
        };
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }
}

