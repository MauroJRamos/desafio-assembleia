package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.common.config.SessaoProperties;
import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.common.error.RegraDeNegocioException;
import br.com.mauroramos.assembleia.pauta.dto.AbrirSessaoRequest;
import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.sessao.StatusSessao;
import br.com.mauroramos.assembleia.voto.OpcaoVoto;
import br.com.mauroramos.assembleia.voto.Resultado;
import br.com.mauroramos.assembleia.voto.VotoRepository;
import br.com.mauroramos.assembleia.voto.dto.ResultadoVotacaoResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;
    private final VotoRepository votoRepository;
    private final Clock clock;
    private final SessaoProperties sessaoProperties;

    public PautaService(
            PautaRepository pautaRepository,
            VotoRepository votoRepository,
            Clock clock,
            SessaoProperties sessaoProperties) {
        this.pautaRepository = pautaRepository;
        this.votoRepository = votoRepository;
        this.clock = clock;
        this.sessaoProperties = sessaoProperties;
    }

    @Transactional
    public Pauta cadastrar(CriarPautaRequest request) {
        Pauta pauta = PautaMapper.toEntity(request, clock.instant());
        return pautaRepository.save(pauta);
    }

    @Transactional
    public Pauta abrirSessao(Long pautaId, AbrirSessaoRequest request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pauta " + pautaId + " não encontrada."));

        if (pauta.getSessaoAbertaEm() != null) {
            throw new ConflitoDeEstadoException("Sessão da pauta " + pautaId + " já foi aberta anteriormente.");
        }

        Duration duracao = (request != null && request.duracao() != null)
                ? request.duracao()
                : sessaoProperties.duracaoPadrao();

        if (duracao.isNegative()) {
            throw new RegraDeNegocioException("A duração da sessão não pode ser negativa.");
        }

        Instant abertaEm = clock.instant();
        pauta.abrirSessao(abertaEm, abertaEm.plus(duracao));

        return pauta;
    }

    @Transactional(readOnly = true)
    public ResultadoVotacaoResponse apurar(Long pautaId) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pauta " + pautaId + " não encontrada."));

        Map<OpcaoVoto, Long> contagem = votoRepository.contarPorOpcao(pautaId).stream()
                .collect(Collectors.toMap(VotoRepository.ContagemVoto::getOpcao, VotoRepository.ContagemVoto::getTotal));
        long votosSim = contagem.getOrDefault(OpcaoVoto.SIM, 0L);
        long votosNao = contagem.getOrDefault(OpcaoVoto.NAO, 0L);

        Instant agora = clock.instant();
        StatusSessao status = StatusSessao.derivar(pauta.getSessaoAbertaEm(), pauta.getSessaoFechaEm(), agora);

        return new ResultadoVotacaoResponse(
                pauta.getId(),
                pauta.getTitulo(),
                status,
                votosSim + votosNao,
                votosSim,
                votosNao,
                Resultado.apurar(votosSim, votosNao),
                status != StatusSessao.FECHADA,
                agora);
    }
}
