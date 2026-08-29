package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.pauta.dto.PautaResponse;
import br.com.mauroramos.assembleia.pauta.dto.SessaoResponse;
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

    public static SessaoResponse toSessaoResponse(Pauta pauta) {
        // "agora" = o proprio instante de abertura: e a resposta da transicao que acabou de
        // acontecer, entao o estado nesse momento se deriva a partir dele mesmo (cobre o caso
        // de borda duracao=0, onde a sessao ja nasce FECHADA).
        StatusSessao status = StatusSessao.derivar(
                pauta.getSessaoAbertaEm(), pauta.getSessaoFechaEm(), pauta.getSessaoAbertaEm());

        return new SessaoResponse(
                pauta.getId(),
                status,
                pauta.getSessaoAbertaEm(),
                pauta.getSessaoFechaEm());
    }
}
