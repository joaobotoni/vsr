package com.botoni.vsr.network;

import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.InetAddress;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Máscara de prefixo")
class MaskTest {

    @Controle
    @ParameterizedTest(name = "{0}/{1} → {2}")
    @DisplayName("zera só os bits depois do prefixo, inclusive fora do limite de um byte")
    @CsvSource({
            "203.0.113.7,      32,  203.0.113.7",
            "203.0.113.7,      24,  203.0.113.0",
            "203.0.113.255,    20,  203.0.112.0",
            "203.0.113.7,      0,   0.0.0.0",
            "2001:db8:1:ff::1, 56,  2001:db8:1:0:0:0:0:0",
            "2001:db8:1:ff::1, 60,  2001:db8:1:f0:0:0:0:0",
            "2001:db8:1:ff::1, 128, 2001:db8:1:ff:0:0:0:1"
    })
    void bitsAfterPrefixAreCleared(String address, int prefix, String expected) throws Exception {
        assertThat(Mask.apply(InetAddress.getByName(address), prefix).getHostAddress()).isEqualTo(expected);
    }

    @Controle
    @ParameterizedTest(name = "{0}/{1} com \"{2}\" → {3}")
    @DisplayName("com a chave, formata o endereço mascarado e o prefixo no padrão pedido")
    @CsvSource(delimiter = '|', value = {
            "203.0.113.7      | 32 | %s    | 203.0.113.7",
            "203.0.113.7      | 24 | %s/%d | 203.0.113.0/24",
            "2001:db8:1:ff::1 | 56 | %s/%d | 2001:db8:1:0:0:0:0:0/56"
    })
    void keyFormatsMaskedAddress(String address, int prefix, String key, String expected) throws Exception {
        assertThat(Mask.key(InetAddress.getByName(address), prefix, key)).isEqualTo(expected);
    }

    @Test
    @Controle
    @DisplayName("a chave sai com dígitos ASCII qualquer que seja o idioma da JVM")
    void keyIgnoresDefaultLocale() throws Exception {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));

            assertThat(Mask.key(InetAddress.getByName("2001:db8::1"), 56, "%s/%d")).isEqualTo("2001:db8:0:0:0:0:0:0/56");
        } finally {
            Locale.setDefault(original);
        }
    }
}
