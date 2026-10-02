package com.botoni.vsr.configuration.properties;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "vsr.mail")
public record SessionProperties(@NotNull Duration ttl) { }
