package br.com.mauroramos.assembleia.common.error;

public class IntegracaoIndisponivelException extends RuntimeException {

    public IntegracaoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
