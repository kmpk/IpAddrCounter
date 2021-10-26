package com.github.kmpk;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Pass file path as command-line argument, example: \"/path/to/file\"");
            System.exit(0);
        }

        try (IpFileReader reader = new IpFileReader(Path.of(args[0]), new IpConverter())) {
            IpCounter counter = new IpCounter();
            reader.getIpsIntStream().forEach(counter::add);
            System.out.println(counter.currentCount());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
