package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.pauta.dto.AbrirSessaoRequest;
import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.pauta.dto.PautaResponse;
import br.com.mauroramos.assembleia.pauta.dto.SessaoResponse;
import br.com.mauroramos.assembleia.voto.dto.ResultadoVotacaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas")
@Tag(name = "Pautas", description = "Cadastro, sessão de votação e apuração de pautas de assembleia")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova pauta")
    public ResponseEntity<PautaResponse> cadastrar(
            @Valid @RequestBody CriarPautaRequest request,
            UriComponentsBuilder uriBuilder) {
        Pauta pauta = pautaService.cadastrar(request);

        URI location = uriBuilder.path("/api/v1/pautas/{id}").buildAndExpand(pauta.getId()).toUri();
        return ResponseEntity.created(location).body(PautaMapper.toResponse(pauta));
    }

    @PostMapping("/{id}/sessao")
    @Operation(summary = "Abre a sessão de votação de uma pauta",
            description = "Duração opcional (ISO-8601, ex.: PT5M); sem informar, usa a duração default configurada.")
    public ResponseEntity<SessaoResponse> abrirSessao(
            @PathVariable Long id,
            @RequestBody(required = false) AbrirSessaoRequest request) {
        Pauta pauta = pautaService.abrirSessao(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PautaMapper.toSessaoResponse(pauta));
    }

    @GetMapping("/{id}/resultado")
    @Operation(summary = "Apura o resultado da votação de uma pauta",
            description = "Disponível em qualquer estado da sessão; parcial=true enquanto a sessão não fechou.")
    public ResponseEntity<ResultadoVotacaoResponse> apurar(@PathVariable Long id) {
        return ResponseEntity.ok(pautaService.apurar(id));
    }
}
