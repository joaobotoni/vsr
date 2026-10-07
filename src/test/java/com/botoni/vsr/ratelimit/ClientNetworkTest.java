package com.botoni.vsr.ratelimit;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rede do cliente para o rate limit")
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
}
