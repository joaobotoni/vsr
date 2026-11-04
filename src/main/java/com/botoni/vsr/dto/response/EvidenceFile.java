package com.botoni.vsr.dto.response;

/**
 * Conteúdo de uma evidência para download, com o tipo gravado no upload (conferido pelos bytes).
 */
public record EvidenceFile(String contentType, byte[] content) {
}
