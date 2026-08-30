package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.pauta.dto.PautaResponse;
import br.com.mauroramos.assembleia.pauta.dto.SessaoResponse;
import br.com.mauroramos.assembleia.sessao.StatusSessao;
import br.com.mauroramos.assembleia.tela.BotaoTela;
import br.com.mauroramos.assembleia.tela.ItemTela;
import br.com.mauroramos.assembleia.tela.TelaResponse;
import br.com.mauroramos.assembleia.tela.TipoTela;
import br.com.mauroramos.assembleia.voto.OpcaoVoto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

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

    public static TelaResponse toTelaVotacao(Pauta pauta) {
        String urlVotos = "/api/v1/pautas/" + pauta.getId() + "/votos";
        String descricao = pauta.getDescricao() != null ? pauta.getDescricao() : pauta.getTitulo();

        List<ItemTela> itens = List.of(
                ItemTela.texto(descricao),
                ItemTela.inputTexto("Identificação do associado", "associadoId"));

        // "body" parcial: o app acrescenta o valor digitado no INPUT_TEXTO (chave "associadoId")
        // antes de fazer o POST, conforme o protocolo do Anexo 1.
        List<BotaoTela> botoes = List.of(
                new BotaoTela("Sim", urlVotos, Map.of("voto", OpcaoVoto.SIM.name())),
                new BotaoTela("Não", urlVotos, Map.of("voto", OpcaoVoto.NAO.name())));

        return new TelaResponse(TipoTela.FORMULARIO, pauta.getTitulo(), itens, botoes);
    }
}
