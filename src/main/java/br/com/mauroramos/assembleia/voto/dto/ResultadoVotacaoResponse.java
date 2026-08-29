package br.com.mauroramos.assembleia.voto.dto;

import br.com.mauroramos.assembleia.sessao.StatusSessao;
import br.com.mauroramos.assembleia.voto.Resultado;

import java.time.Instant;

public record ResultadoVotacaoResponse(
        Long pautaId,
        String titulo,
        StatusSessao status,
        long totalVotos,
        long votosSim,
        long votosNao,
        Resultado resultado,
        boolean parcial,
        Instant apuradoEm
) {
}
