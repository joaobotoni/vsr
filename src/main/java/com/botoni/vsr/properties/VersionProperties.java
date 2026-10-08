package com.botoni.vsr.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "vsr.version")
public record VersionProperties(@NotBlank String header, @NotEmpty String[] supported, @Name("default") @NotBlank String fallback) {
}
