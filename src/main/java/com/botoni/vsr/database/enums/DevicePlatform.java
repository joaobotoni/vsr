package com.botoni.vsr.database.enums;

import com.botoni.vsr.exception.custom.DevicePlatformException;
import com.botoni.vsr.exception.enums.problem.DevicePlatformProblem;
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

    public static DevicePlatform from(String value) {
        for (DevicePlatform platform : values()) {
            if (platform.value.equals(value)) {
                return platform;
            }
        }
        throw new DevicePlatformException(DevicePlatformProblem.UNKNOWN, value);
    }
}
