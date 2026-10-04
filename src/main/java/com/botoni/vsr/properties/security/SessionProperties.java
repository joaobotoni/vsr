package com.botoni.vsr.properties.security;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "security.session")
public record SessionProperties(@NotNull Duration ttl) { }
