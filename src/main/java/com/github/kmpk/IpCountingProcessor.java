package com.github.kmpk;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class IpCountingProcessor {
    public static final int BYTE_BUFFER_SIZE = 1024 * 1024; //1MB
    public static final int BLOCK_OVERLAP_BYTES = 20;
    public static final int PROCESSING_TIMEOUT_MINUTES = 60;

    private final Path path;
    private final IntCounter counter = new IntCounter();
    private final ExecutorService executorService = ForkJoinPool.commonPool();
    private final AtomicReference<Exception> exception = new AtomicReference<>();
    Consumer<Exception> exceptionHandler = e -> {
        exception.compareAndSet(null, e);
        executorService.shutdownNow();
    };

    public IpCountingProcessor(Path file) {
        this.path = file;
        checkFile();
    }

    private void checkFile() {
        File file = path.toFile();
        if (!file.isFile()) {
            throw new IllegalArgumentException(file.getAbsolutePath() + " is not a file");
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("Can't read " + file.getAbsolutePath());
        }
    }

    public long count() throws Exception {
        long block = calculateBlockSize();
        List<IpFileReader> readers = createReadersList(block);
        for (IpFileReader reader : readers) {
            executorService.execute(reader);
        }
        executorService.shutdown();
        executorService.awaitTermination(PROCESSING_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (exception.get() != null) {
            throw exception.get();
        } else {
            return counter.count();
        }
    }

    private long calculateBlockSize() {
        long fileLength = path.toFile().length();
        return Math.max(fileLength / Runtime.getRuntime().availableProcessors(), BYTE_BUFFER_SIZE);
    }

    private List<IpFileReader> createReadersList(long block) {
        List<IpFileReader> result = new ArrayList<>();
        long fileLength = path.toFile().length();
        long leftBound = 0;
        while (leftBound < fileLength) {
            long rightBound = Math.min(leftBound + block + BLOCK_OVERLAP_BYTES, fileLength);
            result.add(new IpFileReader(path, leftBound, rightBound, BYTE_BUFFER_SIZE, counter, exceptionHandler));
            leftBound = rightBound == fileLength ? rightBound : rightBound - BLOCK_OVERLAP_BYTES;
        }
        return result;
    }
}
