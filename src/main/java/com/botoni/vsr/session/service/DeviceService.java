package com.botoni.vsr.session.service;

import com.botoni.vsr.session.dto.request.DeviceRequest;
import com.botoni.vsr.session.entity.Device;
import com.botoni.vsr.user.entity.User;
import com.botoni.vsr.session.mapper.DeviceMapper;
import com.botoni.vsr.session.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper deviceMapper;

    @Transactional
    public Device upsert(User user, DeviceRequest request) {
        return find(user, request).map(device -> update(device, request))
                .orElseGet(() -> register(user, request));
    }

    private Optional<Device> find(User user, DeviceRequest request) {
        return deviceRepository.findByUserAndIdentifier(user, request.identifier());
    }

    private Device register(User user, DeviceRequest request) {
        return deviceRepository.save(deviceMapper.toEntity(user, request));
    }

    private Device update(Device device, DeviceRequest request) {
        deviceMapper.update(device, request);
        return device;
    }
}
