package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.database.repository.DeviceRepository;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.exception.custom.DeviceException;
import com.botoni.vsr.exception.enums.problem.DeviceProblem;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Dispositivos")
class DeviceServiceTest {

    private final DeviceRepository repository = mock(DeviceRepository.class);
    private final DeviceService deviceService = new DeviceService(repository);

    private final User user = Users.ana();
    private final DeviceRequest request =
            new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");

    @Test
    @Controle
    @DisplayName("registrar grava pela procedure e devolve o dispositivo gravado")
    void registerUpsertsThenFinds() {
        Device device = Device.builder().id(3).user(user).identifier(request.identifier()).build();
        when(repository.findByUserAndIdentifier(user, request.identifier())).thenReturn(Optional.of(device));

        assertThat(deviceService.register(user, request)).isSameAs(device);

        InOrder order = inOrder(repository);
        order.verify(repository).upsert(user.getId(), request.identifier(), "android", "Samsung", "S23", "14");
        order.verify(repository).findByUserAndIdentifier(user, request.identifier());
    }

    @Test
    @Controle
    @DisplayName("dispositivo que some depois do registro gera erro interno")
    void missingDeviceAfterUpsertFails() {
        when(repository.findByUserAndIdentifier(user, request.identifier())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.register(user, request))
                .isInstanceOf(DeviceException.class)
                .extracting("problem").isEqualTo(DeviceProblem.NOT_FOUND);
    }
}
