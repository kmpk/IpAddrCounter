package com.github.kmpk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IntCounterTest {
    public static final long All_INTEGERS_COUNT = 4294967296L;
    private IntCounter instance;

    @BeforeEach
    void createNewCounter() {
        instance = new IntCounter();
    }

    @Test
    void testZeroCountIfNotAdded() {
        assertEquals(0, instance.count());
    }

    @Test
    void testAddRandomParallel() {
        Set<Integer> checkSet = ConcurrentHashMap.newKeySet();
        IntStream.generate(() -> ThreadLocalRandom.current().nextInt())
                .limit(1_000_000)
                .parallel()
                .forEach(i -> {
                    checkSet.add(i);
                    instance.accept(i);
                });
        assertEquals(checkSet.size(), instance.count());
    }

    @Test
    void testAddRandomDuplicatesParallel() {
        Set<Integer> checkSet = ConcurrentHashMap.newKeySet();
        IntStream.generate(() -> ThreadLocalRandom.current().nextInt())
                .limit(1_000_000)
                .parallel()
                .forEach(checkSet::add);
        checkSet.stream().parallel().forEach(instance::accept);
        checkSet.stream().parallel().forEach(instance::accept);
        assertEquals(checkSet.size(), instance.count());
    }

    @Test()
    void testAddAllIntegersParallel() {
        IntStream.rangeClosed(Integer.MIN_VALUE, Integer.MAX_VALUE)
                .parallel()
                .forEach(i -> instance.accept(i));
        assertEquals(All_INTEGERS_COUNT, instance.count());
    }
}