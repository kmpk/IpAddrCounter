package com.github.kmpk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileLinesReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadAllLines() throws IOException, InterruptedException {
        String content = "1\n2\n3\n";
        Path testFile = FileCreator.CreateAndPopulateFile(tempDir.resolve("testReadAllLines.tmp"), content);
        StringBuilder testAccumulator = new StringBuilder();
        FileLinesReader reader = FileLinesReader.builder(testFile, line -> testAccumulator.append(line).append('\n'))
                .createReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();
        readerThread.join();
        assertEquals(content, testAccumulator.toString());
    }

    @Test
    void testReadLinesSkipFirst() throws IOException, InterruptedException {
        String content = "1\n2\n3\n";
        Path testFile = FileCreator.CreateAndPopulateFile(tempDir.resolve("testReadLinesSkipFirst.tmp"), content);
        StringBuilder testAccumulator = new StringBuilder();
        FileLinesReader reader = FileLinesReader.builder(testFile, line -> testAccumulator.append(line).append('\n'))
                .setFromPos(2)
                .createReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();
        readerThread.join();
        assertEquals("2\n3\n", testAccumulator.toString());
    }

    @Test
    void testReadLinesSkipLast() throws IOException, InterruptedException {
        String content = "1\n2\n3\n";
        Path testFile = FileCreator.CreateAndPopulateFile(tempDir.resolve("testReadLinesSkipLast.tmp"), content);
        StringBuilder testAccumulator = new StringBuilder();
        FileLinesReader reader = FileLinesReader.builder(testFile, line -> testAccumulator.append(line).append('\n'))
                .setToPos(testFile.toFile().length() - 2)
                .createReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();
        readerThread.join();
        assertEquals("1\n2\n", testAccumulator.toString());
    }
}