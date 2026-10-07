package com.botoni.vsr.properties;

import com.botoni.vsr.vo.Email;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "vsr.mail")
public record EmailProperties(@NotNull Email from, @NotNull Path attachments) {
}
