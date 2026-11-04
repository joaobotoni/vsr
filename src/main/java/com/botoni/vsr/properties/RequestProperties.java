package com.botoni.vsr.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

/**
 * Limites do corpo: {@code maxBodySize} para toda a API e {@code maxUploadSize} só para o upload de evidências.
 */
@Validated
@ConfigurationProperties(prefix = "vsr.request")
public record RequestProperties(@NotNull DataSize maxBodySize, @NotNull DataSize maxUploadSize) {
}
