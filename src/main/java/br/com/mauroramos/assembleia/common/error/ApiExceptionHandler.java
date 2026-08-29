package br.com.mauroramos.assembleia.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;

// Cobre por enquanto só Bean Validation; catálogo de exceções de domínio entra na etapa 6.
@RestControllerAdvice
public class ApiExceptionHandler {

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

    private record CampoInvalido(String campo, String mensagem) {
    }
}
