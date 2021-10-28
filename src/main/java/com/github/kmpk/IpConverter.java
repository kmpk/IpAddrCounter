package com.github.kmpk;

public class IpConverter {
    private final StringBuilder builder = new StringBuilder();

    public int convertIpv4ToInt(CharSequence ipv4) {
        builder.setLength(0);
        int result = 0;
        int counter = 0;
        for (int i = 0; i < ipv4.length(); i++) {
            char c = ipv4.charAt(i);
            if (c != '.') {
                builder.append(c);
            } else {
                result += parseIpv4Octet(builder) << ((3 - counter) * 8);
                counter++;
                builder.setLength(0);
            }
        }
        result += parseIpv4Octet(builder) << ((3 - counter) * 8);
        return result;
    }

    private int parseIpv4Octet(CharSequence s) {
        return switch (s.length()) {
            case 3 -> parseDigit(s.charAt(0)) * 100 + parseDigit(s.charAt(1)) * 10 + parseDigit(s.charAt(2));
            case 2 -> parseDigit(s.charAt(0)) * 10 + parseDigit(s.charAt(1));
            case 1 -> parseDigit(s.charAt(0));
            default -> throw new RuntimeException("Ipv4 octet must contain 1-3 digits " + s);
        };
    }

    private int parseDigit(char c) {
        return c - 48;
    }

    public CharSequence convertIntToIpv4(int ipv4Int) {
        return String.valueOf(ipv4Int >>> 24) + '.' +
                (ipv4Int >>> 16 & 0x000000ff) + '.' +
                (ipv4Int >>> 8 & 0x000000ff) + '.' +
                (ipv4Int & 0x000000ff);
    }
}
