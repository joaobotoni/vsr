package com.botoni.vsr.repository;

import com.botoni.vsr.entity.users.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, Integer> {
    Optional<Device> findByIdentifier(@Param("identificador") UUID identifier);
}
