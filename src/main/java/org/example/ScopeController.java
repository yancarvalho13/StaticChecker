package org.example;

public class ScopeController {
    private final SymbolTable symbolTable;
    private DeclarationKind declarationKind = DeclarationKind.NONE;
    private String declaredType;
    private boolean arrayDeclaration;
    private boolean declarationIdentifiers;
    private boolean expectingFunctionName;
    private boolean expectingFunctionReturnType;

    public ScopeController(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public void startAnalysis() {
        declarationKind = DeclarationKind.NONE;
        declaredType = null;
        arrayDeclaration = false;
        declarationIdentifiers = false;
        expectingFunctionName = false;
        expectingFunctionReturnType = false;
    }

    public Token nextToken(Lexer lexer) {
        prepareLexer(lexer);
        Token token = lexer.nextToken();
        observe(token);
        return token;
    }

    private void prepareLexer(Lexer lexer) {
        if (expectingFunctionName) {
            lexer.setNextIdentifierType(TokenType.FUNCTION_NAME);
        } else if (declarationIdentifiers && declarationKind == DeclarationKind.PARAMETER) {
            lexer.setNextIdentifierType(TokenType.VARIABLE);
        }
    }

    private void observe(Token token) {
        if (token.type() == TokenType.EOF) {
            return;
        }

        switch (token.type()) {
            case VAR_TYPE -> beginDeclaration(DeclarationKind.VARIABLE);
            case PARAM_TYPE -> beginDeclaration(DeclarationKind.PARAMETER);
            case FUNC_TYPE -> {
                beginDeclaration(DeclarationKind.NONE);
                expectingFunctionReturnType = true;
            }
            case REAL, INTEGER, STRING, BOOLEAN, CHARACTER, VOID -> observeType(token.type());
            case LEFT_BRACKET -> {
                if (declaredType != null && !declarationIdentifiers) {
                    arrayDeclaration = true;
                }
            }
            case COLON -> observeColon();
            case VARIABLE, FUNCTION_NAME, PROGRAM_NAME -> observeIdentifier(token);
            case COMMA -> { }
            case SEMICOLON -> endVariableOrParameterDeclaration();
            case END_FUNCTION, END_FUNCTIONS, END_PROGRAM ->
                    beginDeclaration(DeclarationKind.NONE);
            default -> {
                if (declarationIdentifiers && token.type() != TokenType.RIGHT_BRACKET
                        && token.type() != TokenType.LEFT_PAREN && token.type() != TokenType.RIGHT_PAREN
                        && token.type() != TokenType.INT_CONST) {
                    endVariableOrParameterDeclaration();
                }
            }
        }
    }

    private void beginDeclaration(DeclarationKind kind) {
        declarationKind = kind;
        declaredType = null;
        arrayDeclaration = false;
        declarationIdentifiers = false;
        expectingFunctionName = false;
        if (kind != DeclarationKind.NONE) {
            expectingFunctionReturnType = false;
        }
    }

    private void observeType(TokenType type) {
        if (expectingFunctionReturnType) {
            declaredType = typeCode(type, false);
        } else if (declarationKind != DeclarationKind.NONE && !declarationIdentifiers) {
            declaredType = typeCode(type, false);
        }
    }

    private void observeColon() {
        if (expectingFunctionReturnType && declaredType != null) {
            expectingFunctionName = true;
            return;
        }
        if (declarationKind != DeclarationKind.NONE && declaredType != null) {
            declarationIdentifiers = true;
        }
    }

    private void observeIdentifier(Token token) {
        if (token.type() == TokenType.PROGRAM_NAME) {
            symbolTable.register(token, null, token.line() + 1);
            return;
        }

        String type = null;
        if (token.type() == TokenType.FUNCTION_NAME && expectingFunctionName) {
            type = declaredType;
            expectingFunctionName = false;
            expectingFunctionReturnType = false;
            declaredType = null;
        } else if (declarationIdentifiers && declaredType != null) {
            type = arrayDeclaration ? arrayType(declaredType) : declaredType;
        }
        symbolTable.register(token, type, token.line() + 1);
    }

    private void endVariableOrParameterDeclaration() {
        declarationKind = DeclarationKind.NONE;
        declaredType = null;
        arrayDeclaration = false;
        declarationIdentifiers = false;
    }

    private String typeCode(TokenType type, boolean array) {
        String scalar = switch (type) {
            case REAL -> "FP";
            case INTEGER -> "IN";
            case STRING -> "ST";
            case CHARACTER -> "CH";
            case BOOLEAN -> "BL";
            case VOID -> "VD";
            default -> null;
        };
        return array && scalar != null ? arrayType(scalar) : scalar;
    }

    private String arrayType(String scalarType) {
        return switch (scalarType) {
            case "FP" -> "AF";
            case "IN" -> "AI";
            case "ST" -> "AS";
            case "CH" -> "AC";
            case "BL" -> "AB";
            default -> scalarType;
        };
    }

    private enum DeclarationKind { NONE, VARIABLE, PARAMETER }
}
