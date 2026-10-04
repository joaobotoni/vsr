package com.botoni.vsr.command.session;

import com.botoni.vsr.dto.request.session.DeviceRequest;

import java.net.InetAddress;

public record SessionCommand(DeviceRequest device, InetAddress ip) {
}
