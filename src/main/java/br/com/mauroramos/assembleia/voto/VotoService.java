package br.com.mauroramos.assembleia.voto;

import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.pauta.Pauta;
import br.com.mauroramos.assembleia.pauta.PautaRepository;
import br.com.mauroramos.assembleia.sessao.StatusSessao;
import br.com.mauroramos.assembleia.voto.dto.RegistrarVotoRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class VotoService {

    private final VotoRepository votoRepository;
    private final PautaRepository pautaRepository;
    private final Clock clock;

    public VotoService(VotoRepository votoRepository, PautaRepository pautaRepository, Clock clock) {
        this.votoRepository = votoRepository;
        this.pautaRepository = pautaRepository;
        this.clock = clock;
    }

    @Transactional
    public Voto registrar(Long pautaId, RegistrarVotoRequest request) {
        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pauta " + pautaId + " não encontrada."));

        StatusSessao status = StatusSessao.derivar(
                pauta.getSessaoAbertaEm(), pauta.getSessaoFechaEm(), clock.instant());
        if (status != StatusSessao.ABERTA) {
            throw new ConflitoDeEstadoException(
                    "Sessão da pauta " + pautaId + " não está aberta (status atual: " + status + ").");
        }

        // Checagem antecipada só pra dar mensagem melhor no caminho feliz; a constraint
        // unica no banco (ver Voto) é quem garante a corretude sob concorrência real (§9).
        if (votoRepository.existsByPauta_IdAndAssociadoId(pautaId, request.associadoId())) {
            throw new ConflitoDeEstadoException(
                    "Associado " + request.associadoId() + " já votou na pauta " + pautaId + ".");
        }

        Voto voto = VotoMapper.toEntity(pauta, request, clock.instant());
        return votoRepository.save(voto);
    }
}
