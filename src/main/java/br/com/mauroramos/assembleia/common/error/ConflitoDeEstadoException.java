package br.com.mauroramos.assembleia.common.error;

public class ConflitoDeEstadoException extends RuntimeException {

    public ConflitoDeEstadoException(String mensagem) {
        super(mensagem);
    }
}
