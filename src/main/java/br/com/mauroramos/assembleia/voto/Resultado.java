package br.com.mauroramos.assembleia.voto;

public enum Resultado {
    APROVADA,
    REJEITADA,
    EMPATE;

    public static Resultado apurar(long votosSim, long votosNao) {
        if (votosSim > votosNao) {
            return APROVADA;
        }
        if (votosSim < votosNao) {
            return REJEITADA;
        }
        return EMPATE;
    }
}
