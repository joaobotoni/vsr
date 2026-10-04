package com.botoni.vsr.session.repository;

import com.botoni.vsr.session.entity.Device;
import com.botoni.vsr.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Optional<Device> findByUserAndIdentifier(User user, UUID identifier);
}
