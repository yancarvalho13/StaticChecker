package org.example;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final List<CharStatus> fileChars;
    private final List<Token> tokens = new ArrayList<>();
    private int position = 0;

    public Lexer(List<CharStatus> fileChars) {
        this.fileChars = fileChars;
    }

    public List<Token> Tokenize(){
        tokens.clear();
        position = 0;

        while(!IsFileEnd()){
            char current = Peek();

            if(Character.isWhitespace(current)){
                Advance();
            } else if (current == '/' && PeekNext() == '/') {
               SkipLineComment();
            } else if (current == '/' && PeekNext() == '*') {
                SkipBlockComment();
            } else if (IsLetter(current) || current == '_') {
                ReadIdentifier();
            } else if (IsNumber(current)) {
                ReadNumber();
            } else if (current == '"') {
                ReadString();
            } else if (current == '\'') {
                ReadCharacter();
            } else {
                ReadSymbol();
            }
        }
        return List.copyOf(tokens);
    }
    private void ReadSymbol() {
        CharStatus start = fileChars.get(position);

        String two = "" + Peek() + PeekNext();
        TokenType type = TokenType.fromLexeme(two);

        if (type != null) {
            Advance();
            Advance();
            AddToken(type, two, start);
            return;
        }

        String one = String.valueOf(Advance());
        type = TokenType.fromLexeme(one);

        if (type == null) {
            throw error("Símbolo inválido: " + one, start);
        }

        AddToken(type, one, start);
    }
    private void ReadCharacter(){
        CharStatus start = fileChars.get(position);
        StringBuilder lexeme = new StringBuilder();

        lexeme.append(Advance());

        if (!IsLetter(Peek())){
            throw error("Caractere invalido", start);
        }
        lexeme.append(Advance());
        AddToken(TokenType.CHAR_CONST,
                lexeme.toString(), start);
    }
    private void ReadString(){
        CharStatus start = fileChars.get(position);
        StringBuilder lexeme = new StringBuilder();

        AppendLimited(lexeme, Advance());

        int contentLength = 0;
        while(!IsFileEnd() && Peek() != '"'){
            char current = Peek();

            if(!IsLetter(current) &&
                !IsNumber(current) &&
                current != ' ' &&
                current != '$' &&
                current != '_' &&
                current != '.'){
                throw error("Caractere invalido na string: ", start);
            }
            AppendLimited(lexeme, Advance());
            contentLength++;
        }
        if (IsFileEnd()){
            throw error("String nao foi finalizada", start);
        }
        if (contentLength == 0){
            throw error("String vazia", start);
        }

        AppendLimited(lexeme, Advance());

        //Gambiarra para preservar a aspa final em caso de truncagem
        if (lexeme.length() == 30 &&
        lexeme.charAt(29) != '"'){
            lexeme.setCharAt(29, '"');
        }
        AddToken(TokenType.STRING_CONST,
                lexeme.toString(), start);
    }

    private void ReadNumber(){
        var start = fileChars.get(position);
        StringBuilder lexeme = new StringBuilder();
        TokenType type = TokenType.INT_CONST;

        while(IsNumber(Peek())) {
            AppendLimited(lexeme, Advance());
        }

        if (Peek() == '.' && IsNumber(PeekNext())){
            type = TokenType.REAL_CONST;
            AppendLimited(lexeme, Advance());

            while(IsNumber(Peek())){
                AppendLimited(lexeme, Advance());
            }

            if(Peek() == 'e'){
                AppendLimited(lexeme, Advance());
                if(Peek() == '+' || Peek() == '-'){
                    AppendLimited(lexeme, Advance());
                }
                if(!IsNumber(Peek())){
                    throw error("Expoente invalido", start);
                }
                while (IsNumber(Peek())){
                    AppendLimited(lexeme, Advance());
                }
            }
        }

        AddToken(type, lexeme.toString(), start);
    }
    private void ReadIdentifier(){
        var start = fileChars.get(position);
        StringBuilder lexeme = new StringBuilder();

        while (!IsFileEnd()){
            char current = Peek();
            if(!IsLetter(current) && !IsNumber(current) && current != '_'){
                break;
            }
            AppendLimited(lexeme, Advance());
        }

        String word = lexeme.toString();
        TokenType type = TokenType.fromLexeme(word);

        if(type == null) {
            //fazendo isso pra poder passar varType e palavras reservadas... me pergunto de Dennis Rithcie gostava dessa parada kkkk
            for(char c : word.toCharArray()){
                if(c >= 'A' && c <= 'Z'){
                    throw new IllegalArgumentException("Identificador invalido: " + word);
                }
            }
            type = TokenType.VARIABLE;
        }

        AddToken(type, word, start);

    }

    private void AddToken(TokenType type, String lexeme, CharStatus start){
        tokens.add(new Token(
                type,
                lexeme,
                start.line(),
                start.column()
        ));
    }

    //Regra dos 30 caracteres
    private void AppendLimited(StringBuilder text, char c){
        if ( text.length() < 30){
            text.append(c);
        }
    }
    private void SkipLineComment() {
        Advance();
        Advance();

        while (!IsFileEnd() && Peek() != '\n'){
            Advance();
        }
    }

    private void SkipBlockComment(){
        Advance();
        Advance();

        while (!IsFileEnd()){
            if(Peek() == '*' && PeekNext() == '/'){
                Advance();
                Advance();
                return;
            }
            Advance();
        }
        //Pelo roteiro, comentario nao fechado, continua ate o fim do arquivo
    }
    private char Peek(){
        if (IsFileEnd()){
            return '\0';
        }
        return  fileChars.get(position).token();
    }

    private char PeekNext(){
        if(position + 1 >= fileChars.size()){
            return '\0';
        }

        return fileChars.get(position + 1).token();
    }
    private char Advance(){
        return fileChars.get(position++).token();
    }
    private boolean IsFileEnd(){
        return position >= fileChars.size();
    }
    private boolean IsLetter(char c){
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z');
    }
    private boolean IsNumber(char c){
        return c >= '0' && c <= '9';
    }
    private IllegalArgumentException error(
            String message, CharStatus start){

        return new IllegalArgumentException(message + " linha: " + start.line()
        + ", coluna: " + start.column());
    }
}
