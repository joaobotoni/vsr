package com.botoni.vsr.controller;

import com.botoni.vsr.dto.response.EvidenceFile;
import com.botoni.vsr.principal.Principal;
import com.botoni.vsr.service.EvidenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/evidences", version = "1")
@RequiredArgsConstructor
public class EvidenceController {

    private final EvidenceService evidenceService;

    /** A foto só sai para o dono: sem cache compartilhado e sem o navegador adivinhar outro tipo de conteúdo. */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        EvidenceFile file = evidenceService.download(principal.user(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .cacheControl(CacheControl.noStore().cachePrivate())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().build().toString())
                .body(file.content());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        evidenceService.remove(principal.user(), id);
        return ResponseEntity.noContent().build();
    }
}
