package com.github.kmpk;

import org.graalvm.nativeimage.ImageInfo;

import java.nio.file.Path;

public class Main {
    private static final String HELP_FORMAT = "Pass file path as command-line argument, example: %s \"/path/to your/file\"";

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
        String programCommand = "java -jar IpAddrCounter.jar";
        try {
            Class.forName("org.graalvm.nativeimage.ImageInfo");
            if (ImageInfo.inImageRuntimeCode()) {
                if (System.getProperty("os.name").contains("Windows")) {
                    programCommand = "IpAddrCounter.exe";
                } else {
                    programCommand = "IpAddrCounter";
                }
            }
        } catch (ClassNotFoundException ignore) {
        }
        System.out.printf(HELP_FORMAT, programCommand);
        System.exit(0);
    }
}