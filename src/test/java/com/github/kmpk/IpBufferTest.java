package com.github.kmpk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IpBufferTest {
    private final int buffer_capacity = 100000;
    private IpCounter counter;
    private IpBuffer buffer;

    @BeforeEach
    void createNewCounterAndBuffer() {
        counter = new IpCounter();
        buffer = new IpBuffer(buffer_capacity, counter);
    }

    @Test
    void flushNoneIfEmpty() {
        buffer.flush();
        assertEquals(0, counter.currentCount());
    }

    @Test
    void testAcceptIntegers() {
        IntStream.range(-1000, 1000).forEach(i -> buffer.accept(i));
        buffer.flush();
        assertEquals(2000, counter.currentCount());
    }

    @Disabled("Too long, run manually")
    @Test
    void testAcceptAllIntegers() {
        IntStream.range(Integer.MIN_VALUE, Integer.MAX_VALUE).forEach(buffer::accept);
        buffer.flush();
        assertEquals(4294967295L, counter.currentCount());
    }
}