package br.com.mauroramos.assembleia.voto;

import br.com.mauroramos.assembleia.common.error.AssociadoNaoHabilitadoException;
import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.IntegracaoIndisponivelException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.pauta.Pauta;
import br.com.mauroramos.assembleia.voto.dto.RegistrarVotoRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VotoController.class)
class VotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VotoService votoService;

    private Voto votoComId(long id) {
        Pauta pauta = new Pauta("Título", "descrição", Instant.parse("2026-08-27T20:15:00Z"));
        ReflectionTestUtils.setField(pauta, "id", 1L);
        Voto voto = new Voto(pauta, "12345678901", OpcaoVoto.SIM, Instant.parse("2026-08-27T20:22:00Z"));
        ReflectionTestUtils.setField(voto, "id", id);
        return voto;
    }

    @Test
    void deveRegistrarVotoERetornar201() throws Exception {
        when(votoService.registrar(eq(1L), any())).thenReturn(votoComId(987L));

        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(987))
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.voto").value("SIM"));
    }

    @Test
    void deveRetornar400QuandoAssociadoIdEmBranco() throws Exception {
        RegistrarVotoRequest request = new RegistrarVotoRequest(" ", OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("associadoId"));
    }

    @Test
    void deveRetornar404QuandoPautaNaoExiste() throws Exception {
        when(votoService.registrar(eq(99L), any()))
                .thenThrow(new RecursoNaoEncontradoException("Pauta 99 não encontrada."));

        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/99/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409QuandoAssociadoJaVotou() throws Exception {
        when(votoService.registrar(eq(1L), any()))
                .thenThrow(new ConflitoDeEstadoException("Associado já votou."));

        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar403QuandoAssociadoNaoHabilitado() throws Exception {
        when(votoService.registrar(eq(1L), any()))
                .thenThrow(new AssociadoNaoHabilitadoException("Associado não habilitado a votar."));

        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar503QuandoIntegracaoIndisponivel() throws Exception {
        when(votoService.registrar(eq(1L), any()))
                .thenThrow(new IntegracaoIndisponivelException("Serviço de elegibilidade indisponível."));

        RegistrarVotoRequest request = new RegistrarVotoRequest("12345678901", OpcaoVoto.SIM);

        mockMvc.perform(post("/api/v1/pautas/1/votos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable());
    }
}
