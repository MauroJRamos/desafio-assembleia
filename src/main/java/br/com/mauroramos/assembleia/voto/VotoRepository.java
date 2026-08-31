package br.com.mauroramos.assembleia.voto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsByPauta_IdAndAssociadoId(Long pautaId, String associadoId);

    @Query("select v.opcao as opcao, count(v) as total from Voto v where v.pauta.id = :pautaId group by v.opcao")
    List<ContagemVoto> contarPorOpcao(@Param("pautaId") Long pautaId);

    interface ContagemVoto {
        OpcaoVoto getOpcao();

        long getTotal();
    }
}
