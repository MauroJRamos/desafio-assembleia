package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.common.config.SessaoProperties;
import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.common.error.RegraDeNegocioException;
import br.com.mauroramos.assembleia.pauta.dto.AbrirSessaoRequest;
import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.voto.OpcaoVoto;
import br.com.mauroramos.assembleia.voto.Resultado;
import br.com.mauroramos.assembleia.voto.VotoRepository;
import br.com.mauroramos.assembleia.voto.dto.ResultadoVotacaoResponse;
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
import java.util.List;
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

    @Mock
    private VotoRepository votoRepository;

    private PautaService pautaService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
        pautaService = new PautaService(
                pautaRepository, votoRepository, clock, new SessaoProperties(Duration.ofMinutes(1)));
    }

    private Pauta pautaPersistidaComId(long id) {
        Pauta pauta = new Pauta("Titulo", "descricao", AGORA);
        ReflectionTestUtils.setField(pauta, "id", id);
        return pauta;
    }

    private VotoRepository.ContagemVoto contagem(OpcaoVoto opcao, long total) {
        return new VotoRepository.ContagemVoto() {
            @Override
            public OpcaoVoto getOpcao() {
                return opcao;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
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
    void deveLancarRegraDeNegocioQuandoDuracaoNegativa() {
        Pauta pauta = pautaPersistidaComId(1L);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));

        assertThatExceptionOfType(RegraDeNegocioException.class)
                .isThrownBy(() -> pautaService.abrirSessao(1L, new AbrirSessaoRequest(Duration.ofMinutes(-1))));
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

    @Test
    void deveApurarComoParcialEZeradoQuandoSemSessao() {
        Pauta pauta = pautaPersistidaComId(1L);
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.contarPorOpcao(1L)).thenReturn(List.of());

        ResultadoVotacaoResponse resultado = pautaService.apurar(1L);

        assertThat(resultado.totalVotos()).isZero();
        assertThat(resultado.parcial()).isTrue();
        assertThat(resultado.resultado()).isEqualTo(Resultado.EMPATE);
    }

    @Test
    void deveApurarComoFinalQuandoSessaoFechada() {
        Pauta pauta = pautaPersistidaComId(1L);
        pauta.abrirSessao(AGORA.minusSeconds(120), AGORA.minusSeconds(60));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.contarPorOpcao(1L)).thenReturn(
                List.of(contagem(OpcaoVoto.SIM, 8), contagem(OpcaoVoto.NAO, 3)));

        ResultadoVotacaoResponse resultado = pautaService.apurar(1L);

        assertThat(resultado.totalVotos()).isEqualTo(11);
        assertThat(resultado.votosSim()).isEqualTo(8);
        assertThat(resultado.votosNao()).isEqualTo(3);
        assertThat(resultado.resultado()).isEqualTo(Resultado.APROVADA);
        assertThat(resultado.parcial()).isFalse();
    }

    @Test
    void deveApurarComoParcialQuandoSessaoAindaAberta() {
        Pauta pauta = pautaPersistidaComId(1L);
        pauta.abrirSessao(AGORA.minusSeconds(30), AGORA.plusSeconds(30));
        when(pautaRepository.findById(1L)).thenReturn(Optional.of(pauta));
        when(votoRepository.contarPorOpcao(1L)).thenReturn(List.of(contagem(OpcaoVoto.NAO, 2)));

        ResultadoVotacaoResponse resultado = pautaService.apurar(1L);

        assertThat(resultado.parcial()).isTrue();
        assertThat(resultado.resultado()).isEqualTo(Resultado.REJEITADA);
    }

    @Test
    void deveLancarNaoEncontradoAoApurarPautaInexistente() {
        when(pautaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(RecursoNaoEncontradoException.class)
                .isThrownBy(() -> pautaService.apurar(99L));
    }
}
