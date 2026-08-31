package br.com.mauroramos.assembleia.voto;

import br.com.mauroramos.assembleia.pauta.Pauta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DataJpaTest
class VotoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VotoRepository votoRepository;

    private Pauta criarPautaPersistida() {
        Pauta pauta = new Pauta("Aprovação do orçamento 2026", "Votação do orçamento anual", Instant.now());
        return entityManager.persistAndFlush(pauta);
    }

    @Test
    void deveSalvarERecuperarVotoComCamposMapeadosCorretamente() {
        Pauta pauta = criarPautaPersistida();
        Instant registradoEm = Instant.parse("2026-08-27T20:21:10Z");
        Voto voto = new Voto(pauta, "12345678901", OpcaoVoto.SIM, registradoEm);

        Voto salvo = votoRepository.saveAndFlush(voto);
        entityManager.clear();

        Voto recuperado = votoRepository.findById(salvo.getId()).orElseThrow();

        assertThat(recuperado.getId()).isNotNull();
        assertThat(recuperado.getPauta().getId()).isEqualTo(pauta.getId());
        assertThat(recuperado.getAssociadoId()).isEqualTo("12345678901");
        assertThat(recuperado.getOpcao()).isEqualTo(OpcaoVoto.SIM);
        assertThat(recuperado.getRegistradoEm()).isEqualTo(registradoEm);
    }

    @Test
    void naoDevePermitirDoisVotosDoMesmoAssociadoNaMesmaPauta() {
        Pauta pauta = criarPautaPersistida();
        votoRepository.saveAndFlush(new Voto(pauta, "12345678901", OpcaoVoto.SIM, Instant.now()));

        Voto votoDuplicado = new Voto(pauta, "12345678901", OpcaoVoto.NAO, Instant.now());

        assertThatExceptionOfType(DataIntegrityViolationException.class)
                .isThrownBy(() -> votoRepository.saveAndFlush(votoDuplicado));
    }

    @Test
    void devePermitirMesmoAssociadoVotarEmPautasDiferentes() {
        Pauta pautaA = criarPautaPersistida();
        Pauta pautaB = criarPautaPersistida();

        votoRepository.saveAndFlush(new Voto(pautaA, "12345678901", OpcaoVoto.SIM, Instant.now()));
        Voto votoNaOutraPauta = votoRepository.saveAndFlush(
                new Voto(pautaB, "12345678901", OpcaoVoto.NAO, Instant.now()));

        assertThat(votoNaOutraPauta.getId()).isNotNull();
    }

    @Test
    void existsByPautaIdAndAssociadoIdDeveRefletirVotoJaRegistrado() {
        Pauta pauta = criarPautaPersistida();
        votoRepository.saveAndFlush(new Voto(pauta, "12345678901", OpcaoVoto.SIM, Instant.now()));

        assertThat(votoRepository.existsByPauta_IdAndAssociadoId(pauta.getId(), "12345678901")).isTrue();
        assertThat(votoRepository.existsByPauta_IdAndAssociadoId(pauta.getId(), "00000000000")).isFalse();
    }

    @Test
    void contarPorOpcaoDeveAgruparVotosDaPauta() {
        Pauta pauta = criarPautaPersistida();
        votoRepository.saveAndFlush(new Voto(pauta, "111", OpcaoVoto.SIM, Instant.now()));
        votoRepository.saveAndFlush(new Voto(pauta, "222", OpcaoVoto.SIM, Instant.now()));
        votoRepository.saveAndFlush(new Voto(pauta, "333", OpcaoVoto.NAO, Instant.now()));

        Map<OpcaoVoto, Long> contagem = votoRepository.contarPorOpcao(pauta.getId()).stream()
                .collect(Collectors.toMap(VotoRepository.ContagemVoto::getOpcao, VotoRepository.ContagemVoto::getTotal));

        assertThat(contagem).hasSize(2);
        assertThat(contagem.get(OpcaoVoto.SIM)).isEqualTo(2L);
        assertThat(contagem.get(OpcaoVoto.NAO)).isEqualTo(1L);
    }

    @Test
    void contarPorOpcaoDeveVirVazioQuandoPautaSemVotos() {
        Pauta pauta = criarPautaPersistida();

        assertThat(votoRepository.contarPorOpcao(pauta.getId())).isEmpty();
    }
}
