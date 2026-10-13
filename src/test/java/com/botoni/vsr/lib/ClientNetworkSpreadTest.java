package com.botoni.vsr.lib;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Agrupamento de IPv6 no rate limit")
class ClientNetworkSpreadTest {

    private static final int SUBNETS = 256;

    @Test
    @Controle
    @DisplayName("as 256 redes /64 de um /56 (comum em provedor doméstico) dividem um único bucket")
    void prefix56SharesOneBucket() {
        Set<String> buckets = new HashSet<>();
        for (int subnet = 0; subnet < SUBNETS; subnet++) {
            buckets.add(ClientNetwork.of("2001:db8:aa:bb%02x::1".formatted(subnet)));
        }

        assertThat(buckets).hasSize(1);
    }
}
