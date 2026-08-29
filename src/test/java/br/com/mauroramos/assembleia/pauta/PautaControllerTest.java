package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PautaController.class)
class PautaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PautaService pautaService;

    @Test
    void deveCadastrarPautaERetornar201ComLocation() throws Exception {
        Pauta pauta = new Pauta(
                "Aprovação do orçamento 2026",
                "Votação do orçamento anual",
                Instant.parse("2026-08-27T20:15:30Z"));
        ReflectionTestUtils.setField(pauta, "id", 1L);
        when(pautaService.cadastrar(any())).thenReturn(pauta);

        CriarPautaRequest request = new CriarPautaRequest(
                "Aprovação do orçamento 2026", "Votação do orçamento anual");

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/pautas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Aprovação do orçamento 2026"))
                .andExpect(jsonPath("$.status").value("SEM_SESSAO"));
    }

    @Test
    void deveRetornar400QuandoTituloEmBranco() throws Exception {
        CriarPautaRequest request = new CriarPautaRequest(" ", "descrição válida");

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("titulo"));
    }

    @Test
    void deveRetornar400QuandoDescricaoUltrapassaLimite() throws Exception {
        CriarPautaRequest request = new CriarPautaRequest("Título válido", "x".repeat(501));

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("descricao"));
    }
}
