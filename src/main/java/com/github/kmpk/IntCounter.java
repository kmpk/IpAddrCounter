package com.github.kmpk;

import java.util.BitSet;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntConsumer;

public class IntCounter implements IntConsumer {
    private final BitSet positive = new BitSet(Integer.MAX_VALUE);
    private final BitSet negative = new BitSet(Integer.MAX_VALUE);
    private final Collection<IntBuffer> buffers = ConcurrentHashMap.newKeySet();
    private final ThreadLocal<IntBuffer> threadLocalIpBuffer = ThreadLocal.withInitial(() -> {
        IntBuffer buffer = new IntBuffer(this);
        buffers.add(buffer);
        return buffer;
    });

    private boolean isMinIntegerSet = false;
    private boolean isMaxIntegerSet = false;

    public void accept(int i) {
        IntBuffer intBuffer = threadLocalIpBuffer.get();
        intBuffer.accept(i);
    }

    public long count() {
        synchronized (this) {
            buffers.forEach(IntBuffer::flush);
            return (long) positive.cardinality() + negative.cardinality() + (isMinIntegerSet ? 1 : 0) + (isMaxIntegerSet ? 1 : 0);
        }
    }

    private void add(int[] ints, int rightBoundExclusive) {
        synchronized (this) {
            for (int i = 0; i < rightBoundExclusive; i++) {
                add(ints[i]);
            }
        }
    }

    private void add(int i) {
        if (i < 0) {
            if (i != Integer.MIN_VALUE) {
                this.negative.set(-i);
            } else {
                isMinIntegerSet = true;
            }
        } else {
            if (i != Integer.MAX_VALUE) {
                this.positive.set(i);
            } else {
                isMaxIntegerSet = true;
            }
        }
    }

    private static class IntBuffer {
        public static final int IP_BUFFER_SIZE = 100000;

        private final int[] buffer;
        private final IntCounter counter;
        private int pos = 0;

        IntBuffer(IntCounter counter) {
            this.buffer = new int[IP_BUFFER_SIZE];
            this.counter = counter;
        }

        public void accept(int i) {
            synchronized (this) {
                if (pos == buffer.length) {
                    flush();
                }
                buffer[pos] = i;
                pos++;
            }
        }

        void flush() {
            synchronized (this) {
                if (pos > 0) {
                    counter.add(buffer, pos);
                    pos = 0;
                }
            }
        }
    }
}
