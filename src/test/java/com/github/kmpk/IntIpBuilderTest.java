package com.github.kmpk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

class IntIpBuilderTest {
    private IntIpBuilder instance;

    @BeforeEach
    void setUp() {
        instance = new IntIpBuilder();
    }

    @Test
    void testIsNewAfterBuild() {
        Assertions.assertTrue(instance.isClear());
        instance.addChar('1').addChar('2').addChar('7').addChar('.')
                .addChar('0').addChar('.')
                .addChar('0').addChar('.')
                .addChar('1');
        instance.buildIpAndClear();
        Assertions.assertTrue(instance.isClear());
    }

    @Test
    void testExceptionThrownIfCallBuildOnNew() {
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> instance.buildIpAndClear());
        Assertions.assertTrue(exception.getMessage().contains("builder is clear"));
    }

    @Test
    void testExceptionThrownOnOctetOverflow() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> instance.addChar('1').addChar('2').addChar('3').addChar('4'));
        Assertions.assertTrue(exception.getMessage().contains("Octet are already filled"));
    }

    @Test
    void testExceptionThrownOnAddingFifthOctet() {
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> instance
                        .addChar('1').addChar('2').addChar('7')
                        .addChar('.').addChar('0')
                        .addChar('.').addChar('0')
                        .addChar('.').addChar('1')
                        .addChar('.').addChar('0'));
        Assertions.assertTrue(exception.getMessage().contains("All four octets are already filled"));
    }

    @Test
    void testBuildIps() {
        int ip = 0;
        CharSequence ipv4 = TestUtil.convertIntToIpv4(ip);
        ipv4.chars()
                .forEach(i -> instance.addChar((char) i));
        Assertions.assertEquals(ip, instance.buildIpAndClear());

        ip = Integer.MAX_VALUE;
        ipv4 = TestUtil.convertIntToIpv4(ip);
        ipv4.chars()
                .forEach(i -> instance.addChar((char) i));
        Assertions.assertEquals(ip, instance.buildIpAndClear());

        ip = Integer.MIN_VALUE;
        ipv4 = TestUtil.convertIntToIpv4(ip);
        ipv4.chars()
                .forEach(i -> instance.addChar((char) i));
        Assertions.assertEquals(ip, instance.buildIpAndClear());
    }

    @Test
    void testBuildRandomIps() {
        IntStream.generate(() -> ThreadLocalRandom.current().nextInt())
                .limit(1_000_000)
                .forEach(i -> {
                    TestUtil.convertIntToIpv4(i)
                            .chars()
                            .forEach(c -> instance.addChar((char) c));
                    Assertions.assertEquals(i, instance.buildIpAndClear());
                });
    }
}