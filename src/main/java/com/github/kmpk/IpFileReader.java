package com.github.kmpk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IpFileReader implements AutoCloseable {
    private final IntStream stream;

    public IpFileReader(Path file, IpConverter ipConverter) throws IOException {
        Stream<String> lines = Files.lines(file, StandardCharsets.US_ASCII);
        this.stream = lines.parallel().mapToInt(ipConverter::convertIpv4ToInt);
    }

    public IntStream getIpsIntStream() {
        return stream;
    }

    @Override
    public void close() {
        stream.close();
    }
}
