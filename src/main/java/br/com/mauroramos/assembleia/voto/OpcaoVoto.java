package br.com.mauroramos.assembleia.voto;

public enum OpcaoVoto {
    SIM("Sim"),
    NAO("Não");

    private final String descricao;

    OpcaoVoto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
