package com.github.kmpk;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestUtil {
    private TestUtil() {
    }

    public static Path CreateAndPopulateFile(Path file, CharSequence content) throws IOException {
        Files.writeString(file, content);
        return file;
    }

    public static Path CreateAndPopulateFile(Path file, Stream<CharSequence> stream) throws IOException {
        Iterator<CharSequence> iterator = stream.iterator();
        try (final FileWriter writer = new FileWriter(file.toFile(), true);) {
            while (iterator.hasNext()) {
                writer.append(iterator.next()).append('\n');
            }
        }
        return file;
    }

    public static CharSequence convertIntToIpv4(int ipv4Int) {
        return String.valueOf(ipv4Int >>> 24) + '.' +
                (ipv4Int >>> 16 & 0x000000ff) + '.' +
                (ipv4Int >>> 8 & 0x000000ff) + '.' +
                (ipv4Int & 0x000000ff);
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
        return IntStream.concat(uniquesStream, duplicatesStream).mapToObj(TestUtil::convertIntToIpv4);
    }
}
