package br.com.mauroramos.assembleia.pauta.dto;

import br.com.mauroramos.assembleia.sessao.StatusSessao;

import java.time.Instant;

public record PautaResponse(
        Long id,
        String titulo,
        String descricao,
        StatusSessao status,
        Instant criadaEm
) {
}
