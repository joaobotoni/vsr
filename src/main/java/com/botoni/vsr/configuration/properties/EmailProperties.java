package com.botoni.vsr.configuration.properties;

import com.botoni.vsr.vo.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.session")
public record EmailProperties(@NotNull Email from) {}