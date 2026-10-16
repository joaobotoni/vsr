package com.botoni.vsr.network;

import com.botoni.vsr.exception.custom.RequestException;
import com.botoni.vsr.exception.enums.problem.RequestProblem;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
        assertThat(ClientNetwork.key("203.0.113.7")).isEqualTo("203.0.113.7");
    }

    @Test
    @Controle
    @DisplayName("endereços IPv6 do mesmo /56 viram a mesma chave")
    void ipv6SameNetworkSharesKey() {
        assertThat(ClientNetwork.key("2001:db8:1:2:aaaa:bbbb:cccc:dddd"))
                .isEqualTo(ClientNetwork.key("2001:db8:1:ff:0:0:0:1"))
                .isEqualTo("2001:db8:1:0:0:0:0:0/56");
    }

    @Test
    @Controle
    @DisplayName("redes /56 diferentes viram chaves diferentes")
    void ipv6OtherNetworkHasOtherKey() {
        assertThat(ClientNetwork.key("2001:db8:1:2:0:0:0:1")).isNotEqualTo(ClientNetwork.key("2001:db8:1:100:0:0:0:1"));
    }

    @Test
    @Controle
    @DisplayName("IPv4 e IPv6 literais viram o endereço correspondente")
    void literalsAreParsed() {
        assertThat(ClientNetwork.address("203.0.113.7")).isInstanceOf(Inet4Address.class);
        assertThat(ClientNetwork.address("2001:db8:0:0:0:0:0:1")).isInstanceOf(Inet6Address.class);
    }

    @Controle
    @ParameterizedTest(name = "\"{0}\" vira {1}")
    @DisplayName("IPv4 aceita octetos de 0 a 255, inclusive as bordas")
    @CsvSource({"0.0.0.0, 0.0.0.0", "255.255.255.255, 255.255.255.255", "10.0.0.1, 10.0.0.1", "250.249.199.99, 250.249.199.99"})
    void ipv4OctetsInRangeAreAccepted(String address, String expected) {
        assertThat(ClientNetwork.address(address).getHostAddress()).isEqualTo(expected);
    }

    @Controle
    @ParameterizedTest(name = "\"{0}\" é aceito")
    @DisplayName("IPv6 é aceito na forma completa e na comprimida (::), como os proxies costumam enviar")
    @ValueSource(strings = {"0:0:0:0:0:0:0:1", "2001:DB8:0:0:0:0:0:1", "2001:db8:1:2:aaaa:bbbb:cccc:dddd", "fe80:0:0:0:0:0:0:1", "::1", "::", "2001:db8::1", "2001:DB8::", "fe80::1", "2001:db8:1::ff:1", "1::8", "1:2:3:4:5:6::8"})
    void ipv6FormsAreAccepted(String address) {
        assertThat(ClientNetwork.address(address)).isNotNull();
    }

    @Controle
    @NullSource
    @ParameterizedTest(name = "\"{0}\" é recusado sem consulta DNS")
    @ValueSource(strings = {"atacante.invalid", "localhost", "", "1.2.3", "g:h", "x.y:z", "atacante.invalid:80", "evil:host%eth0", "999.1.1.1", "256.0.0.1", "1.2.3.300", "1.2.3.4.5", "1.2.3.4.", ".1.2.3.4", "1..2.3", "1.2.3.0999", "01.02.03.004", "::ffff:203.0.113.7", "fe80::1%1", "2001:db8::1::1", ":::", "1:2:3:4:5:6:7::8", "12345::1", "2001:db8::g", ":1", "1:", "2001:db8:0:0:0:0:0:1%1", "2001:db8:0:0:0:0:0:0:1"})
    void nonLiteralIsRejected(String address) {
        assertThatThrownBy(() -> ClientNetwork.address(address))
                .isInstanceOf(RequestException.class)
                .extracting("problem").isEqualTo(RequestProblem.INVALID_ADDRESS);
    }

    @Test
    @Controle
    @DisplayName("a chave de rede também recusa endereço que não é IP")
    void networkRejectsNonLiteral() {
        assertThatThrownBy(() -> ClientNetwork.key("atacante.invalid"))
                .isInstanceOf(RequestException.class)
                .extracting("problem").isEqualTo(RequestProblem.INVALID_ADDRESS);
    }
}
