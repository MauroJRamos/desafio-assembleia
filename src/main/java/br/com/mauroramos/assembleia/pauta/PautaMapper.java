package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.pauta.dto.PautaResponse;
import br.com.mauroramos.assembleia.sessao.StatusSessao;

import java.time.Instant;

public final class PautaMapper {

    private PautaMapper() {
    }

    public static Pauta toEntity(CriarPautaRequest request, Instant criadaEm) {
        return new Pauta(request.titulo(), request.descricao(), criadaEm);
    }

    public static PautaResponse toResponse(Pauta pauta) {
        StatusSessao status = StatusSessao.derivar(
                pauta.getSessaoAbertaEm(), pauta.getSessaoFechaEm(), Instant.now());

        return new PautaResponse(
                pauta.getId(),
                pauta.getTitulo(),
                pauta.getDescricao(),
                status,
                pauta.getCriadaEm());
    }
}
