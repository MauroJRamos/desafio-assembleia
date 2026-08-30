package br.com.mauroramos.assembleia.voto;

import br.com.mauroramos.assembleia.voto.dto.RegistrarVotoRequest;
import br.com.mauroramos.assembleia.voto.dto.VotoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pautas/{pautaId}/votos")
@Tag(name = "Votos", description = "Registro de voto de associado numa pauta")
public class VotoController {

    private final VotoService votoService;

    public VotoController(VotoService votoService) {
        this.votoService = votoService;
    }

    @PostMapping
    @Operation(summary = "Registra o voto de um associado",
            description = "Único voto por associado por pauta; a sessão precisa estar aberta.")
    public ResponseEntity<VotoResponse> registrar(
            @PathVariable Long pautaId,
            @Valid @RequestBody RegistrarVotoRequest request) {
        Voto voto = votoService.registrar(pautaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(VotoMapper.toResponse(voto));
    }
}
