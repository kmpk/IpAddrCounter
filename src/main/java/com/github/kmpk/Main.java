package com.github.kmpk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.stream.Stream;

public class Main {
    private final Path file;
    private final BitSet positive = new BitSet();
    private final BitSet negative = new BitSet();

    public Main(Path file) {
        this.file = file;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Pass file path as command-line argument, example: \"/path/to/file\"");
            System.exit(0);
        }

        Main instance = new Main(Path.of(args[0]));
        try {
            System.out.println(instance.countIps());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int countIps() throws IOException {
        try (Stream<String> lines = Files
                .lines(file, StandardCharsets.US_ASCII)) {
            lines
                    .mapToInt(this::parseIpv4)
                    .forEach(i -> {
                        if (i < 0) {
                            this.negative.set(-i);
                        } else {
                            this.positive.set(i);
                        }
                    });
            return positive.cardinality() + negative.cardinality();
        }
    }

    private int parseIpv4(CharSequence ipv4) {
        StringBuilder builder = new StringBuilder();
        int result = 0;
        int counter = 0;
        for (int i = 0; i < ipv4.length(); i++) {
            char c = ipv4.charAt(i);
            if (c != '.') {
                builder.append(c);
            } else {
                result += Integer.parseInt(builder.toString()) << ((3 - counter) * 8);
                counter++;
                builder.setLength(0);
            }
        }
        result += Integer.parseInt(builder.toString()) << ((3 - counter) * 8);
        builder.setLength(0);
        return result;
    }
}
