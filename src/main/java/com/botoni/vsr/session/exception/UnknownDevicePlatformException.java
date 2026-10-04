package com.botoni.vsr.session.exception;

public final class UnknownDevicePlatformException extends RuntimeException {

    public UnknownDevicePlatformException(String value) {
        super(String.format("A plataforma de dispositivo %s é desconhecida.", value));
    }
}
