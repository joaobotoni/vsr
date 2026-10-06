package com.botoni.vsr.dto.request;

import com.botoni.vsr.database.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DeviceRequest(
        @NotNull(message = "O identificador do dispositivo é obrigatório.")
        UUID identifier,

        @NotNull(message = "A plataforma do dispositivo é obrigatória.")
        DevicePlatform platform,

        @NotBlank(message = "O fabricante é obrigatório.")
        @Size(max = 64, message = "O fabricante deve conter no máximo {max} caracteres.")
        String manufacturer,

        @NotBlank(message = "O modelo é obrigatório.")
        @Size(max = 64, message = "O modelo deve conter no máximo {max} caracteres.")
        String model,

        @NotBlank(message = "A versão do sistema é obrigatória.")
        @Size(max = 16, message = "A versão do sistema deve conter no máximo {max} caracteres.")
        @Pattern(regexp = "^[0-9A-Za-z._\\- ]+$", message = "A versão do sistema contém caracteres inválidos.")
        String osVersion
) {
}
