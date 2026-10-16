package com.botoni.vsr.network;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public sealed interface Network permits Ipv4, Ipv6 {

    InetAddress address();

    int prefix();

    String format();

    default String key() {
        return Mask.key(address(), prefix(), format());
    }

    static Network from(InetAddress address) {
        return address instanceof Inet4Address ipv4 ? new Ipv4(ipv4) : new Ipv6((Inet6Address) address);
    }
}
