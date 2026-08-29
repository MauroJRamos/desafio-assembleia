package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.pauta.dto.PautaResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/pautas")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @PostMapping
    public ResponseEntity<PautaResponse> cadastrar(
            @Valid @RequestBody CriarPautaRequest request,
            UriComponentsBuilder uriBuilder) {
        Pauta pauta = pautaService.cadastrar(request);

        URI location = uriBuilder.path("/api/v1/pautas/{id}").buildAndExpand(pauta.getId()).toUri();
        return ResponseEntity.created(location).body(PautaMapper.toResponse(pauta));
    }
}
