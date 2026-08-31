package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.common.config.SessaoProperties;
import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.common.error.RegraDeNegocioException;
import br.com.mauroramos.assembleia.pauta.dto.AbrirSessaoRequest;
import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.sessao.StatusSessao;
import br.com.mauroramos.assembleia.tela.TelaResponse;
import br.com.mauroramos.assembleia.voto.OpcaoVoto;
import br.com.mauroramos.assembleia.voto.Resultado;
import br.com.mauroramos.assembleia.voto.VotoRepository;
import br.com.mauroramos.assembleia.voto.dto.ResultadoVotacaoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PautaService {

    private static final Logger log = LoggerFactory.getLogger(PautaService.class);

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
        pauta = pautaRepository.save(pauta);
        log.info("Pauta cadastrada: id={}", pauta.getId());
        return pauta;
    }

    @Transactional
    public Pauta abrirSessao(Long pautaId, AbrirSessaoRequest request) {
        Pauta pauta = buscarPautaOuFalhar(pautaId);

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
        Instant fechaEm = abertaEm.plus(duracao);
        pauta.abrirSessao(abertaEm, fechaEm);
        log.info("Sessão aberta: pautaId={}, fechaEm={}", pautaId, fechaEm);

        return pauta;
    }

    @Transactional(readOnly = true)
    public ResultadoVotacaoResponse apurar(Long pautaId) {
        Pauta pauta = buscarPautaOuFalhar(pautaId);

        Map<OpcaoVoto, Long> contagem = votoRepository.contarPorOpcao(pautaId).stream()
                .collect(Collectors.toMap(VotoRepository.ContagemVoto::getOpcao, VotoRepository.ContagemVoto::getTotal));
        long votosSim = contagem.getOrDefault(OpcaoVoto.SIM, 0L);
        long votosNao = contagem.getOrDefault(OpcaoVoto.NAO, 0L);

        Instant agora = clock.instant();
        StatusSessao status = StatusSessao.derivar(pauta.getSessaoAbertaEm(), pauta.getSessaoFechaEm(), agora);

        Resultado resultado = Resultado.apurar(votosSim, votosNao);
        log.info("Resultado apurado: pautaId={}, status={}, resultado={}", pautaId, status, resultado);

        return new ResultadoVotacaoResponse(
                pauta.getId(),
                pauta.getTitulo(),
                status,
                votosSim + votosNao,
                votosSim,
                votosNao,
                resultado,
                status != StatusSessao.FECHADA,
                agora);
    }

    @Transactional(readOnly = true)
    public TelaResponse telaVotacao(Long pautaId) {
        return PautaMapper.toTelaVotacao(buscarPautaOuFalhar(pautaId));
    }

    private Pauta buscarPautaOuFalhar(Long pautaId) {
        return pautaRepository.findById(pautaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pauta " + pautaId + " não encontrada."));
    }
}
