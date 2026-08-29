package br.com.mauroramos.assembleia.voto;

import br.com.mauroramos.assembleia.pauta.Pauta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "voto",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_voto_pauta_associado",
                columnNames = {"pauta_id", "associado_id"}
        ),
        indexes = @Index(name = "ix_voto_pauta_opcao", columnList = "pauta_id, opcao")
)
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pauta_id", nullable = false)
    private Pauta pauta;

    @Column(name = "associado_id", nullable = false)
    private String associadoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private OpcaoVoto opcao;

    @Column(name = "registrado_em", nullable = false)
    private Instant registradoEm;

    protected Voto() {
        // uso exclusivo do JPA
    }

    public Voto(Pauta pauta, String associadoId, OpcaoVoto opcao, Instant registradoEm) {
        this.pauta = pauta;
        this.associadoId = associadoId;
        this.opcao = opcao;
        this.registradoEm = registradoEm;
    }

    public Long getId() {
        return id;
    }

    public Pauta getPauta() {
        return pauta;
    }

    public String getAssociadoId() {
        return associadoId;
    }

    public OpcaoVoto getOpcao() {
        return opcao;
    }

    public Instant getRegistradoEm() {
        return registradoEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Voto voto)) return false;
        return id != null && id.equals(voto.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
