package com.botoni.vsr.network;

import java.net.Inet6Address;
import java.util.regex.Pattern;

public record Ipv6(Inet6Address address) implements Network {

    private static final int PREFIX = 56;
    private static final int HEXTETS = 8;
    private static final String FORMAT = "%s/%d";
    private static final String HEXTET = "[0-9a-fA-F]{1,4}";
    private static final String OR = "|";
    private static final Pattern SYNTAX = syntax();

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

    private static Pattern syntax() {
        return Pattern.compile("^(" + alternatives() + ")$");
    }

    private static String alternatives() {
        return expanded() + OR + compressions();
    }

    private static String compressions() {
        StringBuilder compressions = new StringBuilder(compressed(0));
        for (int leading = 1; leading < HEXTETS; leading++) {
            compressions.append(OR).append(compressed(leading));
        }
        return compressions.toString();
    }

    private static String expanded() {
        return "(" + HEXTET + ":){" + (HEXTETS - 1) + "}" + HEXTET;
    }

    private static String compressed(int leading) {
        return head(leading) + "::" + tail(HEXTETS - 1 - leading);
    }

    private static String head(int count) {
        return count == 0 ? "" : "(" + HEXTET + ":){" + (count - 1) + "}" + HEXTET;
    }

    private static String tail(int remaining) {
        return remaining == 0 ? "" : "(" + HEXTET + "(:" + HEXTET + "){0," + (remaining - 1) + "})?";
    }
}
