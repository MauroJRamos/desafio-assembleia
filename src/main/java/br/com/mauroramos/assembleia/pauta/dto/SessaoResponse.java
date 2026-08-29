package br.com.mauroramos.assembleia.pauta.dto;

import br.com.mauroramos.assembleia.sessao.StatusSessao;

import java.time.Instant;

public record SessaoResponse(
        Long pautaId,
        StatusSessao status,
        Instant abertaEm,
        Instant fechaEm
) {
}
