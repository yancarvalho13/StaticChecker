package org.example;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileFinder {
   
    public FileFinder(){
        
    }
    public void ReadFileLines(String filePath){
        try {
            var file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Arquivo nao encontrado");
                throw new FileNotFoundException();
            }
            var reader = new BufferedReader(new FileReader(file));
            System.out.println(readAllLines(reader)); 
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    
    private String readAllLines(BufferedReader reader){
        return reader.lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
