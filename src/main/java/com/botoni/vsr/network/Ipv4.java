package com.botoni.vsr.network;

import java.net.Inet4Address;
import java.util.regex.Pattern;

public record Ipv4(Inet4Address address) implements Network {

    private static final int PREFIX = 32;
    private static final String FORMAT = "%s";
    private static final Pattern SYNTAX = Pattern.compile("^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(?!$)|$)){4}$");

    public static boolean valid(String address) {
        return SYNTAX.matcher(address).matches();
    }

    @Override
    public int prefix() {
        return PREFIX;
    }

    @Override
    public String format() {
        return FORMAT;
    }
}
