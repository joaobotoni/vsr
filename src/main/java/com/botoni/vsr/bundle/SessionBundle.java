package com.botoni.vsr.bundle;

import com.botoni.vsr.session.dto.request.DeviceRequest;

import java.net.InetAddress;

public record SessionBundle(DeviceRequest device, InetAddress ip) {
}
