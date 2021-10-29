package com.github.kmpk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

class IpConverterTest {
    IpConverter instance = new IpConverter();

    @Test
    void testConvertIpv4ToInt() {
        Assertions.assertEquals(0, instance.convertIpv4ToInt("0.0.0.0"));
        Assertions.assertEquals(0x80000000, instance.convertIpv4ToInt("128.0.0.0"));
        Assertions.assertEquals(0xFFFFFFFF, instance.convertIpv4ToInt("255.255.255.255"));
        Assertions.assertEquals(1, instance.convertIpv4ToInt("0.0.0.1"));
        Assertions.assertEquals(0x7FFFFFFF, instance.convertIpv4ToInt("127.255.255.255"));
    }

    @Test
    void testRandomConvertConsistency() {
        IntStream.generate(() -> ThreadLocalRandom.current().nextInt())
                .limit(100000)
                .forEach(i -> {
                    CharSequence converted = IpIntToCharSequenceConverter.convertIntToIpv4(i);
                    int reconverted = instance.convertIpv4ToInt(converted);
                    Assertions.assertEquals(i, reconverted, "Int " + i + " was unexpectedly converted back to " + reconverted + " from " + converted);
                });
    }
}