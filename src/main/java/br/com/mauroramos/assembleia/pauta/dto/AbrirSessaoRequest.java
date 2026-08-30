package br.com.mauroramos.assembleia.pauta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;

public record AbrirSessaoRequest(

        @Schema(description = "Duração da sessão (ISO-8601); ausente usa a duração default", example = "PT5M")
        Duration duracao
) {
}
