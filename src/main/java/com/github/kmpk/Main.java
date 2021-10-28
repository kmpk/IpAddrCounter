package com.github.kmpk;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Main {
    public static final int BYTE_BUFFER_SIZE = 1024 * 1024; //1MB
    public static final int BLOCK_OVERLAP_BYTES = 20;
    public static final int IP_BUFFER_SIZE = 100000;

    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.out.println("Pass file path as command-line argument, example: \"/path/to/file\"");
            System.exit(0);
        }
        IpCounter counter = new IpCounter();
        Path file = Path.of(args[0]);

        long fileLength = file.toFile().length();
        long block = calculateBlockSize(fileLength);

        ExecutorService service = ForkJoinPool.commonPool();
        AtomicReference<Exception> ex = new AtomicReference<>();

        long leftBound = 0;
        while (leftBound < fileLength) {
            long rightBound = Math.min(leftBound + block + BLOCK_OVERLAP_BYTES, fileLength);
            service.execute(new IpFileReader(file, leftBound, rightBound, BYTE_BUFFER_SIZE, new IpBuffer(IP_BUFFER_SIZE, counter), e -> {
                ex.compareAndSet(null, e);
                service.shutdownNow();
            }));
            leftBound = rightBound == fileLength ? rightBound : rightBound - BLOCK_OVERLAP_BYTES;
        }

        service.shutdown();
        service.awaitTermination(1, TimeUnit.HOURS);
        if (ex.get() != null) {
            ex.get().printStackTrace();
        } else {
            System.out.println(counter.currentCount());
        }
    }

    private static long calculateBlockSize(long fileLength) {
        int maxThreads = Runtime.getRuntime().availableProcessors();
        return Math.max(fileLength / maxThreads, BYTE_BUFFER_SIZE);
    }
}
