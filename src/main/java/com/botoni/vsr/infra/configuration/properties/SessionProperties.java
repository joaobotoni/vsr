package com.botoni.vsr.infra.configuration.properties;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.session")
public record SessionProperties(@NotNull Duration ttl) { }
