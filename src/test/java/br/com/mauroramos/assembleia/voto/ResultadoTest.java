package br.com.mauroramos.assembleia.voto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultadoTest {

    @Test
    void aprovadaQuandoSimMaiorQueNao() {
        assertThat(Resultado.apurar(8, 3)).isEqualTo(Resultado.APROVADA);
    }

    @Test
    void rejeitadaQuandoNaoMaiorQueSim() {
        assertThat(Resultado.apurar(2, 5)).isEqualTo(Resultado.REJEITADA);
    }

    @Test
    void empateQuandoIguais() {
        assertThat(Resultado.apurar(4, 4)).isEqualTo(Resultado.EMPATE);
        assertThat(Resultado.apurar(0, 0)).isEqualTo(Resultado.EMPATE);
    }
}
