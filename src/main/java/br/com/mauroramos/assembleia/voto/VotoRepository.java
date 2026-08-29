package br.com.mauroramos.assembleia.voto;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsByPauta_IdAndAssociadoId(Long pautaId, String associadoId);
}
