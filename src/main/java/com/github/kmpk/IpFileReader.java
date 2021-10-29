package com.github.kmpk;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class IpFileReader implements Runnable {
    private final Path file;
    private final long initPos;
    private final long toPos;
    private final int byteBuffer;
    private final IntConsumer consumer;
    private final Consumer<Exception> exceptionHandler;
    private final IpConverter converter = new IpConverter();

    public IpFileReader(Path file, long initPos, long toPos, int bufferSize, IntConsumer consumer, Consumer<Exception> exceptionHandler) {
        this.file = file;
        this.initPos = initPos;
        this.toPos = toPos;
        this.byteBuffer = bufferSize;
        this.consumer = consumer;
        this.exceptionHandler = exceptionHandler;
    }

    private void readIpsAndAddToCounter() throws IOException {
        ByteBuffer bf = ByteBuffer.allocate(byteBuffer);
        try (SeekableByteChannel seekableByteChannel = Files.newByteChannel(file, StandardOpenOption.READ)) {
            seekableByteChannel.position(initPos);
            findNextLineAndSetPosition(seekableByteChannel, bf);
            process(bf, seekableByteChannel);
        }
    }

    private void process(ByteBuffer bf, SeekableByteChannel seekableByteChannel) throws IOException {
        StringBuilder current = new StringBuilder();
        long currentPosition = seekableByteChannel.position();
        long fileSize = seekableByteChannel.size();
        while (currentPosition < fileSize && currentPosition < toPos) {
            seekableByteChannel.read(bf);
            bf.flip();
            while (bf.position() < bf.limit()) {
                char c = (char) bf.get();
                if (c == '\n') {
                    consumer.accept(converter.convertIpv4ToInt(current));
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            bf.clear();
            currentPosition = seekableByteChannel.position();
        }
        if (currentPosition == fileSize && !current.isEmpty()) {
            consumer.accept(converter.convertIpv4ToInt(current));
        }
    }

    private void findNextLineAndSetPosition(SeekableByteChannel seekableByteChannel, ByteBuffer bf) throws IOException {
        if (initPos != 0) {
            seekableByteChannel.read(bf);
            bf.flip();
            int c = 1;
            while (bf.position() < bf.limit()) {
                char c1 = (char) bf.get();
                if (c1 == '\n') break;
                c++;
            }
            seekableByteChannel.position(initPos + c);
            bf.clear();
        }
    }

    @Override
    public void run() {
        try {
            readIpsAndAddToCounter();
        } catch (Exception e) {
            exceptionHandler.accept(e);
        }
    }
}
