package org.example;

import com.sun.jdi.InvalidTypeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {
        try {
            var controller = new CheckerController();
            controller.StartAnalisys(new File("teste.262"));
        } catch (InvalidTypeException e) {
            throw new RuntimeException(e);
        }
    }
}