package com.github.kmpk;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class FileByteReader implements Runnable {
    private static final int BYTE_BUFFER_SIZE = 1024 * 1024; //1MB
    private final Path file;
    private final long toPos;
    private final long fromPos;
    private final IntConsumer byteConsumer;
    private final Consumer<Exception> exceptionHandler;

    private FileByteReader(Path file, long fromPos, long toPos, IntConsumer byteConsumer, Consumer<Exception> exceptionHandler) {
        this.file = file;
        this.fromPos = fromPos;
        this.toPos = toPos;
        this.byteConsumer = byteConsumer;
        this.exceptionHandler = exceptionHandler;
    }

    public static FileBytesReaderBuilder builder(Path path, IntConsumer consumer) {
        return new FileBytesReaderBuilder(path, consumer);
    }

    @Override
    public void run() {
        try {
            readCharsAndForwardToConsumer();
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }

    private void readCharsAndForwardToConsumer() throws IOException {
        try (FileChannel seekableByteChannel = new RandomAccessFile(file.toFile(), "r").getChannel()) {
            ByteBuffer bf = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
            seekableByteChannel.position(fromPos);
            read(bf, seekableByteChannel);
        }
    }

    private void read(ByteBuffer bf, SeekableByteChannel seekableByteChannel) throws IOException {
        long totalBytesToRead = toPos - fromPos;
        long bytesCounter = 0L;
        while (seekableByteChannel.position() < seekableByteChannel.size()
                && bytesCounter < totalBytesToRead
                && !Thread.currentThread().isInterrupted()) {
            seekableByteChannel.read(bf);
            bf.flip();
            long bytesLeftToRead = totalBytesToRead - bytesCounter;
            readByteBufferAndAcceptBytes(bf, bytesLeftToRead);
            bytesCounter += bf.position();
            bf.clear();
        }
    }

    private void readByteBufferAndAcceptBytes(ByteBuffer bf, long maxBytesToRead) {
        while (bf.position() < bf.limit()
                && bf.position() < maxBytesToRead) {
            byteConsumer.accept(bf.get());
        }
    }

    public static class FileBytesReaderBuilder {
        private final Path file;
        private final IntConsumer consumer;
        private long initPos;
        private long toPos;
        private Consumer<Exception> exceptionHandler;

        private FileBytesReaderBuilder(Path path, IntConsumer consumer) {
            this.file = path;
            toPos = file.toFile().length();
            this.consumer = consumer;
        }

        public FileBytesReaderBuilder setFromPos(long initPos) {
            this.initPos = initPos;
            return this;
        }

        public FileBytesReaderBuilder setToPos(long toPos) {
            this.toPos = toPos;
            return this;
        }

        public FileBytesReaderBuilder setExceptionHandler(Consumer<Exception> exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public FileByteReader createReader() {
            return new FileByteReader(file, initPos, toPos, consumer, exceptionHandler);
        }
    }
}
