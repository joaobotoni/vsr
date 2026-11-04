package com.botoni.vsr.dto.request;

import com.botoni.vsr.lib.Texts;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record InspectionEditRequest(
        @Size(max = 2000, message = "As observações devem conter no máximo {max} caracteres.")
        @Pattern(regexp = Texts.NOTE, message = "As observações contêm caracteres inválidos.")
        String notes
) {

    public InspectionEditRequest {
        notes = Texts.note(notes);
    }
}
