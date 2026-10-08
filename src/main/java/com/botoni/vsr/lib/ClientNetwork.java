package com.botoni.vsr.lib;

import com.botoni.vsr.exception.custom.RequestException;
import com.botoni.vsr.exception.enums.problem.RequestProblem;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;

public final class ClientNetwork {

    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");
    private static final String IPV6_SEPARATOR = ":";
    private static final int IPV6_NETWORK_BYTES = 8;
    private static final String IPV6_NETWORK = "%s/64";

    private ClientNetwork() {
    }

    public static InetAddress address(String address) {
        return parse(literal(address));
    }

    public static String of(String address) {
        return network(address(address));
    }

    private static String literal(String address) {
        if (!isLiteral(address)) {
            throw new RequestException(RequestProblem.INVALID_ADDRESS);
        }
        return address;
    }

    private static InetAddress parse(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException exception) {
            throw new RequestException(RequestProblem.INVALID_ADDRESS);
        }
    }

    private static String network(InetAddress address) {
        if (address instanceof Inet4Address) {
            return address.getHostAddress();
        }
        return prefix(address);
    }

    private static String prefix(InetAddress address) {
        byte[] bytes = address.getAddress();
        Arrays.fill(bytes, IPV6_NETWORK_BYTES, bytes.length, (byte) 0);
        return String.format(IPV6_NETWORK, fromBytes(bytes).getHostAddress());
    }

    private static InetAddress fromBytes(byte[] bytes) {
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException exception) {
            throw new RequestException(RequestProblem.INVALID_ADDRESS);
        }
    }

    private static boolean isLiteral(String address) {
        return address != null && (IPV4.matcher(address).matches() || address.contains(IPV6_SEPARATOR));
    }
}
