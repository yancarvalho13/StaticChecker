package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class SourceReader {
    private List<CharStatus> fileChars = new ArrayList<>();
    public SourceReader(){
    }
    public List<CharStatus> GetTokenList(){
        return  fileChars;
    }
    public void GenerateFileCharsMetadata(String filePath) throws IOException {
            fileChars.clear();
            var file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("Arquivo nao encontrado: " + filePath);
            }
            /*if(!file.getAbsolutePath().endsWith(".262")){
                throw new InvalidTypeException("Informe um arquivo .262");
            }*/
            try (var reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(file.toPath()), StandardCharsets.US_ASCII))) {
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

