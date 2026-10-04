package com.botoni.vsr.session.dto.request;

import com.botoni.vsr.session.enums.DevicePlatform;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DeviceRequest(

        @NotNull(message = "O identificador do dispositivo é obrigatório")
        UUID identifier,

        @NotNull(message = "A plataforma do dispositivo é obrigatória")
        DevicePlatform platform,

        @NotBlank(message = "O fabricante é obrigatório")
        @Size(max = 64, message = "O fabricante deve ter no máximo {max} caracteres")
        String manufacturer,

        @NotBlank(message = "O modelo é obrigatório")
        @Size(max = 64, message = "O modelo deve ter no máximo {max} caracteres")
        String model,

        @NotBlank(message = "A versão do sistema é obrigatória")
        @Size(max = 16, message = "A versão do sistema deve ter no máximo {max} caracteres")
        @Pattern(regexp = "^[0-9A-Za-z._\\- ]+$", message = "A versão do sistema contém caracteres inválidos")
        String osVersion
) {}