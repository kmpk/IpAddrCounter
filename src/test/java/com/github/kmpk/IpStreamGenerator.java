package com.github.kmpk;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IpStreamGenerator {
    private IpStreamGenerator() {

    }

    public static Stream<CharSequence> generateIpStream(int unique, int duplicates) {
        IntStream uniquesStream = IntStream.range(Integer.MIN_VALUE, Integer.MIN_VALUE + unique);
        IntStream duplicatesStream;
        if (duplicates <= unique) {
            duplicatesStream = IntStream.range(Integer.MIN_VALUE, Integer.MIN_VALUE + duplicates);
        } else {
            AtomicInteger atomic = new AtomicInteger(Integer.MIN_VALUE);
            duplicatesStream = IntStream.generate(() -> {
                int result = atomic.getAndIncrement();
                if (atomic.get() >= Integer.MIN_VALUE + unique) {
                    atomic.set(Integer.MIN_VALUE);
                }
                return result;
            }).limit(duplicates);
        }
        return IntStream.concat(uniquesStream, duplicatesStream).mapToObj(IpIntToCharSequenceConverter::convertIntToIpv4);
    }
}
