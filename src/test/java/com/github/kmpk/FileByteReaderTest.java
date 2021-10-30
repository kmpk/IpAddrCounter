package com.github.kmpk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileByteReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testReadAll() throws IOException, InterruptedException {
        String content = "1\n2\n3\n";
        Path testFile = TestUtil.CreateAndPopulateFile(tempDir.resolve("testReadAll.tmp"), content);
        StringBuilder testAccumulator = new StringBuilder();
        FileByteReader reader = FileByteReader.builder(testFile, i -> testAccumulator.append((char) i))
                .createReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();
        readerThread.join();
        assertEquals(content, testAccumulator.toString());
    }

    @Test
    void testReadAllSkipFirst() throws IOException, InterruptedException {
        String content = "1\n2\n3\n";
        Path testFile = TestUtil.CreateAndPopulateFile(tempDir.resolve("testReadAllSkipFirst.tmp"), content);
        StringBuilder testAccumulator = new StringBuilder();
        FileByteReader reader = FileByteReader.builder(testFile, i -> testAccumulator.append((char) i))
                .setFromPos(2)
                .createReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();
        readerThread.join();
        assertEquals("2\n3\n", testAccumulator.toString());
    }

    @Test
    void testReadAllSkipLast() throws IOException, InterruptedException {
        String content = "1\n2\n3\n";
        Path testFile = TestUtil.CreateAndPopulateFile(tempDir.resolve("testReadAllSkipLast.tmp"), content);
        StringBuilder testAccumulator = new StringBuilder();
        FileByteReader reader = FileByteReader.builder(testFile, i -> testAccumulator.append((char) i))
                .setToPos(testFile.toFile().length() - 2)
                .createReader();
        Thread readerThread = new Thread(reader);
        readerThread.start();
        readerThread.join();
        assertEquals("1\n2\n", testAccumulator.toString());
    }
}