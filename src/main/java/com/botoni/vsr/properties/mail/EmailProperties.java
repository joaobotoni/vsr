package com.botoni.vsr.properties.mail;

import com.botoni.vsr.vo.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vsr.mail")
public record EmailProperties(@NotNull Email from) {}