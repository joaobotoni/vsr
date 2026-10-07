package com.botoni.vsr.security;

import com.botoni.vsr.exception.custom.OpaqueTokenException;
import com.botoni.vsr.exception.enums.problem.OpaqueTokenProblem;
import com.botoni.vsr.support.Controle;
import com.botoni.vsr.support.Tokens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Hash HMAC do refresh token")
class OpaqueTokenTest {

    private static final String OTHER_SECRET = "MTEyMjMzNDQ1NTY2Nzc4ODk5YWFiYmNjZGRlZWZmMDA=";

    private final OpaqueToken opaqueToken = new OpaqueToken(Tokens.REFRESH_SECRET);

    @Test
    @Controle
    @DisplayName("hash tem 32 bytes e é sempre o mesmo para o mesmo token")
    void hashIsDeterministic() {
        String token = opaqueToken.generate();

        assertThat(opaqueToken.hash(token)).hasSize(32).isEqualTo(opaqueToken.hash(token));
    }

    @Test
    @Controle
    @DisplayName("sem a chave não se chega ao hash gravado: outra chave ou SHA-256 puro dão outro valor")
    void hashDependsOnKey() throws Exception {
        String token = opaqueToken.generate();
        byte[] plain = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));

        assertThat(opaqueToken.hash(token))
                .isNotEqualTo(new OpaqueToken(OTHER_SECRET).hash(token))
                .isNotEqualTo(plain);
    }

    @Test
    @Controle
    @DisplayName("chave ausente, fora de Base64 ou curta impede a subida")
    void insecureSecretIsRejected() {
        String shortSecret = Base64.getEncoder().encodeToString(new byte[16]);

        assertThatThrownBy(() -> new OpaqueToken(" ")).extracting("problem").isEqualTo(OpaqueTokenProblem.MISSING_SECRET);
        assertThatThrownBy(() -> new OpaqueToken("não é base64")).extracting("problem").isEqualTo(OpaqueTokenProblem.INVALID_SECRET);
        assertThatThrownBy(() -> new OpaqueToken(shortSecret))
                .isInstanceOf(OpaqueTokenException.class)
                .extracting("problem").isEqualTo(OpaqueTokenProblem.WEAK_SECRET);
    }
}
