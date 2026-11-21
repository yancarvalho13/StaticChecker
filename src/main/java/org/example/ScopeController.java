package org.example;

public class ScopeController {
    private final SymbolTable symbolTable;
    private DeclarationKind declarationKind = DeclarationKind.NONE;
    private String declaredType;
    private boolean arrayDeclaration;
    private boolean declarationIdentifiers;
    private boolean expectingFunctionName;
    private boolean expectingFunctionReturnType;
    private String lastParameterLexeme;
    private boolean parameterVectorPending;

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
        lastParameterLexeme = null;
        parameterVectorPending = false;
    }

    public Token nextToken(Lexer lexer) {
        prepareLexer(lexer);
        Token token = lexer.nextToken();
        return observe(token);
    }

    private void prepareLexer(Lexer lexer) {
        if (expectingFunctionName) {
            lexer.setNextIdentifierType(TokenType.FUNCTION_NAME);
        } else if (declarationIdentifiers && declarationKind == DeclarationKind.PARAMETER) {
            lexer.setNextIdentifierType(TokenType.VARIABLE);
        }
    }

    private Token observe(Token token) {
        if (token.type() == TokenType.EOF) {
            return token;
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
                if (declarationIdentifiers && declarationKind == DeclarationKind.PARAMETER) {
                    parameterVectorPending = lastParameterLexeme != null;
                } else if (declaredType != null && !declarationIdentifiers) {
                    arrayDeclaration = true;
                }
            }
            case INT_CONST -> {
                if (parameterVectorPending) {
                    parameterVectorPending = true;
                }
            }
            case RIGHT_BRACKET -> {
                if (parameterVectorPending && lastParameterLexeme != null) {
                    SymbolEntry parameter = symbolTable.find(lastParameterLexeme);
                    if (parameter != null) {
                        parameter.setSymbolType(arrayType(declaredType));
                    }
                    parameterVectorPending = false;
                }
            }
            case COLON -> observeColon();
            case VARIABLE, FUNCTION_NAME, PROGRAM_NAME -> { return observeIdentifier(token); }
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
        return token;
    }

    private void beginDeclaration(DeclarationKind kind) {
        declarationKind = kind;
        declaredType = null;
        arrayDeclaration = false;
        declarationIdentifiers = false;
        expectingFunctionName = false;
        lastParameterLexeme = null;
        parameterVectorPending = false;
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

    private Token observeIdentifier(Token token) {
        if (token.type() == TokenType.PROGRAM_NAME) {
            SymbolEntry entry = symbolTable.register(token, null, token.line() + 1);
            return token.withSymbolIndex(entry.getIndex());
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
        SymbolEntry entry = symbolTable.register(token, type, token.line() + 1);
        if (declarationIdentifiers && declarationKind == DeclarationKind.PARAMETER
                && token.type() == TokenType.VARIABLE) {
            lastParameterLexeme = token.lexeme();
        }
        return token.withSymbolIndex(entry.getIndex());
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
