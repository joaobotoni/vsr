package com.botoni.vsr.properties;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "security.session")
public record SessionProperties(@NotNull Duration ttl, @NotNull Duration idle, @Positive int max) {
}
