package com.botoni.vsr.support;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Os buckets de rate limit vivem no contexto Spring compartilhado entre as classes de teste;
 * cada requisição usa um IP inédito para que um teste nunca consuma o limite de outro.
 */
public final class Ips {

    private static final AtomicInteger NEXT = new AtomicInteger(1);

    private Ips() {
    }

    public static String next() {
        int n = NEXT.getAndIncrement();
        return "10.%d.%d.%d".formatted(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF);
    }

    public static String nextV6() {
        return "2001:db8:%x::1".formatted(NEXT.getAndIncrement());
    }

    public static String nextV6InSameNetwork() {
        return "2001:db8:ffff:ffff::" + Integer.toHexString(NEXT.getAndIncrement());
    }
}
