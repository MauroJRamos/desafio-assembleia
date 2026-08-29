package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.common.config.SessaoProperties;
import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.pauta.dto.AbrirSessaoRequest;
import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class PautaService {

    private final PautaRepository pautaRepository;
    private final Clock clock;
    private final SessaoProperties sessaoProperties;

    public PautaService(PautaRepository pautaRepository, Clock clock, SessaoProperties sessaoProperties) {
        this.pautaRepository = pautaRepository;
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

        Instant abertaEm = clock.instant();
        pauta.abrirSessao(abertaEm, abertaEm.plus(duracao));

        return pauta;
    }
}
