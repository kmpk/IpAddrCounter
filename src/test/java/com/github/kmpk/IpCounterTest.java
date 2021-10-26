package com.github.kmpk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpCounterTest {
    private IpCounter counter;

    @BeforeEach
    void createNewCounter() {
        counter = new IpCounter();
    }

    @Test
    void testZeroCountIfNotAdded() {
        assertEquals(0, counter.currentCount());
    }

    @Test
    void testAddedCountNoDuplicates() {
        IntStream.range(0, 1000).forEach(counter::add);
        assertEquals(1000, counter.currentCount());
        IntStream.range(-1000, -1).forEach(counter::add);
        assertEquals(1999, counter.currentCount());
    }

    @Test
    void testAddedCountWithDuplicates() {
        IntStream.range(-10, 10).forEach(counter::add);
        assertEquals(20, counter.currentCount());
        IntStream.range(-15, 15).forEach(counter::add);
        assertEquals(30, counter.currentCount());
        IntStream.range(-1000, 16).forEach(counter::add);
        assertEquals(1016, counter.currentCount());
    }
}