package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CheckerControllerTest {
    @TempDir Path tempDir;

    @Test
    void acceptsBasePathAndAddsTheSourceExtension() {
        assertEquals(tempDir.resolve("source.262"), Main.sourcePath(tempDir.resolve("source").toString()));
        assertEquals(tempDir.resolve("source.262"), Main.sourcePath(tempDir.resolve("source.262").toString()));
    }

    @Test
    void analyzesCompleteFileAndResetsSymbolsForNextFile() throws Exception {
        Path file = tempDir.resolve("program.262");
        Files.writeString(file, "program app declarations varType integer: count; "
                + "varType integer[]: values[10] endDeclarations functions "
                + "funcType void: run(paramType integer: items[3], amount) "
                + "return endFunction endFunctions endProgram");
        CheckerController controller = new CheckerController();

        controller.StartAnalisys(file.toFile());

        SymbolTable table = controller.getSymbolTable();
        assertEquals("IN", table.find("count").getSymbolType());
        assertEquals("AI", table.find("values").getSymbolType());
        assertEquals(TokenType.FUNCTION_NAME, table.find("run").getAtom());
        assertEquals("VD", table.find("run").getSymbolType());
        assertEquals("AI", table.find("items").getSymbolType());
        assertEquals("IN", table.find("amount").getSymbolType());

        Path second = tempDir.resolve("second.262");
        Files.writeString(second, "program other declarations endDeclarations endProgram");
        controller.StartAnalisys(second.toFile());
        assertNull(controller.getSymbolTable().find("count"));
        assertNotNull(controller.getSymbolTable().find("other"));
    }

    @Test
    void missingInputFileProducesUsefulError() {
        CheckerController controller = new CheckerController();
        Exception ex = assertThrows(java.io.FileNotFoundException.class,
                () -> controller.StartAnalisys(tempDir.resolve("missing.262").toFile()));
        assertTrue(ex.getMessage().contains("missing.262"));
    }
}
