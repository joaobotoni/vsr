package com.botoni.vsr.infra.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServletServerHttpRequest;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public final class RemoteAddress {

    private RemoteAddress() {
    }

    public static InetAddress of(HttpServletRequest http) {
        ServletServerHttpRequest request = new ServletServerHttpRequest(http);
        InetSocketAddress remote = request.getRemoteAddress();
        return remote.getAddress();
    }
}