package com.botoni.vsr.ratelimit;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public final class ClientNetwork {

    private static final int IPV6_NETWORK_BYTES = 8;
    private static final String IPV6_NETWORK = "%s/64";

    private ClientNetwork() {
    }

    public static String of(String address) {
        if (!isIpv6(address)) {
            return address;
        }
        return prefix(address);
    }

    private static boolean isIpv6(String address) {
        return address.contains(":");
    }

    private static String prefix(String address) {
        byte[] bytes = literal(address).getAddress();
        Arrays.fill(bytes, IPV6_NETWORK_BYTES, bytes.length, (byte) 0);
        return String.format(IPV6_NETWORK, literal(bytes).getHostAddress());
    }

    private static InetAddress literal(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    private static InetAddress literal(byte[] bytes) {
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
