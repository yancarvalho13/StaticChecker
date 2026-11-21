package org.example;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum TokenType {
    BOOLEAN("BOOLEAN", "A01"), BREAK("BREAK", "A02"), CHARACTER("CHARACTER", "A03"),
    DECLARATIONS("DECLARATIONS", "A04"), ELSE("ELSE", "A05"),
    END_DECLARATIONS("ENDDECLARATIONS", "A06"), END_FUNCTION("ENDFUNCTION", "A07"),
    END_FUNCTIONS("ENDFUNCTIONS", "A08"), END_IF("ENDIF", "A09"),
    END_PROGRAM("ENDPROGRAM", "A10"), END_WHILE("ENDWHILE", "A11"),
    FALSE("FALSE", "A12"), FUNCTIONS("FUNCTIONS", "A13"), FUNC_TYPE("FUNCTYPE", "A14"),
    IF("IF", "A15"), INTEGER("INTEGER", "A16"), PARAM_TYPE("PARAMTYPE", "A17"),
    PRINT("PRINT", "A18"), PROGRAM("PROGRAM", "A19"), REAL("REAL", "A20"),
    RETURN("RETURN", "A21"), STRING("STRING", "A22"), TRUE("TRUE", "A23"),
    VAR_TYPE("VARTYPE", "A24"), VOID("VOID", "A25"), WHILE("WHILE", "A26"),

    SEMICOLON(";", "B01"), COMMA(",", "B02"), COLON(":", "B03"), ASSIGN(":=", "B04"),
    QUESTION_MARK("?", "B05"), LEFT_PAREN("(", "B06"), RIGHT_PAREN(")", "B07"),
    LEFT_BRACKET("[", "B08"), RIGHT_BRACKET("]", "B09"), LEFT_BRACE("{", "B10"),
    RIGHT_BRACE("}", "B11"), PLUS("+", "B12"), MINUS("-", "B13"),
    MULTIPLY("*", "B14"), DIVIDE("/", "B15"), MODULO("%", "B16"),
    EQUAL("==", "B17"), NOT_EQUAL("!=", "B18"), HASH("#", "B18"),
    LESS_THAN("<", "B19"), LESS_EQUAL("<=", "B20"), GREATER_THAN(">", "B21"),
    GREATER_EQUAL(">=", "B22"),

    VARIABLE(null, "C01"), FUNCTION_NAME(null, "C02"), PROGRAM_NAME(null, "C03"),
    STRING_CONST(null, "C04"), CHAR_CONST(null, "C05"), INT_CONST(null, "C06"),
    REAL_CONST(null, "C07"),

    EOF(null, null), UNKNOWN(null, null);

    private final String lexeme;
    private final String code;
    private static final Map<String, TokenType> LOOKUP = new HashMap<>();

    static {
        for (TokenType type : values()) {
            if (type.lexeme != null) {
                LOOKUP.put(type.lexeme, type);
            }
        }
        LOOKUP.put("ENDIF", END_IF);
    }

    TokenType(String lexeme, String code) {
        this.lexeme = lexeme;
        this.code = code;
    }

    public static TokenType fromLexeme(String lexeme) {
        return lexeme == null ? null : LOOKUP.get(lexeme.toUpperCase(Locale.ROOT));
    }

    public String getLexeme() {
        return lexeme;
    }

    public String getCode() {
        return code;
    }
}
