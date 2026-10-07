package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.mapper.AuthenticationMapper;
import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.support.Brecha;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Cadastro")
class RegisterServiceTest {

    private final AccessService accessService = mock(AccessService.class);
    private final IndividualService individualService = mock(IndividualService.class);
    private final UserService userService = mock(UserService.class);
    private final LocalCredentialService localCredentialService = mock(LocalCredentialService.class);
    private final RegisterService registerService = new RegisterService(
            accessService, individualService, userService, localCredentialService, mock(AuthenticationMapper.class));

    @Test
    @Brecha
    @DisplayName("cadastro emite tokens na hora, sem confirmar a posse do e-mail ou do CPF")
    void registerGrantsAccessWithoutVerification() {
        RegisterRequest request = request();
        Individual person = new Individual(request.name(), request.cpf());
        User user = Users.ana();
        when(individualService.save(request.name(), request.cpf())).thenReturn(person);
        when(userService.save(person, request.email())).thenReturn(user);
        when(localCredentialService.save(eq(user), any()))
                .thenReturn(LocalCredential.builder().id(user.getId()).user(user).build());

        registerService.register(request, InetAddress.getLoopbackAddress());

        verify(accessService).grant(any(), eq(request.device()), any());
    }

    private static RegisterRequest request() {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");
        return new RegisterRequest("Ana", Cpf.of("52998224725"), Email.of("email.de.terceiro@vsr.com"), Password.of("senha segura 123"), device);
    }
}
