package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.tela.TelaResponse;
import br.com.mauroramos.assembleia.tela.TipoTela;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PautaMapperTest {

    @Test
    void toTelaVotacaoDeveMontarFormularioComBotoesSimNao() {
        Pauta pauta = new Pauta("Aprovação do orçamento 2026", "Votação do orçamento anual", Instant.now());
        ReflectionTestUtils.setField(pauta, "id", 1L);

        TelaResponse tela = PautaMapper.toTelaVotacao(pauta);

        assertThat(tela.tipo()).isEqualTo(TipoTela.FORMULARIO);
        assertThat(tela.titulo()).isEqualTo("Aprovação do orçamento 2026");
        assertThat(tela.itens()).hasSize(2);
        assertThat(tela.itens().get(0).texto()).isEqualTo("Votação do orçamento anual");
        assertThat(tela.itens().get(1).tipo()).isEqualTo("INPUT_TEXTO");
        assertThat(tela.itens().get(1).chave()).isEqualTo("associadoId");

        assertThat(tela.botoes()).hasSize(2);
        assertThat(tela.botoes()).allSatisfy(botao -> assertThat(botao.url()).isEqualTo("/api/v1/pautas/1/votos"));
        assertThat(tela.botoes().get(0).body()).containsEntry("voto", "SIM");
        assertThat(tela.botoes().get(1).body()).containsEntry("voto", "NAO");
    }

    @Test
    void toTelaVotacaoDeveUsarTituloComoTextoQuandoDescricaoAusente() {
        Pauta pauta = new Pauta("Título sem descrição", null, Instant.now());
        ReflectionTestUtils.setField(pauta, "id", 2L);

        TelaResponse tela = PautaMapper.toTelaVotacao(pauta);

        assertThat(tela.itens().get(0).texto()).isEqualTo("Título sem descrição");
    }
}
