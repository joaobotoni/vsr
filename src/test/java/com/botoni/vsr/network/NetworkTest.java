package com.botoni.vsr.network;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rede do cliente por família")
class NetworkTest {

    @Test
    @Controle
    @DisplayName("endereço IPv4 vira rede IPv4 e IPv6 vira rede IPv6")
    void familyIsChosenByAddress() throws Exception {
        assertThat(Network.from(InetAddress.getByName("203.0.113.7"))).isInstanceOf(Ipv4.class);
        assertThat(Network.from(InetAddress.getByName("2001:db8::1"))).isInstanceOf(Ipv6.class);
    }

    @Test
    @Controle
    @DisplayName("IPv4 é chave pelo endereço inteiro (/32)")
    void ipv4KeyIsWholeAddress() throws Exception {
        assertThat(Network.from(InetAddress.getByName("203.0.113.7")).key()).isEqualTo("203.0.113.7");
        assertThat(Network.from(InetAddress.getByName("203.0.113.8")).key()).isEqualTo("203.0.113.8");
    }

    @Test
    @Controle
    @DisplayName("IPv4 mapeado em IPv6 divide a chave com o IPv4 puro")
    void mappedIpv4SharesKey() throws Exception {
        assertThat(Network.from(InetAddress.getByName("::ffff:203.0.113.7")).key()).isEqualTo("203.0.113.7");
    }

    @Test
    @Controle
    @DisplayName("IPv6 é chave pelo /56, com o prefixo explícito")
    void ipv6KeyIsSlash56() throws Exception {
        assertThat(Network.from(InetAddress.getByName("2001:db8:1:2:aaaa:bbbb:cccc:dddd")).key())
                .isEqualTo("2001:db8:1:0:0:0:0:0/56");
        assertThat(Network.from(InetAddress.getByName("2001:db8:1:ff::1")).key())
                .isEqualTo("2001:db8:1:0:0:0:0:0/56");
    }

    @Test
    @Controle
    @DisplayName("a zona do IPv6 (interface de rede) não muda a chave")
    void ipv6ZoneIsIgnored() throws Exception {
        assertThat(Network.from(InetAddress.getByName("fe80::1%1")).key())
                .isEqualTo(Network.from(InetAddress.getByName("fe80::1")).key());
    }
}
