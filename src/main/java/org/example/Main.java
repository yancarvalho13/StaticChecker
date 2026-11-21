package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Uso: StaticChecker <arquivo-sem-extensao-.262>");
            return;
        }

        Path source = sourcePath(args[0]);
        new CheckerController().StartAnalisys(source.toFile());
    }

    static Path sourcePath(String input) {
        Path supplied = Path.of(input);
        String fileName = supplied.getFileName().toString();
        return fileName.toLowerCase().endsWith(".262")
                ? supplied
                : supplied.resolveSibling(fileName + ".262");
    }
}
