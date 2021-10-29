package com.github.kmpk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        int[] ips;
        ips = IntStream.range(0, 1000).toArray();
        counter.add(ips, ips.length);
        assertEquals(1000, counter.currentCount());
        ips = IntStream.range(-1000, -1).toArray();
        counter.add(ips, ips.length);
        assertEquals(1999, counter.currentCount());
    }

    @Test
    void testAddedCountWithDuplicates() {
        int[] ips;
        ips = IntStream.range(-10, 10).toArray();
        counter.add(ips, ips.length);
        assertEquals(20, counter.currentCount());
        ips = IntStream.range(-15, 15).toArray();
        counter.add(ips, ips.length);
        assertEquals(30, counter.currentCount());
        ips = IntStream.range(-1000, 16).toArray();
        counter.add(ips, ips.length);
        assertEquals(1016, counter.currentCount());
    }

    @Test
    void testAddParallel() {
        Set<Integer> checkSet = ConcurrentHashMap.newKeySet();
        Stream.generate(() -> IntStream.generate(() -> ThreadLocalRandom.current().nextInt()).limit(10000).toArray())
                .parallel()
                .limit(1000)
                .forEach(arr -> {
                    for (int i : arr) {
                        checkSet.add(i);
                    }
                    counter.add(arr, arr.length);
                });
        assertEquals(checkSet.size(), counter.currentCount());
    }

    @Disabled("Too long, run manually")
    @Test
    void testAddAllIntegersParallel() {
        List<IntStream> intStreams = new ArrayList<>();
        long leftBound = Integer.MIN_VALUE;
        int block = 10000000;
        while (leftBound < Integer.MAX_VALUE) {
            long rightBound = Math.min(leftBound + block, Integer.MAX_VALUE);
            intStreams.add(IntStream.range((int) leftBound, (int) rightBound));
            leftBound = rightBound;
        }
        intStreams.stream().parallel().forEach(is -> {
            int[] ints = is.toArray();
            counter.add(ints, ints.length);
        });
        assertEquals(4294967295L, counter.currentCount());
    }
}