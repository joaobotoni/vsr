package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Device;
import com.botoni.vsr.database.entity.Session;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.dto.response.AuthenticationResponse;
import com.botoni.vsr.dto.response.TokenResponse;
import com.botoni.vsr.mapper.AuthenticationMapperImpl;
import com.botoni.vsr.mapper.UserMapper;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Concessão de acesso")
class AccessServiceTest {

    private static final int SESSION = 10;

    private final DeviceService deviceService = mock(DeviceService.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final AccessService accessService = new AccessService(deviceService, sessionService, refreshTokenService,
            tokenService, new AuthenticationMapperImpl(Mappers.getMapper(UserMapper.class)));

    private final User user = Users.ana();
    private final DeviceRequest request = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");
    private final InetAddress ip = InetAddress.getLoopbackAddress();

    @Test
    @Controle
    @DisplayName("registra o dispositivo, abre a sessão, emite os tokens e monta a resposta, nessa ordem")
    void grantBuildsResponse() {
        Device device = Device.builder().id(3).user(user).build();
        Session session = Session.builder().id(SESSION).device(device).build();
        TokenResponse token = new TokenResponse("access", "refresh", 900);
        when(deviceService.register(user, request)).thenReturn(device);
        when(sessionService.open(device, ip)).thenReturn(session);
        when(refreshTokenService.issue(session)).thenReturn("refresh");
        when(tokenService.issue(user, SESSION, "refresh")).thenReturn(token);

        AuthenticationResponse response = accessService.grant(user, request, ip);

        assertThat(response.token()).isEqualTo(token);
        assertThat(response.user().id()).isEqualTo(Users.UUID);
        InOrder order = inOrder(deviceService, sessionService, refreshTokenService, tokenService);
        order.verify(deviceService).register(user, request);
        order.verify(sessionService).open(device, ip);
        order.verify(refreshTokenService).issue(session);
        order.verify(tokenService).issue(user, SESSION, "refresh");
    }
}
