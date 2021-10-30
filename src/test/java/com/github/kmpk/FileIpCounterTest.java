package com.github.kmpk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

class FileIpCounterTest {

    @TempDir
    Path tempDir;

    @Test
    void testCountEmptyFile() throws Exception {
        Path tempFile = TestUtil.CreateAndPopulateFile(tempDir.resolve("testCountEmptyFile.tmp"), TestUtil.generateIpStream(0, 0));
        FileIpCounter instance = new FileIpCounter(tempFile);
        Assertions.assertEquals(0, instance.count());
    }

    @Test
    void testCountNoDuplicates() throws Exception {
        Path tempFile = TestUtil.CreateAndPopulateFile(tempDir.resolve("testCountNoDuplicates.tmp"), TestUtil.generateIpStream(10000, 0));
        FileIpCounter instance = new FileIpCounter(tempFile);
        Assertions.assertEquals(10000, instance.count());
    }

    @Test
    void testCountDuplicates() throws Exception {
        Path tempFile = TestUtil.CreateAndPopulateFile(tempDir.resolve("testCountDuplicates.tmp"), TestUtil.generateIpStream(10000, 5000));
        FileIpCounter instance = new FileIpCounter(tempFile);
        Assertions.assertEquals(10000, instance.count());
    }

    @Test
    void testCountBigFileNoDuplicates() throws Exception {
        Path tempFile = TestUtil.CreateAndPopulateFile(tempDir.resolve("testCountBigFileNoDuplicates.tmp"), TestUtil.generateIpStream(10_000_000, 0));
        FileIpCounter instance = new FileIpCounter(tempFile);
        Assertions.assertEquals(10_000_000, instance.count());
    }

    @Test
    void testCountBigFileDuplicates() throws Exception {
        Path tempFile = TestUtil.CreateAndPopulateFile(tempDir.resolve("testCountBigFileDuplicates.tmp"), TestUtil.generateIpStream(10_000_000, 10_000_000));
        FileIpCounter instance = new FileIpCounter(tempFile);
        Assertions.assertEquals(10_000_000, instance.count());
    }
}