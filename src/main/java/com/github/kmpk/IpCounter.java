package com.github.kmpk;

import java.util.BitSet;

public class IpCounter {
    private final BitSet positive = new BitSet();
    private final BitSet negative = new BitSet();

    public void add(int ip) {
        if (ip < 0) {
            this.negative.set(-ip);
        } else {
            this.positive.set(ip);
        }
    }

    public long currentCount() {
        return (long) positive.cardinality() + negative.cardinality();
    }
}
