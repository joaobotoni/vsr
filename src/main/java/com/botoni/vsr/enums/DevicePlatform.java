package com.botoni.vsr.enums;

import com.botoni.vsr.exception.infrastructure.UnknownTypeException;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum DevicePlatform {

    ANDROID("android"),
    IOS("ios");

    @JsonValue
    private final String value;

    DevicePlatform(String value) {
        this.value = value;
    }

    public static DevicePlatform fromValue(String value) {
        for (DevicePlatform platform : values()) {
            if (platform.value.equals(value)) {
                return platform;
            }
        }
        throw new UnknownTypeException.Device(value);
    }
}