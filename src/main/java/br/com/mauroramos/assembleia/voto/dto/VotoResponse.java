package br.com.mauroramos.assembleia.voto.dto;

import br.com.mauroramos.assembleia.voto.OpcaoVoto;

import java.time.Instant;

public record VotoResponse(
        Long id,
        Long pautaId,
        String associadoId,
        OpcaoVoto voto,
        Instant registradoEm
) {
}
