package br.com.mauroramos.assembleia.pauta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPautaRequest(

        @Schema(description = "Título da pauta", example = "Aprovação do orçamento 2026")
        @NotBlank(message = "titulo é obrigatório")
        @Size(min = 3, max = 120, message = "titulo deve ter entre 3 e 120 caracteres")
        String titulo,

        @Schema(description = "Descrição opcional da pauta", example = "Votação do orçamento anual")
        @Size(max = 500, message = "descricao deve ter no máximo 500 caracteres")
        String descricao
) {
}
