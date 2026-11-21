
package org.example;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum TokenType {

    PROGRAM("program"),
    DECLARATIONS("declarations"),
    END_DECLARATIONS("endDeclarations"),
    FUNCTIONS("functions"),
    END_FUNCTIONS("endFunctions"),
    END_PROGRAM("endProgram"),

    VAR_TYPE("varType"),
    FUNC_TYPE("funcType"),
    PARAM_TYPE("paramType"),
    END_FUNCTION("endFunction"),

    REAL("real"),
    INTEGER("integer"),
    STRING("string"),
    BOOLEAN("boolean"),
    CHARACTER("character"),
    VOID("void"),

    IF("if"),
    END_IF("endIf"),
    ELSE("else"),
    WHILE("while"),
    END_WHILE("endWhile"),
    RETURN("return"),
    BREAK("break"),
    PRINT("print"),

    TRUE("true"),
    FALSE("false"),

    PROGRAM_NAME(null),
    VARIABLE(null),
    FUNCTION_NAME(null),

    INT_CONST(null),
    REAL_CONST(null),
    STRING_CONST(null),
    CHAR_CONST(null),

    LESS_EQUAL("<="),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    GREATER_EQUAL(">="),
    EQUAL("=="),
    NOT_EQUAL("!="),
    HASH("#"),

    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MODULO("%"),

    ASSIGN(":="),

    SEMICOLON(";"),
    COLON(":"),
    COMMA(","),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    LEFT_BRACKET("["),
    RIGHT_BRACKET("]"),
    LEFT_BRACE("{"),
    RIGHT_BRACE("}"),
    QUESTION_MARK("?"),

    EOF(null),
    UNKNOWN(null);

    private final String lexeme;

    private static final Map<String, TokenType> LOOKUP = new HashMap<>();

    static {
        for (TokenType type : values()) {
            if (type.lexeme != null) {
                LOOKUP.put(type.lexeme, type);
            }
        }
    }

    TokenType(String lexeme) {
        this.lexeme = lexeme == null
                ? null
                : lexeme.toUpperCase(Locale.ROOT);
    }

    public static TokenType fromLexeme(String lexeme) {
        if (lexeme == null) {
            return null;
        }

        return LOOKUP.get(lexeme.toUpperCase(Locale.ROOT));
    }

    public String getLexeme() {
        return lexeme;
    }
}
