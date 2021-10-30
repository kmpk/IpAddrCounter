package com.github.kmpk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

public class FileLinesReader implements Runnable {
    private static final int BYTE_BUFFER_SIZE = 1024 * 1024; //1MB
    private final Path file;
    private final long toPos;
    private final long fromPos;
    private final Consumer<CharSequence> lineConsumer;
    private final Consumer<Exception> exceptionHandler;

    private FileLinesReader(Path file, long fromPos, long toPos, Consumer<CharSequence> lineConsumer, Consumer<Exception> exceptionHandler) {
        this.file = file;
        this.fromPos = fromPos;
        this.toPos = toPos;
        this.lineConsumer = lineConsumer;
        this.exceptionHandler = exceptionHandler;
    }

    public static FileLinesReaderBuilder builder(Path path, Consumer<CharSequence> consumer) {
        return new FileLinesReaderBuilder(path, consumer);
    }

    @Override
    public void run() {
        try {
            readLinesAndForwardToConsumer();
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }

    private void readLinesAndForwardToConsumer() throws IOException {
        ByteBuffer bf = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
        try (SeekableByteChannel seekableByteChannel = Files.newByteChannel(file, StandardOpenOption.READ)) {
            seekableByteChannel.position(fromPos);
            readLines(bf, seekableByteChannel);
        }
    }

    private void readLines(ByteBuffer bf, SeekableByteChannel seekableByteChannel) throws IOException {
        StringBuilder currentLine = new StringBuilder();
        long totalBytesToRead = toPos - fromPos;
        long bytesCounter = 0L;
        while (seekableByteChannel.position() < seekableByteChannel.size()
                && bytesCounter < totalBytesToRead
                && !Thread.currentThread().isInterrupted()) {
            seekableByteChannel.read(bf);
            bf.flip();
            long bytesLeftToRead = totalBytesToRead - bytesCounter;
            readByteBufferAndAcceptLines(bf, currentLine, bytesLeftToRead);
            bytesCounter += bf.position();
            bf.clear();
        }
    }

    private void readByteBufferAndAcceptLines(ByteBuffer bf, StringBuilder currentLine, long bytesLeftToRead) {
        while (bf.position() < bf.limit()
                && bf.position() < bytesLeftToRead) {
            char c = (char) bf.get();
            if (c == '\n') {
                lineConsumer.accept(currentLine);
                currentLine.setLength(0);
            } else {
                currentLine.append(c);
            }
        }
    }

    public static class FileLinesReaderBuilder {
        private final Path file;
        private final Consumer<CharSequence> consumer;
        private long initPos;
        private long toPos;
        private Consumer<Exception> exceptionHandler;

        private FileLinesReaderBuilder(Path path, Consumer<CharSequence> consumer) {
            this.file = path;
            toPos = file.toFile().length();
            this.consumer = consumer;
        }

        public FileLinesReaderBuilder setFromPos(long initPos) {
            this.initPos = initPos;
            return this;
        }

        public FileLinesReaderBuilder setToPos(long toPos) {
            this.toPos = toPos;
            return this;
        }

        public FileLinesReaderBuilder setExceptionHandler(Consumer<Exception> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public FileLinesReader createReader() {
            return new FileLinesReader(file, initPos, toPos, consumer, exceptionHandler);
        }
    }
}
