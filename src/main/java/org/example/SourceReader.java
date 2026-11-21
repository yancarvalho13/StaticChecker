package org.example;

import com.sun.jdi.InvalidTypeException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SourceReader {
    private List<CharStatus> fileChars = new ArrayList<>();
    public SourceReader(){
    }
    public List<CharStatus> GetTokenList(){
        return  fileChars;
    }
    public void GenerateFileCharsMetadata(String filePath) throws IOException, InvalidTypeException {
            var file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Arquivo nao encontrado");
                throw new FileNotFoundException();
            }
            /*if(!file.getAbsolutePath().endsWith(".262")){
                throw new InvalidTypeException("Informe um arquivo .262");
            }*/
            try (var reader = new BufferedReader(new FileReader(file))){
                GenerateTokens(reader);
            }
        }

    private void GenerateTokens(BufferedReader reader) throws IOException {
        int readerChar;
        int line = 0;
        int column = 0;

        while((readerChar = reader.read()) != -1){
            var realChar = (char) readerChar;
            fileChars.add(new CharStatus(realChar,line,column));
            if(realChar == '\n'){
                line++;
                column = 0;
            } else {
                column ++;
            }
        }
    }
}

