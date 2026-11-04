package com.botoni.vsr.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

/**
 * Diretório onde ficam os arquivos das evidências ({@code EVIDENCE_DIR}).
 */
@Validated
@ConfigurationProperties(prefix = "vsr.evidence")
public record EvidenceProperties(@NotNull Path directory) {
}
