package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.repository.DeviceRepository;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.exception.custom.DeviceException;
import com.botoni.vsr.exception.enums.problem.DeviceProblem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public Device register(User user, DeviceRequest request) {
        upsert(user, request);
        return find(user, request.identifier());
    }

    private void upsert(User user, DeviceRequest request) {
        deviceRepository.upsert(user.getId(), request.identifier(), platform(request),
                request.manufacturer(), request.model(), request.osVersion());
    }

    private Device find(User user, UUID identifier) {
        return deviceRepository.findByUserAndIdentifier(user, identifier)
                .orElseThrow(() -> new DeviceException(DeviceProblem.NOT_FOUND));
    }

    private static String platform(DeviceRequest request) {
        return request.platform().getValue();
    }
}
