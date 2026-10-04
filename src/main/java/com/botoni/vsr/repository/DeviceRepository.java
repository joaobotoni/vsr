package com.botoni.vsr.repository;

import com.botoni.vsr.entity.users.Device;
import com.botoni.vsr.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Optional<Device> findByUserAndIdentifier(User user, UUID identifier);
}
