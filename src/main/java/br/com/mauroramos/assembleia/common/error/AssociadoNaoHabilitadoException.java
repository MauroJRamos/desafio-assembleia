package br.com.mauroramos.assembleia.common.error;

public class AssociadoNaoHabilitadoException extends RuntimeException {

    public AssociadoNaoHabilitadoException(String mensagem) {
        super(mensagem);
    }
}
