package org.example;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private static final int MAX_ATOM_LENGTH = 30;

    private final List<CharStatus> fileChars;
    private int position;
    private TokenType nextIdentifierType;
    private TokenType previousType;

    public Lexer(List<CharStatus> fileChars) {
        this.fileChars = fileChars.stream()
                .map(c -> new CharStatus(Character.toUpperCase(c.token()), c.line(), c.column()))
                .toList();
    }

    public void setNextIdentifierType(TokenType type) {
        if (type != TokenType.VARIABLE && type != TokenType.FUNCTION_NAME
                && type != TokenType.PROGRAM_NAME) {
            throw new IllegalArgumentException("Tipo de identificador invalido: " + type);
        }
        nextIdentifierType = type;
    }

    public List<Token> Tokenize() {
        position = 0;
        previousType = null;
        nextIdentifierType = null;
        List<Token> tokens = new ArrayList<>();
        Token token;
        do {
            token = nextToken();
            if (token.type() != TokenType.EOF) {
                tokens.add(token);
            }
        } while (token.type() != TokenType.EOF);
        return List.copyOf(tokens);
    }

    public Token nextToken() {
        while (true) {
            skipIgnored();
            if (IsFileEnd()) {
                CharStatus eof = fileChars.isEmpty()
                        ? new CharStatus('\0', 0, 0)
                        : fileChars.get(fileChars.size() - 1);
                return new Token(TokenType.EOF, "", 0, eof.line(), eof.column() + 1);
            }

            char current = Peek();
            Token token;
            if (IsLetter(current) || current == '_') {
                token = ReadIdentifier();
            } else if (IsNumber(current)) {
                token = ReadNumber();
            } else if (current == '"') {
                token = ReadString();
            } else if (current == '\'') {
                token = ReadCharacter();
            } else {
                token = ReadSymbol();
                if (token == null) {
                    continue;
                }
            }
            previousType = token.type();
            return token;
        }
    }

    private Token ReadSymbol() {
        CharStatus start = fileChars.get(position);
        int distance = distanceToNextValid();
        String two = "" + Peek() + charAt(position + distance);
        TokenType type = TokenType.fromLexeme(two);
        if (type != null) {
            Advance();
            for (int i = 1; i < distance; i++) {
                Advance();
            }
            Advance();
            return token(type, two, 2, start);
        }

        String one = String.valueOf(Peek());
        type = TokenType.fromLexeme(one);
        if (type == null) {
            Advance();
            return null;
        }
        Advance();
        return token(type, one, 1, start);
    }

    private Token ReadCharacter() {
        CharStatus start = fileChars.get(position);
        StringBuilder lexeme = new StringBuilder();
        lexeme.append(Advance());
        if (IsFileEnd() || !IsLetter(Peek())) {
            throw error("CharConst deve conter uma unica letra", start);
        }
        lexeme.append(Advance());
        if (IsFileEnd() || Peek() != '\'') {
            throw error("Aspas de fechamento ausente em CharConst", start);
        }
        lexeme.append(Advance());
        return token(TokenType.CHAR_CONST, lexeme.toString(), lexeme.length(), start);
    }

    private Token ReadString() {
        CharStatus start = fileChars.get(position);
        StringBuilder stored = new StringBuilder();
        int originalLength = 0;
        append(stored, Advance());
        originalLength++;
        int contentLength = 0;
        while (!IsFileEnd() && Peek() != '"') {
            char current = Peek();
            if (isInvalid(current)) {
                Advance();
                continue;
            }
            if (!IsLetter(current) && !IsNumber(current) && current != ' '
                    && current != '$' && current != '_' && current != '.') {
                throw error("Caractere invalido na string", start);
            }
            append(stored, Advance());
            originalLength++;
            contentLength++;
        }
        if (IsFileEnd()) {
            throw error("String nao foi finalizada", start);
        }
        if (contentLength == 0) {
            throw error("String vazia", start);
        }
        char closingQuote = Advance();
        originalLength++;
        if (stored.length() < MAX_ATOM_LENGTH) {
            stored.append(closingQuote);
        } else {
            stored.setCharAt(MAX_ATOM_LENGTH - 1, closingQuote);
        }
        //Gambiarra para preservar a aspa final em caso de truncagem
        return token(TokenType.STRING_CONST, stored.toString(), originalLength, start);
    }

    private Token ReadNumber() {
        CharStatus start = fileChars.get(position);
        StringBuilder lexeme = new StringBuilder();
        TokenType type = TokenType.INT_CONST;
        int originalLength = readDigits(lexeme);
        skipInvalidInNumber();

        if (Peek() == '.') {
            type = TokenType.REAL_CONST;
            append(lexeme, Advance());
            originalLength++;
            skipInvalidInNumber();
            if (!IsNumber(Peek())) {
                throw error("Parte fracionaria invalida", start);
            }
            originalLength += readDigits(lexeme);
            skipInvalidInNumber();
            if (Peek() == 'E') {
                append(lexeme, Advance());
                originalLength++;
                if (Peek() == '+' || Peek() == '-') {
                    append(lexeme, Advance());
                    originalLength++;
                }
                skipInvalidInNumber();
                if (!IsNumber(Peek())) {
                    throw error("Expoente invalido", start);
                }
                originalLength += readDigits(lexeme);
            }
        } else if (Peek() == 'E') {
            throw error("Expoente so e permitido em real", start);
        }
        return token(type, truncateNumber(lexeme.toString(), type), originalLength, start);
    }

    private int readDigits(StringBuilder lexeme) {
        int count = 0;
        while (IsNumber(Peek())) {
            lexeme.append(Advance());
            count++;
        }
        return count;
    }

    private String truncateNumber(String number, TokenType type) {
        if (number.length() <= MAX_ATOM_LENGTH) {
            return number;
        }
        String stored = number.substring(0, MAX_ATOM_LENGTH);
        if (type == TokenType.INT_CONST) {
            return stored;
        }
        int decimal = stored.indexOf('.');
        if (decimal < 0 || decimal == stored.length() - 1) {
            int integerDigits = Math.min(number.indexOf('.'), MAX_ATOM_LENGTH - 2);
            return number.substring(0, integerDigits) + ".0";
        }
        int exponent = stored.indexOf('E');
        if (exponent >= 0) {
            String exponentPart = stored.substring(exponent + 1);
            if (exponentPart.isEmpty() || exponentPart.equals("+") || exponentPart.equals("-")) {
                stored = stored.substring(0, exponent);
            }
        }
        return stored;
    }

    private void skipInvalidInNumber() {
        while (!IsFileEnd() && isInvalid(Peek())) {
            Advance();
        }
    }

    private Token ReadIdentifier() {
        CharStatus start = fileChars.get(position);
        StringBuilder lexeme = new StringBuilder();
        int originalLength = 0;
        TokenType requestedType = Peek() == '_' ? TokenType.VARIABLE : nextIdentifierType != null
                ? nextIdentifierType
                : previousType == TokenType.PROGRAM ? TokenType.PROGRAM_NAME : TokenType.VARIABLE;
        while (!IsFileEnd()) {
            char current = Peek();
            boolean accepted = IsLetter(current) || IsNumber(current)
                    || (requestedType == TokenType.VARIABLE && current == '_');
            if (accepted) {
                append(lexeme, Advance());
                originalLength++;
            } else if (isInvalid(current)) {
                Advance();
            } else {
                break;
            }
        }

        String word = lexeme.toString();
        TokenType type = TokenType.fromLexeme(word);
        if (type == null) {
            //fazendo isso pra poder passar varType e palavras reservadas... me pergunto de Dennis Rithcie gostava dessa parada kkkk
            /*for(char c : word.toCharArray()){
                if(c >= 'A' && c <= 'Z'){
                    throw new IllegalArgumentException("Identificador invalido: " + word);
                }
            } Resolvi metendo um uppercase no documento todo, essa bendita linguagem pede uppercase em tudo */
            if (nextIdentifierType != null) {
                type = requestedType;
                nextIdentifierType = null;
            } else if (previousType == TokenType.PROGRAM
                    && requestedType == TokenType.PROGRAM_NAME) {
                type = TokenType.PROGRAM_NAME;
            } else {
                type = TokenType.VARIABLE;
            }
        }
        return token(type, word, originalLength, start);
    }

    private void skipIgnored() {
        while (!IsFileEnd()) {
            char current = Peek();
            if (Character.isWhitespace(current)) {
                Advance();
            } else if (current == '/' && PeekNext() == '/') {
                SkipLineComment();
            } else if (current == '/' && PeekNext() == '*') {
                SkipBlockComment();
            } else if (isInvalid(current)) {
                Advance();
            } else {
                return;
            }
        }
    }

    private boolean isInvalid(char c) {
        if (c == '.' || c == '=' || c == '!') {
            return false;
        }
        return !Character.isWhitespace(c) && !IsLetter(c) && !IsNumber(c) && c != '_'
                && c != '"' && c != '\'' && TokenType.fromLexeme(String.valueOf(c)) == null;
    }

    private Token token(TokenType type, String lexeme, int originalLength, CharStatus start) {
        return new Token(type, lexeme, originalLength, start.line(), start.column());
    }

    private void append(StringBuilder text, char c) {
        //Regra dos 30 caracteres
        if (text.length() < MAX_ATOM_LENGTH) {
            text.append(c);
        }
    }

    private void SkipLineComment() {
        Advance();
        Advance();
        while (!IsFileEnd() && Peek() != '\n') {
            Advance();
        }
    }

    private void SkipBlockComment() {
        Advance();
        Advance();
        while (!IsFileEnd()) {
            if (Peek() == '*' && PeekNext() == '/') {
                Advance();
                Advance();
                return;
            }
            Advance();
        }
        //Pelo roteiro, comentario nao fechado, continua ate o fim do arquivo
    }

    private char Peek() {
        return IsFileEnd() ? '\0' : fileChars.get(position).token();
    }

    private char PeekNext() {
        return position + 1 >= fileChars.size() ? '\0' : fileChars.get(position + 1).token();
    }

    private int distanceToNextValid() {
        int next = position + 1;
        while (next < fileChars.size() && isInvalid(fileChars.get(next).token())) {
            next++;
        }
        return next - position;
    }

    private char charAt(int index) {
        return index >= fileChars.size() ? '\0' : fileChars.get(index).token();
    }

    private char Advance() {
        return fileChars.get(position++).token();
    }

    private boolean IsFileEnd() {
        return position >= fileChars.size();
    }

    private boolean IsLetter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private boolean IsNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private IllegalArgumentException error(String message, CharStatus start) {
        return new IllegalArgumentException(message + " linha: " + (start.line() + 1)
                + ", coluna: " + (start.column() + 1));
    }
}
