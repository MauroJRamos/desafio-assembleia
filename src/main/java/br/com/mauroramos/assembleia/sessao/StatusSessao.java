package br.com.mauroramos.assembleia.sessao;

import java.time.Instant;

public enum StatusSessao {
    SEM_SESSAO,
    ABERTA,
    FECHADA;

    public static StatusSessao derivar(Instant sessaoAbertaEm, Instant sessaoFechaEm, Instant agora) {
        if (sessaoAbertaEm == null) {
            return SEM_SESSAO;
        }
        return agora.isBefore(sessaoFechaEm) ? ABERTA : FECHADA;
    }
}
