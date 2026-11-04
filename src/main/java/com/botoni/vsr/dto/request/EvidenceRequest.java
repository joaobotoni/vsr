package com.botoni.vsr.dto.request;

import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Upload de evidência em {@code multipart/form-data}: {@code file} (JPEG ou PNG), {@code item} (opcional, do mesmo
 * ambiente) e {@code description} (opcional).
 */
public record EvidenceRequest(
        @NotNull(message = "O arquivo é obrigatório.")
        MultipartFile file,

        UUID item,

        @Size(max = 500, message = "A descrição da evidência deve conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.SAFE, message = "A descrição da evidência contém caracteres inválidos.")
        String description
) {

    public EvidenceRequest {
        description = Texts.clean(description);
    }
}
