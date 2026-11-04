package com.botoni.vsr.service;

import com.botoni.vsr.exception.custom.StorageException;
import com.botoni.vsr.exception.enums.problem.StorageProblem;
import com.botoni.vsr.properties.EvidenceProperties;
import com.botoni.vsr.vo.Photo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

/**
 * Arquivos das evidências em disco. O nome é sempre gerado aqui (UUID + extensão do formato real); a leitura confere o
 * caminho real dentro do diretório e não segue link simbólico. Gravado dentro de uma transação que depois é desfeita,
 * o arquivo é apagado.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final EvidenceProperties evidenceProperties;

    public String save(Photo photo) {
        String name = name(photo);
        Path path = path(name);
        write(path, photo);
        cleanup(path);
        return name;
    }

    public byte[] load(String name) {
        Path real = real(path(name));
        inside(real);
        return read(real);
    }

    private static String name(Photo photo) {
        return UUID.randomUUID() + photo.extension();
    }

    private Path path(String name) {
        return directory().resolve(name).normalize();
    }

    private Path directory() {
        return evidenceProperties.directory().toAbsolutePath().normalize();
    }

    private void write(Path path, Photo photo) {
        try {
            Files.createDirectories(directory());
            Files.write(path, photo.content(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new StorageException(StorageProblem.WRITE_FAILED, exception);
        }
    }

    private static void cleanup(Path path) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new Cleanup(path));
    }

    private static Path real(Path path) {
        try {
            return path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException exception) {
            throw new StorageException(StorageProblem.READ_FAILED, exception);
        }
    }

    private void inside(Path real) {
        if (!real.startsWith(real(directory()))) {
            throw new StorageException(StorageProblem.OUTSIDE_DIRECTORY);
        }
    }

    private static byte[] read(Path real) {
        try (InputStream input = Files.newInputStream(real, LinkOption.NOFOLLOW_LINKS)) {
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new StorageException(StorageProblem.READ_FAILED, exception);
        }
    }

    /** Apaga o arquivo gravado quando a transação que o registrou é desfeita. */
    private record Cleanup(Path path) implements TransactionSynchronization {

        @Override
        public void afterCompletion(int status) {
            if (status != STATUS_ROLLED_BACK) {
                return;
            }
            try {
                Files.deleteIfExists(path);
            } catch (IOException exception) {
                log.warn("Arquivo de evidência órfão não apagado: {}", path.getFileName(), exception);
            }
        }
    }
}
