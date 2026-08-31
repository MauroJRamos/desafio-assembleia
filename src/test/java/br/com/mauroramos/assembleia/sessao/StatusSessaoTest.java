package br.com.mauroramos.assembleia.sessao;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class StatusSessaoTest {

    @Test
    void semSessaoQuandoNuncaAbriu() {
        StatusSessao status = StatusSessao.derivar(null, null, Instant.now());

        assertThat(status).isEqualTo(StatusSessao.SEM_SESSAO);
    }

    @Test
    void abertaQuandoAgoraAntesDoFechamento() {
        Instant abertura = Instant.parse("2026-08-27T20:20:00Z");
        Instant fechamento = Instant.parse("2026-08-27T20:25:00Z");
        Instant agora = Instant.parse("2026-08-27T20:22:00Z");

        StatusSessao status = StatusSessao.derivar(abertura, fechamento, agora);

        assertThat(status).isEqualTo(StatusSessao.ABERTA);
    }

    @Test
    void fechadaQuandoAgoraIgualOuAposFechamento() {
        Instant abertura = Instant.parse("2026-08-27T20:20:00Z");
        Instant fechamento = Instant.parse("2026-08-27T20:25:00Z");

        assertThat(StatusSessao.derivar(abertura, fechamento, fechamento)).isEqualTo(StatusSessao.FECHADA);
        assertThat(StatusSessao.derivar(abertura, fechamento, fechamento.plusSeconds(1))).isEqualTo(StatusSessao.FECHADA);
    }
}
