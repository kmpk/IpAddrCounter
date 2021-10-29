package com.github.kmpk;

public class IpIntToCharSequenceConverter {
    public static CharSequence convertIntToIpv4(int ipv4Int) {
        return String.valueOf(ipv4Int >>> 24) + '.' +
                (ipv4Int >>> 16 & 0x000000ff) + '.' +
                (ipv4Int >>> 8 & 0x000000ff) + '.' +
                (ipv4Int & 0x000000ff);
    }
}
