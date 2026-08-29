package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.common.config.SessaoProperties;
import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.pauta.dto.AbrirSessaoRequest;
import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PautaServiceTest {

    private static final Instant AGORA = Instant.parse("2026-08-27T20:20:00Z");

    @Mock
    private PautaRepository pautaRepository;

    private PautaService pautaService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
        pautaService = new PautaService(pautaRepository, clock, new SessaoProperties(Duration.ofMinutes(1)));
    }

    private Pauta pautaPersistidaComId(long id) {
        Pauta pauta = new Pauta("Titulo", "descricao", AGORA);
        ReflectionTestUtils.setField(pauta, "id", id);
        return pauta;
    }

    @Test
    void deveCadastrarPautaComInstanteDoClock() {
        when(pautaRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        Pauta pauta = pautaService.cadastrar(new CriarPautaRequest("Titulo válido", "descricao"));

        assertThat(pauta.getCriadaEm()).isEqualTo(AGORA);
    }

    @Test
    void deveAbrirSessaoComDuracaoInformada() {
        Pauta pauta = pautaPersistidaComId(1L);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        Pauta atualizada = pautaService.abrirSessao(1L, new AbrirSessaoRequest(Duration.ofMinutes(5)));

        assertThat(atualizada.getSessaoAbertaEm()).isEqualTo(AGORA);
        assertThat(atualizada.getSessaoFechaEm()).isEqualTo(AGORA.plus(Duration.ofMinutes(5)));
    }

    @Test
    void deveUsarDuracaoPadraoQuandoRequestNaoInformaDuracao() {
        Pauta pauta = pautaPersistidaComId(1L);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        Pauta atualizada = pautaService.abrirSessao(1L, null);

        assertThat(atualizada.getSessaoFechaEm()).isEqualTo(AGORA.plus(Duration.ofMinutes(1)));
    }

    @Test
    void deveLancarNaoEncontradoQuandoPautaNaoExiste() {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(RecursoNaoEncontradoException.class)
                .isThrownBy(() -> pautaService.abrirSessao(99L, null));
    }

    @Test
    void deveLancarConflitoQuandoSessaoJaFoiAberta() {
        Pauta pauta = pautaPersistidaComId(1L);
        pauta.abrirSessao(AGORA, AGORA.plusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        assertThatExceptionOfType(ConflitoDeEstadoException.class)
                .isThrownBy(() -> pautaService.abrirSessao(1L, null));
    }
}
