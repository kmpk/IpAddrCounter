package com.github.kmpk;

import java.nio.file.Path;

public class Main {
    private static Path path;

    public static void main(String[] args) {
        if (args.length != 1) {
            printHelpAndExit();
        }
        path = Path.of(args[0]);
        try {
            FileIpCounter fileIpCounter = new FileIpCounter(path);
            System.out.println(fileIpCounter.count());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printHelpAndExit() {
        System.out.println("""
                Pass file path as command-line argument, example: java -jar IpAddrCounter.jar "/path/to your/file"
                """);
        System.exit(0);
    }
}