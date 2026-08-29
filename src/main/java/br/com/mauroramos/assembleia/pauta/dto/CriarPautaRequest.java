package br.com.mauroramos.assembleia.pauta.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarPautaRequest(

        @NotBlank(message = "titulo é obrigatório")
        @Size(min = 3, max = 120, message = "titulo deve ter entre 3 e 120 caracteres")
        String titulo,

        @Size(max = 500, message = "descricao deve ter no máximo 500 caracteres")
        String descricao
) {
}
