package br.com.mauroramos.assembleia.voto;

import br.com.mauroramos.assembleia.pauta.Pauta;
import br.com.mauroramos.assembleia.voto.dto.RegistrarVotoRequest;
import br.com.mauroramos.assembleia.voto.dto.VotoResponse;

import java.time.Instant;

public final class VotoMapper {

    private VotoMapper() {
    }

    public static Voto toEntity(Pauta pauta, RegistrarVotoRequest request, Instant registradoEm) {
        return new Voto(pauta, request.associadoId(), request.voto(), registradoEm);
    }

    public static VotoResponse toResponse(Voto voto) {
        return new VotoResponse(
                voto.getId(),
                voto.getPauta().getId(),
                voto.getAssociadoId(),
                voto.getOpcao(),
                voto.getRegistradoEm());
    }
}
