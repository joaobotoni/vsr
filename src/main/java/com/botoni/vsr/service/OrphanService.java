package com.botoni.vsr.service;

import com.botoni.vsr.database.repository.EvidenceRepository;
import com.botoni.vsr.properties.EvidenceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Limpeza periódica dos arquivos de evidência sem registro no banco (sobras de uma queda no meio do envio). Só apaga
 * arquivos mais antigos que {@code vsr.evidence.orphan-age}, para nunca alcançar um envio em andamento. Arquivos de
 * evidências removidas logicamente têm registro e ficam.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrphanService {

    private final StorageService storageService;
    private final EvidenceRepository evidenceRepository;
    private final EvidenceProperties evidenceProperties;

    @Scheduled(cron = "${vsr.evidence.cleanup-cron}")
    public void sweep() {
        List<String> files = stale();
        files.forEach(this::purge);
    }

    private List<String> stale() {
        return storageService.stale(evidenceProperties.orphanAge());
    }

    private void purge(String name) {
        if (evidenceRepository.existsByPath(name)) {
            return;
        }
        log.warn("Arquivo de evidência órfão apagado: {}", name);
        storageService.delete(name);
    }
}
