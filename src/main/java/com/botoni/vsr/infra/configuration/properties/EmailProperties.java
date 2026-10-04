package com.botoni.vsr.infra.configuration.properties;

import com.botoni.vsr.shared.vo.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vsr.mail")
public record EmailProperties(@NotNull Email from) {}