package br.com.mauroramos.assembleia.pauta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PautaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PautaRepository pautaRepository;

    @Test
    void deveSalvarERecuperarPautaComCamposMapeadosCorretamente() {
        Instant criadaEm = Instant.parse("2026-08-27T20:15:30Z");
        Pauta pauta = new Pauta("Aprovação do orçamento 2026", "Votação do orçamento anual", criadaEm);

        Pauta salva = pautaRepository.saveAndFlush(pauta);
        entityManager.clear();

        Pauta recuperada = pautaRepository.findById(salva.getId()).orElseThrow();

        assertThat(recuperada.getId()).isNotNull();
        assertThat(recuperada.getTitulo()).isEqualTo("Aprovação do orçamento 2026");
        assertThat(recuperada.getDescricao()).isEqualTo("Votação do orçamento anual");
        assertThat(recuperada.getCriadaEm()).isEqualTo(criadaEm);
        assertThat(recuperada.getSessaoAbertaEm()).isNull();
        assertThat(recuperada.getSessaoFechaEm()).isNull();
    }

    @Test
    void deveAceitarDescricaoNula() {
        Pauta pauta = new Pauta("Sem descrição", null, Instant.now());

        Pauta salva = pautaRepository.saveAndFlush(pauta);

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getDescricao()).isNull();
    }
}
