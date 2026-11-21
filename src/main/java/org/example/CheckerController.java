package org.example;

import com.sun.jdi.InvalidTypeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CheckerController {
    private final SourceReader sourceReader;
    public CheckerController(){
       sourceReader = new SourceReader();
    }
    public void StartAnalisys(File file) throws InvalidTypeException, IOException {
        var path = file.getAbsolutePath();
        sourceReader.GenerateFileCharsMetadata(path);
        var fileChars = sourceReader.GetTokenList();
        var lexer = new Lexer(fileChars);
        var tokens = lexer.Tokenize();
        tokens.forEach(System.out::println);
    }
}
