package com.botoni.vsr.service.session;

import com.botoni.vsr.dto.request.session.DeviceRequest;
import com.botoni.vsr.entity.users.Device;
import com.botoni.vsr.entity.users.User;
import com.botoni.vsr.mapper.session.DeviceMapper;
import com.botoni.vsr.repository.DeviceRepository;
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
