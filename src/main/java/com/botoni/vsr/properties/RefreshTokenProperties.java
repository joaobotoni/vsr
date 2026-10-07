package com.botoni.vsr.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security.refresh-token")
public record RefreshTokenProperties(
        @NotBlank String secretKey
) {
}
