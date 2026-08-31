package br.com.mauroramos.assembleia.pauta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "pauta")
public class Pauta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private Instant criadaEm;

    @Column(name = "sessao_aberta_em")
    private Instant sessaoAbertaEm;

    @Column(name = "sessao_fecha_em")
    private Instant sessaoFechaEm;

    protected Pauta() {
        // uso exclusivo do JPA
    }

    public Pauta(String titulo, String descricao, Instant criadaEm) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.criadaEm = criadaEm;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public Instant getCriadaEm() {
        return criadaEm;
    }

    public Instant getSessaoAbertaEm() {
        return sessaoAbertaEm;
    }

    public Instant getSessaoFechaEm() {
        return sessaoFechaEm;
    }

    public void abrirSessao(Instant abertaEm, Instant fechaEm) {
        this.sessaoAbertaEm = abertaEm;
        this.sessaoFechaEm = fechaEm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pauta pauta)) return false;
        return id != null && id.equals(pauta.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
