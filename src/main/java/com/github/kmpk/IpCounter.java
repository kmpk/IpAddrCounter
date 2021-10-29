package com.github.kmpk;

import java.util.BitSet;

public class IpCounter {
    private final BitSet positive = new BitSet();
    private final BitSet negative = new BitSet();
    private boolean isMinIntegerSet = false;

    private void add(int ip) {
        if (ip < 0) {
            if (ip != Integer.MIN_VALUE) {
                this.negative.set(-ip);
            } else {
                isMinIntegerSet = true;
            }
        } else {
            this.positive.set(ip);
        }
    }

    public void add(int[] ints, int rightBoundExclusive) {
        synchronized (this) {
            for (int i = 0; i < rightBoundExclusive; i++) {
                add(ints[i]);
            }
        }
    }

    public long currentCount() {
        synchronized (this) {
            return (long) positive.cardinality() + negative.cardinality() + (isMinIntegerSet ? 1 : 0);
        }
    }
}
