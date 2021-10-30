package com.github.kmpk;

public class IntIpBuilder {
    private final int[] firstOctet = new int[3];
    private final int[] secondOctet = new int[3];
    private final int[] thirdOctet = new int[3];
    private final int[] fourthOctet = new int[3];
    private byte currentOctet;
    private byte firstOctetIndex = 0;
    private byte secondOctetIndex = 0;
    private byte thirdOctetIndex = 0;
    private byte fourthOctetIndex = 0;

    public IntIpBuilder() {
        clear();
    }

    public IntIpBuilder addChar(char c) {
        if (c != '.') {
            int digit = parseDigit(c);
            switch (currentOctet) {
                case 0 -> {
                    checkOctetOverflow(firstOctetIndex);
                    firstOctet[firstOctetIndex] = digit;
                    firstOctetIndex++;
                }
                case 1 -> {
                    checkOctetOverflow(secondOctetIndex);
                    secondOctet[secondOctetIndex] = digit;
                    secondOctetIndex++;
                }
                case 2 -> {
                    checkOctetOverflow(thirdOctetIndex);
                    thirdOctet[thirdOctetIndex] = digit;
                    thirdOctetIndex++;
                }
                case 3 -> {
                    checkOctetOverflow(fourthOctetIndex);
                    fourthOctet[fourthOctetIndex] = digit;
                    fourthOctetIndex++;
                }
            }
        } else {
            if (currentOctet == 3) {
                throw new IllegalArgumentException("All four octets are already filled");
            }
            currentOctet++;
        }
        return this;
    }

    private void checkOctetOverflow(int octetIndex) {
        if (octetIndex == 3) {
            throw new IllegalArgumentException("Octet are already filled");
        }
    }

    public int buildIpAndClear() {
        if (isClear()) {
            throw new IllegalStateException("Can't build IP: builder is clear");
        }
        int ip = (parseOctet(firstOctet) << (3 * 8))
                + (parseOctet(secondOctet) << (2 * 8))
                + (parseOctet(thirdOctet) << (8))
                + (parseOctet(fourthOctet));
        clear();
        return ip;
    }

    public boolean isClear() {
        return currentOctet == 0 && firstOctetIndex == 0;
    }

    private int parseOctet(int[] octet) {
        while (octet[2] == -1) {
            shiftRight(octet);
        }
        return octet[0] * 100 + octet[1] * 10 + octet[2];
    }

    private void shiftRight(int[] octet) {
        octet[2] = octet[1];
        octet[1] = octet[0];
        octet[0] = 0;
    }

    private int parseDigit(char c) {
        return c - 48;
    }

    private void clear() {
        currentOctet = 0;
        for (int i = 0; i < 3; i++) {
            firstOctet[i] = -1;
            secondOctet[i] = -1;
            thirdOctet[i] = -1;
            fourthOctet[i] = -1;
        }
        firstOctetIndex = 0;
        secondOctetIndex = 0;
        thirdOctetIndex = 0;
        fourthOctetIndex = 0;
    }
}
