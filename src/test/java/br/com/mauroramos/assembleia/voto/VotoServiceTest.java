package br.com.mauroramos.assembleia.voto;

import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.pauta.Pauta;
import br.com.mauroramos.assembleia.pauta.PautaRepository;
import br.com.mauroramos.assembleia.voto.dto.RegistrarVotoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-08-27T20:22:00Z");

    @Mock
    private VotoRepository votoRepository;

    @Mock
    private PautaRepository pautaRepository;

    private VotoService votoService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
        votoService = new VotoService(votoRepository, pautaRepository, clock);
    }

    private Pauta pautaComId(long id) {
        Pauta pauta = new Pauta("Título", "descrição", AGORA.minusSeconds(120));
        ReflectionTestUtils.setField(pauta, "id", id);
        return pauta;
    }

    @Test
    void deveRegistrarVotoQuandoSessaoAberta() {
        Pauta pauta = pautaComId(1L);
        pauta.abrirSessao(AGORA.minusSeconds(60), AGORA.plusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.existsByPauta_IdAndAssociadoId(1L, "12345678901")).thenReturn(false);
        when(votoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        Voto voto = votoService.registrar(1L, new RegistrarVotoRequest("12345678901", OpcaoVoto.SIM));

        assertThat(voto.getAssociadoId()).isEqualTo("12345678901");
        assertThat(voto.getOpcao()).isEqualTo(OpcaoVoto.SIM);
        assertThat(voto.getRegistradoEm()).isEqualTo(AGORA);
    }

    @Test
    void deveLancarNaoEncontradoQuandoPautaNaoExiste() {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(RecursoNaoEncontradoException.class)
                .isThrownBy(() -> votoService.registrar(99L, new RegistrarVotoRequest("123", OpcaoVoto.SIM)));
    }

    @Test
    void deveLancarConflitoQuandoSessaoNuncaFoiAberta() {
        Pauta pauta = pautaComId(1L);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        assertThatExceptionOfType(ConflitoDeEstadoException.class)
                .isThrownBy(() -> votoService.registrar(1L, new RegistrarVotoRequest("123", OpcaoVoto.SIM)));
    }

    @Test
    void deveLancarConflitoQuandoSessaoJaFechou() {
        Pauta pauta = pautaComId(1L);
        pauta.abrirSessao(AGORA.minusSeconds(120), AGORA.minusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        assertThatExceptionOfType(ConflitoDeEstadoException.class)
                .isThrownBy(() -> votoService.registrar(1L, new RegistrarVotoRequest("123", OpcaoVoto.SIM)));
    }

    @Test
    void deveLancarConflitoQuandoAssociadoJaVotou() {
        Pauta pauta = pautaComId(1L);
        pauta.abrirSessao(AGORA.minusSeconds(60), AGORA.plusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.existsByPauta_IdAndAssociadoId(1L, "12345678901")).thenReturn(true);

        assertThatExceptionOfType(ConflitoDeEstadoException.class)
                .isThrownBy(() -> votoService.registrar(1L, new RegistrarVotoRequest("12345678901", OpcaoVoto.NAO)));
    }
}
