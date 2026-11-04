package com.botoni.vsr.service;

import com.botoni.vsr.database.repository.EvidenceRepository;
import com.botoni.vsr.properties.EvidenceProperties;
import com.botoni.vsr.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Limpeza de arquivos órfãos de evidência")
class OrphanServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");

    @TempDir
    private Path directory;

    @Test
    @Controle
    @DisplayName("apaga só o arquivo antigo e sem registro; o recente e o registrado ficam")
    void sweepDeletesOnlyOldOrphans() throws Exception {
        Path orphan = file("orfao.jpg", NOW.minus(Duration.ofHours(2)));
        Path recorded = file("registrado.jpg", NOW.minus(Duration.ofHours(2)));
        Path recent = file("recente.jpg", NOW.minus(Duration.ofMinutes(5)));
        EvidenceProperties properties = new EvidenceProperties(directory, Duration.ofHours(1));
        EvidenceRepository repository = mock(EvidenceRepository.class);
        when(repository.existsByPath("registrado.jpg")).thenReturn(true);
        StorageService storage = new StorageService(properties, Clock.fixed(NOW, java.time.ZoneOffset.UTC));

        new OrphanService(storage, repository, properties).sweep();

        assertThat(orphan).doesNotExist();
        assertThat(recorded).exists();
        assertThat(recent).exists();
    }

    private Path file(String name, Instant modified) throws Exception {
        Path path = Files.write(directory.resolve(name), new byte[]{1});
        Files.setLastModifiedTime(path, FileTime.from(modified));
        return path;
    }
}
