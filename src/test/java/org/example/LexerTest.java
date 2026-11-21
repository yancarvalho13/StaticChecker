package org.example;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {
    private Lexer lexer(String source) {
        List<CharStatus> chars = new ArrayList<>();
        int line = 0;
        int column = 0;
        for (char c : source.toCharArray()) {
            chars.add(new CharStatus(c, line, column));
            if (c == '\n') {
                line++;
                column = 0;
            } else {
                column++;
            }
        }
        return new Lexer(chars);
    }

    @Test
    void recognizesReservedAtomsAndOfficialCodesWithoutCaseSensitivity() {
        List<Token> tokens = lexer("program declarations endDeclarations "
                + "funcType integer paramType endIf endProgram").Tokenize();

        assertEquals(List.of(TokenType.PROGRAM, TokenType.DECLARATIONS,
                TokenType.END_DECLARATIONS, TokenType.FUNC_TYPE, TokenType.INTEGER,
                TokenType.PARAM_TYPE, TokenType.END_IF, TokenType.END_PROGRAM),
                tokens.stream().map(Token::type).toList());
        assertEquals("A19", tokens.getFirst().atomCode());
        assertEquals("A16", tokens.get(4).atomCode());
        assertEquals("ENDPROGRAM", tokens.getLast().lexeme());
    }

    @Test
    void recognizesAllOperatorsAndDelimitersWithLongestMatch() {
        List<Token> tokens = lexer("<= < >= > == != # := + - * / % ; : , ( ) [ ] { } ?")
                .Tokenize();
        assertEquals(List.of(TokenType.LESS_EQUAL, TokenType.LESS_THAN,
                TokenType.GREATER_EQUAL, TokenType.GREATER_THAN, TokenType.EQUAL,
                TokenType.NOT_EQUAL, TokenType.HASH, TokenType.ASSIGN, TokenType.PLUS,
                TokenType.MINUS, TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO,
                TokenType.SEMICOLON, TokenType.COLON, TokenType.COMMA,
                TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN, TokenType.LEFT_BRACKET,
                TokenType.RIGHT_BRACKET, TokenType.LEFT_BRACE, TokenType.RIGHT_BRACE,
                TokenType.QUESTION_MARK), tokens.stream().map(Token::type).toList());
        assertEquals("B20", tokens.getFirst().atomCode());
        assertEquals("B18", tokens.get(5).atomCode());
        assertEquals("B18", tokens.get(6).atomCode());
    }

    @Test
    void classifiesProgramNameVariableAndExternallyHintedFunctionName() {
        Lexer lexer = lexer("program Projeto1 valor");
        List<Token> tokens = lexer.Tokenize();
        assertEquals(TokenType.PROGRAM_NAME, tokens.get(1).type());
        assertEquals("C03", tokens.get(1).atomCode());
        assertEquals(TokenType.VARIABLE, tokens.get(2).type());

        Lexer functionLexer = lexer("somafunc");
        functionLexer.setNextIdentifierType(TokenType.FUNCTION_NAME);
        Token function = functionLexer.nextToken();
        assertEquals(TokenType.FUNCTION_NAME, function.type());
        assertEquals("C02", function.atomCode());
    }

    @Test
    void enforcesIdentifierStartAndContinuationPatterns() {
        List<Token> tokens = lexer("program App var_name _local function2").Tokenize();
        assertEquals(TokenType.PROGRAM_NAME, tokens.get(1).type());
        assertEquals(TokenType.VARIABLE, tokens.get(2).type());
        assertEquals("VAR_NAME", tokens.get(2).lexeme());
        assertEquals(TokenType.VARIABLE, tokens.get(3).type());
        assertEquals("_LOCAL", tokens.get(3).lexeme());
        assertEquals("FUNCTION2", tokens.get(4).lexeme());
    }

    @Test
    void filtersInvalidCharactersAndCanonicalizesIdentifiers() {
        Token token = lexer("va@riavel").nextToken();
        assertEquals("VARIAVEL", token.lexeme());
        assertEquals(8, token.originalLength());
        assertEquals("VARIAVEL", lexer("VaRiAvEl").nextToken().lexeme());
    }

    @Test
    void truncatesLongAtomsButPreservesOriginalLengthAndContinuesReading() {
        String word = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        Token token = lexer(word + " next").nextToken();
        assertEquals(35, token.originalLength());
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ1234", token.lexeme());
        assertEquals(30, token.lexeme().length());
        assertEquals("NEXT", lexer(word + " next").Tokenize().get(1).lexeme());
    }

    @Test
    void readsIntegerRealAndExponentForms() {
        List<Token> tokens = lexer("123 123.45 123.45E10 123.45E+10 123.45E-10").Tokenize();
        assertEquals(List.of(TokenType.INT_CONST, TokenType.REAL_CONST, TokenType.REAL_CONST,
                TokenType.REAL_CONST, TokenType.REAL_CONST),
                tokens.stream().map(Token::type).toList());
        assertEquals("123.45E+10", tokens.get(3).lexeme());
    }

    @Test
    void rejectsMalformedNumbers() {
        for (String source : List.of("123.", "123.E10", "123.45E", "123.45E+", "123E10")) {
            assertThrows(IllegalArgumentException.class, () -> lexer(source).nextToken(), source);
        }
    }

    @Test
    void keepsTruncatedRealLexemesValidWhenTheLimitHitsNumericPunctuation() {
        String numberEndingAtDot = "12345678901234567890123456789.123";
        Token trailingDot = lexer(numberEndingAtDot).nextToken();
        Token trailingExponent = lexer("1.234567890123456789012345678E+123").nextToken();
        Token trailingSign = lexer("1.23456789012345678901234567E+123").nextToken();

        assertEquals(TokenType.REAL_CONST, trailingDot.type());
        assertTrue(trailingDot.lexeme().matches("[0-9]+\\.[0-9]+"));
        assertEquals(numberEndingAtDot.length(), trailingDot.originalLength());
        assertTrue(trailingExponent.lexeme().matches("[0-9]+\\.[0-9]+(E[+-]?[0-9]+)?"));
        assertTrue(trailingSign.lexeme().matches("[0-9]+\\.[0-9]+(E[+-]?[0-9]+)?"));
        assertTrue(trailingDot.lexeme().length() <= 30);
        assertTrue(trailingExponent.lexeme().length() <= 30);
        assertTrue(trailingSign.lexeme().length() <= 30);
    }

    @Test
    void validatesAndTruncatesStringsIncludingQuotes() {
        Token token = lexer("\"" + "A".repeat(29) + "\"").nextToken();
        assertEquals(TokenType.STRING_CONST, token.type());
        assertEquals(31, token.originalLength());
        assertEquals(30, token.lexeme().length());
        assertEquals('"', token.lexeme().charAt(29));
        assertEquals("\"AB\"", lexer("\"A@B\"").nextToken().lexeme());
        assertThrows(IllegalArgumentException.class, () -> lexer("\"\"").nextToken());
        assertThrows(IllegalArgumentException.class, () -> lexer("\"ABC").nextToken());
        assertThrows(IllegalArgumentException.class, () -> lexer("\"A;B\"").nextToken());
    }

    @Test
    void acceptsOnlySingleLetterCharacterConstants() {
        assertEquals("'A'", lexer("'a'").nextToken().lexeme());
        for (String source : List.of("'AB'", "''", "'1'")) {
            assertThrows(IllegalArgumentException.class, () -> lexer(source).nextToken(), source);
        }
    }

    @Test
    void skipsLineAndBlockCommentsAndKeepsLineLocations() {
        List<Token> tokens = lexer("one // comentário\ntwo /* a\n b */ three /* unfinished")
                .Tokenize();
        assertEquals(List.of("ONE", "TWO", "THREE"), tokens.stream().map(Token::lexeme).toList());
        assertEquals(List.of(0, 1, 2), tokens.stream().map(Token::line).toList());
    }

    @Test
    void nextTokenReturnsOneAtomThenEof() {
        Lexer lexer = lexer("<= value");
        assertEquals(TokenType.LESS_EQUAL, lexer.nextToken().type());
        assertEquals(TokenType.VARIABLE, lexer.nextToken().type());
        assertEquals(TokenType.EOF, lexer.nextToken().type());
        assertEquals(TokenType.EOF, lexer.nextToken().type());
    }
}
