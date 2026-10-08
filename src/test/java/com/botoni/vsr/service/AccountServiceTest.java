package com.botoni.vsr.service;

import com.botoni.vsr.database.entity.Individual;
import com.botoni.vsr.database.entity.LocalCredential;
import com.botoni.vsr.database.entity.User;
import com.botoni.vsr.database.enums.DevicePlatform;
import com.botoni.vsr.dto.request.DeviceRequest;
import com.botoni.vsr.dto.request.RegisterRequest;
import com.botoni.vsr.support.Brecha;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Users;
import com.botoni.vsr.vo.Cpf;
import com.botoni.vsr.vo.Email;
import com.botoni.vsr.vo.Name;
import com.botoni.vsr.vo.Password;
import com.botoni.vsr.vo.PasswordHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Conta")
class AccountServiceTest {

    private static final int SESSION = 10;
    private static final PasswordHash HASH = PasswordHash.of("$argon2id$v=19$m=19456,t=2,p=1$c2FsdA$aGFzaA");

    private final IndividualService individualService = mock(IndividualService.class);
    private final UserService userService = mock(UserService.class);
    private final LocalCredentialService localCredentialService = mock(LocalCredentialService.class);
    private final SessionService sessionService = mock(SessionService.class);
    private final AccessService accessService = mock(AccessService.class);
    private final AccountService accountService = new AccountService(
            individualService, userService, localCredentialService, sessionService, accessService);

    private final User user = Users.ana();

    @Test
    @Brecha
    @DisplayName("cadastro emite tokens na hora, sem confirmar a posse do e-mail ou do CPF")
    void openGrantsAccessWithoutVerification() {
        RegisterRequest request = request();
        InetAddress ip = InetAddress.getLoopbackAddress();
        Individual person = new Individual(request.name(), request.cpf());
        when(individualService.save(request.name(), request.cpf())).thenReturn(person);
        when(userService.save(person, request.email())).thenReturn(user);

        accountService.open(request, HASH, ip);

        InOrder order = inOrder(individualService, userService, localCredentialService, accessService);
        order.verify(individualService).save(request.name(), request.cpf());
        order.verify(userService).save(person, request.email());
        order.verify(localCredentialService).save(user, HASH);
        order.verify(accessService).grant(user, request.device(), ip);
    }

    @Test
    @Controle
    @DisplayName("substituir a senha grava o hash e depois revoga as outras sessões")
    void replaceChangesPasswordThenRevokesOthers() {
        LocalCredential credential = LocalCredential.builder().id(user.getId()).user(user).build();

        accountService.replace(Users.UUID, SESSION, credential, HASH);

        InOrder order = inOrder(localCredentialService, sessionService);
        order.verify(localCredentialService).replace(credential, HASH);
        order.verify(sessionService).revokeOthers(Users.UUID, SESSION);
    }

    @Test
    @Controle
    @DisplayName("troca concorrente detectada no banco não revoga sessões")
    void concurrentReplaceDoesNotRevoke() {
        LocalCredential credential = LocalCredential.builder().id(user.getId()).user(user).build();
        doThrow(new DataIntegrityViolationException("rn_senha_alterada"))
                .when(localCredentialService).replace(credential, HASH);

        assertThatThrownBy(() -> accountService.replace(Users.UUID, SESSION, credential, HASH))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(sessionService, never()).revokeOthers(any(), any());
    }

    @Controle
    @ParameterizedTest(name = "{0}.{1} transacional = {2}")
    @CsvSource({
            "AccountService,        open,     true",
            "AccountService,        replace,  true",
            "RegisterService,       register, false",
            "ChangePasswordService, change,   false",
            "LocalCredentialService, verify,  false",
            "LocalCredentialService, replace, true"
    })
    @DisplayName("só a gravação é transacional; o cálculo de senha fica fora da transação")
    void onlyWritesAreTransactional(String service, String method, boolean transactional) throws Exception {
        Method found = Arrays.stream(Class.forName("com.botoni.vsr.service." + service).getMethods())
                .filter(candidate -> candidate.getName().equals(method))
                .findFirst()
                .orElseThrow();

        assertThat(found.isAnnotationPresent(Transactional.class)).isEqualTo(transactional);
    }

    private static RegisterRequest request() {
        DeviceRequest device = new DeviceRequest(UUID.randomUUID(), DevicePlatform.ANDROID, "Samsung", "S23", "14");
        return new RegisterRequest(Name.of("Ana"), Cpf.of("52998224725"), Email.of("email.de.terceiro@vsr.com"), Password.of("senha segura 123"), device);
    }
}
