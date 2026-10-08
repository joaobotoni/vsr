package com.botoni.vsr.service;

import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Name;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.net.InetAddress;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Cadastro")
class RegisterServiceTest {

    private static final PasswordHash HASH = PasswordHash.of("$argon2id$v=19$m=19456,t=2,p=1$c2FsdA$aGFzaA");

    private final LocalCredentialService localCredentialService = mock(LocalCredentialService.class);
    private final AccountService accountService = mock(AccountService.class);
    private final RegisterService registerService = new RegisterService(localCredentialService, accountService);

    @Test
    @Controle
    @DisplayName("o hash da senha é calculado antes de abrir a conta, e a conta recebe o hash pronto")
    void passwordIsHashedBeforeOpeningAccount() {
        RegisterRequest request = request();
        InetAddress ip = InetAddress.getLoopbackAddress();
        when(localCredentialService.hash(request.password())).thenReturn(HASH);

        registerService.register(request, ip);

        InOrder order = inOrder(localCredentialService, accountService);
        order.verify(localCredentialService).hash(request.password());
        order.verify(accountService).open(request, HASH, ip);
    }

    private static RegisterRequest request() {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");
        return new RegisterRequest(Name.of("Ana"), Cpf.of("52998224725"), Email.of("email.de.terceiro@vsr.com"), Password.of("senha segura 123"), device);
    }
}
