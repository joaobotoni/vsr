package com.botoni.vsr.lib;

import com.botoni.vsr.exception.custom.RequestException;
import com.botoni.vsr.exception.enums.problem.RequestProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.Inet4Address;
import java.net.Inet6Address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Endereço e rede do cliente")
class ClientNetworkTest {

    @Test
    @Controle
    @DisplayName("IPv4 é usado como está")
    void ipv4IsKept() {
        assertThat(ClientNetwork.of("203.0.113.7")).isEqualTo("203.0.113.7");
    }

    @Test
    @Controle
    @DisplayName("endereços IPv6 do mesmo /64 viram a mesma chave")
    void ipv6SameNetworkSharesKey() {
        assertThat(ClientNetwork.of("2001:db8:1:2:aaaa:bbbb:cccc:dddd"))
                .isEqualTo(ClientNetwork.of("2001:db8:1:2::1"))
                .isEqualTo("2001:db8:1:2:0:0:0:0/64");
    }

    @Test
    @Controle
    @DisplayName("redes /64 diferentes viram chaves diferentes")
    void ipv6OtherNetworkHasOtherKey() {
        assertThat(ClientNetwork.of("2001:db8:1:2::1")).isNotEqualTo(ClientNetwork.of("2001:db8:1:3::1"));
    }

    @Test
    @Controle
    @DisplayName("IPv4 e IPv6 literais viram o endereço correspondente")
    void literalsAreParsed() {
        assertThat(ClientNetwork.address("203.0.113.7")).isInstanceOf(Inet4Address.class);
        assertThat(ClientNetwork.address("2001:db8::1")).isInstanceOf(Inet6Address.class);
    }

    @Controle
    @NullSource
    @ParameterizedTest(name = "\"{0}\" é recusado sem consulta DNS")
    @ValueSource(strings = {"atacante.invalid", "localhost", "", "1.2.3"})
    void nonLiteralIsRejected(String address) {
        assertThatThrownBy(() -> ClientNetwork.address(address))
                .isInstanceOf(RequestException.class)
                .extracting("problem").isEqualTo(RequestProblem.INVALID_ADDRESS);
    }

    @Test
    @Controle
    @DisplayName("a chave de rede também recusa endereço que não é IP")
    void networkRejectsNonLiteral() {
        assertThatThrownBy(() -> ClientNetwork.of("atacante.invalid"))
                .isInstanceOf(RequestException.class)
                .extracting("problem").isEqualTo(RequestProblem.INVALID_ADDRESS);
    }
}
