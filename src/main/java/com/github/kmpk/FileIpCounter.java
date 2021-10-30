package com.github.kmpk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class FileIpCounter {
    private static final int PROCESSING_TIMEOUT_MINUTES = 60;
    private static final long MIN_FILE_BLOCK_SIZE = 1024 * 1024 * 10L; //10 MB

    private final Path path;
    private final IntCounter counter = new IntCounter();

    private final int numOfProcessors = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executorService = Executors.newFixedThreadPool(numOfProcessors);
    private final AtomicReference<Exception> exception = new AtomicReference<>();
    private final Consumer<Exception> exceptionHandler = e -> {
        exception.compareAndSet(null, e);
        executorService.shutdownNow();
    };
    private final List<FileLinesReader> readers = new ArrayList<>();

    public FileIpCounter(Path file) {
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
        readers.addAll(createReadersList());
        for (FileLinesReader reader : readers) {
            executorService.execute(reader);
        }
        executorService.shutdown();
        if (!executorService.awaitTermination(PROCESSING_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            throw new TimeoutException("Counting timeout elapsed");
        }
        if (exception.get() != null) {
            throw exception.get();
        } else {
            return counter.count();
        }
    }

    private List<FileLinesReader> createReadersList() throws IOException {
        List<Long> fileBlocksRightEnds = divideFileAligningByLines(numOfProcessors);
        List<FileLinesReader> result = new ArrayList<>();
        long fileBlockLeftPos = 0;
        for (long nextRightPos : fileBlocksRightEnds) {
            FileLinesReader reader = createReader(fileBlockLeftPos, nextRightPos);
            result.add(reader);
            fileBlockLeftPos = nextRightPos;
        }
        return result;
    }

    private List<Long> divideFileAligningByLines(int chunks) throws IOException {
        long fileLength = path.toFile().length();
        if (chunks < 2) {
            return List.of(fileLength);
        }
        List<Long> result = new ArrayList<>();
        long approximateBlockSize = Math.max(fileLength / chunks, MIN_FILE_BLOCK_SIZE);
        long nextPosition = approximateBlockSize;
        try (RandomAccessFile r = new RandomAccessFile(path.toFile(), "r")) {
            r.seek(nextPosition);
            while (r.getFilePointer() < r.length()) {
                char currentChar = (char) r.readByte();
                while (r.getFilePointer() < fileLength && currentChar != '\n') {
                    r.skipBytes(1);
                    currentChar = (char) r.readByte();
                }
                result.add(r.getFilePointer());
                nextPosition = Math.min(r.getFilePointer() + approximateBlockSize, fileLength);
                r.seek(nextPosition);
            }
        }
        result.add(fileLength);
        return result;
    }

    private FileLinesReader createReader(long leftBound, long rightBound) {
        return FileLinesReader.builder(path, getCharSequenceConsumer())
                .setFromPos(leftBound)
                .setToPos(rightBound)
                .setExceptionHandler(exceptionHandler)
                .createReader();
    }

    private Consumer<CharSequence> getCharSequenceConsumer() {
        IpConverter converter = new IpConverter();
        return line -> counter.accept(converter.convertIpv4ToInt(line));
    }
}
