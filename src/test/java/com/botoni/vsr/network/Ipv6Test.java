package com.botoni.vsr.network;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Sintaxe do IPv6")
class Ipv6Test {

    @Controle
    @ParameterizedTest(name = "aceita \"{0}\"")
    @DisplayName("aceita a forma expandida e o :: em qualquer posição, de 0 a 7 hextets antes dele")
    @ValueSource(strings = {
            "2001:db8:1:2:aaaa:bbbb:cccc:dddd",
            "0000:0000:0000:0000:0000:0000:0000:0001",
            "::",
            "::1",
            "::a:b:c:d:e:f:1",
            "1::",
            "1::8",
            "1::3:4:5:6:7:8",
            "2001:db8::",
            "2001:db8::1",
            "2001:db8:1::ff:1",
            "1:2:3::6:7:8",
            "1:2:3:4::7:8",
            "1:2:3:4:5::8",
            "1:2:3:4:5:6::8",
            "1:2:3:4:5:6:7::",
            "FE80::ABCD"
    })
    void validFormsAreAccepted(String address) {
        assertThat(Ipv6.valid(address)).isTrue();
    }

    @Controle
    @ParameterizedTest(name = "recusa \"{0}\"")
    @DisplayName("recusa :: duplicado, hextets demais ou de menos, hextet longo, zona, IPv4 mapeado e texto que não é IP")
    @ValueSource(strings = {
            "",
            ":",
            ":::",
            ":1",
            "1:",
            "1:2:3:4:5:6:7",
            "1:2:3:4:5:6:7:8:9",
            "1:2:3:4:5:6:7::8",
            "::1:2:3:4:5:6:7:8",
            "2001:db8::1::1",
            "12345::1",
            "2001:db8::g",
            "fe80::1%1",
            "::ffff:203.0.113.7",
            "1234",
            "ab",
            "atacante.invalid"
    })
    void invalidFormsAreRejected(String address) {
        assertThat(Ipv6.valid(address)).isFalse();
    }

    @Test
    @Controle
    @DisplayName("texto malicioso longo é recusado sem retrocesso explosivo")
    void longHostileInputIsRejectedQuickly() {
        long start = System.nanoTime();

        assertThat(Ipv6.valid("a:".repeat(5000) + "!")).isFalse();
        assertThat(Ipv6.valid("ffff:".repeat(10000))).isFalse();

        assertThat(System.nanoTime() - start).isLessThan(1_000_000_000L);
    }
}
