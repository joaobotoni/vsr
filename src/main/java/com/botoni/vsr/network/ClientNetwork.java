package com.botoni.vsr.network;

import com.botoni.vsr.exception.custom.RequestException;
import com.botoni.vsr.exception.enums.problem.RequestProblem;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class ClientNetwork {

    private ClientNetwork() {
    }

    public static InetAddress address(String address) {
        literal(address);
        return parse(address);
    }

    public static String key(String address) {
        return network(address(address)).key();
    }

    private static Network network(InetAddress address) {
        return Network.from(address);
    }

    private static void literal(String address) {
        if (!ip(address)) {
            throw invalid();
        }
    }

    private static InetAddress parse(String address) {
        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException exception) {
            throw invalid();
        }
    }

    private static RequestException invalid() {
        return new RequestException(RequestProblem.INVALID_ADDRESS);
    }

    private static boolean ip(String address) {
        return address != null && (Ipv4.valid(address) || Ipv6.valid(address));
    }
}
