package com.botoni.vsr.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

final class Mask {

    private static final int BITS = Byte.SIZE;
    private static final int HIGHEST = 0x80;

    private Mask() {
    }

    static String key(InetAddress address, int prefix, String format) {
        return String.format(Locale.ROOT, format, apply(address, prefix).getHostAddress(), prefix);
    }

    static InetAddress apply(InetAddress address, int prefix) {
        byte[] bytes = address.getAddress();
        truncate(bytes, prefix);
        return inet(bytes);
    }

    private static void truncate(byte[] bytes, int prefix) {
        for (int bit = prefix; bit < bytes.length * BITS; bit++) {
            bytes[bit / BITS] &= (byte) ~(HIGHEST >>> bit % BITS);
        }
    }

    private static InetAddress inet(byte[] bytes) {
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException exception) {
            throw new IllegalStateException("Endereço mascarado com tamanho inválido", exception);
        }
    }
}
