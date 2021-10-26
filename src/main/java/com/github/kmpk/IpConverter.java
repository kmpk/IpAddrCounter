package com.github.kmpk;

public class IpConverter {
    public int convertIpv4ToInt(CharSequence ipv4) {
        StringBuilder builder = new StringBuilder();
        int result = 0;
        int counter = 0;
        for (int i = 0; i < ipv4.length(); i++) {
            char c = ipv4.charAt(i);
            if (c != '.') {
                builder.append(c);
            } else {
                result += Integer.parseInt(builder.toString()) << ((3 - counter) * 8);
                counter++;
                builder.setLength(0);
            }
        }
        result += Integer.parseInt(builder.toString()) << ((3 - counter) * 8);
        builder.setLength(0);
        return result;
    }

    public CharSequence convertIntToIpv4(int ipv4Int) {
        return String.valueOf(ipv4Int >>> 24) + '.' +
                (ipv4Int >>> 16 & 0x000000ff) + '.' +
                (ipv4Int >>> 8 & 0x000000ff) + '.' +
                (ipv4Int & 0x000000ff);
    }
}
