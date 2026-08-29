package br.com.mauroramos.assembleia.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarValidacao(MethodArgumentNotValidException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setType(URI.create("https://assembleia/errors/validacao"));
        problema.setTitle("Requisição inválida");

        List<CampoInvalido> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new CampoInvalido(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        problema.setProperty("errors", erros);

        return problema;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail tratarCorpoMalFormado(HttpMessageNotReadableException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou mal formatado.");
        problema.setType(URI.create("https://assembleia/errors/validacao"));
        problema.setTitle("Requisição inválida");
        return problema;
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarNaoEncontrado(RecursoNaoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setType(URI.create("https://assembleia/errors/recurso-nao-encontrado"));
        problema.setTitle("Recurso não encontrado");
        return problema;
    }

    @ExceptionHandler(ConflitoDeEstadoException.class)
    public ProblemDetail tratarConflito(ConflitoDeEstadoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setType(URI.create("https://assembleia/errors/conflito-de-estado"));
        problema.setTitle("Conflito de estado");
        return problema;
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ProblemDetail tratarRegraDeNegocio(RegraDeNegocioException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problema.setType(URI.create("https://assembleia/errors/regra-de-negocio"));
        problema.setTitle("Violação de regra de negócio");
        return problema;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail tratarViolacaoDeIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade de dados: {}", ex.getMessage());

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade (por exemplo, um registro duplicado).");
        problema.setType(URI.create("https://assembleia/errors/conflito-de-integridade"));
        problema.setTitle("Conflito de integridade de dados");
        return problema;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
        problema.setTitle("Método HTTP não suportado");
        return problema;
    }

    // Fallback genérico: qualquer exceção não mapeada acima vira 500 com corpo sem
    // detalhes internos; a causa completa fica só no log do servidor.
    @ExceptionHandler(Exception.class)
    public ProblemDetail tratarErroInesperado(Exception ex) {
        log.error("Erro inesperado não tratado", ex);

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        problema.setTitle("Erro interno");
        return problema;
    }

    private record CampoInvalido(String campo, String mensagem) {
    }
}
