package br.com.mauroramos.assembleia.voto.dto;

import br.com.mauroramos.assembleia.voto.OpcaoVoto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistrarVotoRequest(

        @Schema(description = "Identificador do associado que está votando", example = "12345678901")
        @NotBlank(message = "associadoId é obrigatório")
        String associadoId,

        @Schema(description = "Opção de voto")
        @NotNull(message = "voto é obrigatório")
        OpcaoVoto voto
) {
}
