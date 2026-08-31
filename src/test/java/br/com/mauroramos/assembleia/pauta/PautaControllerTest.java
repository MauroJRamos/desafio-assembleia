package br.com.mauroramos.assembleia.pauta;

import br.com.mauroramos.assembleia.common.error.ConflitoDeEstadoException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import br.com.mauroramos.assembleia.common.error.RegraDeNegocioException;
import br.com.mauroramos.assembleia.pauta.dto.CriarPautaRequest;
import br.com.mauroramos.assembleia.sessao.StatusSessao;
import br.com.mauroramos.assembleia.tela.BotaoTela;
import br.com.mauroramos.assembleia.tela.ItemTela;
import br.com.mauroramos.assembleia.tela.TelaResponse;
import br.com.mauroramos.assembleia.tela.TipoTela;
import br.com.mauroramos.assembleia.voto.Resultado;
import br.com.mauroramos.assembleia.voto.dto.ResultadoVotacaoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void deveAbrirSessaoERetornar201ComStatusAberta() throws Exception {
        Pauta pauta = new Pauta("Título", "descrição", Instant.parse("2026-08-27T20:15:00Z"));
        ReflectionTestUtils.setField(pauta, "id", 1L);
        pauta.abrirSessao(Instant.parse("2026-08-27T20:20:00Z"), Instant.parse("2026-08-27T20:21:00Z"));
        when(pautaService.abrirSessao(eq(1L), any())).thenReturn(pauta);

        mockMvc.perform(post("/api/v1/pautas/1/sessao"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pautaId").value(1))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.fechaEm").value("2026-08-27T20:21:00Z"));
    }

    @Test
    void deveRetornar404QuandoPautaNaoExisteAoAbrirSessao() throws Exception {
        when(pautaService.abrirSessao(eq(99L), any()))
                .thenThrow(new RecursoNaoEncontradoException("Pauta 99 não encontrada."));

        mockMvc.perform(post("/api/v1/pautas/99/sessao"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar409QuandoSessaoJaFoiAberta() throws Exception {
        when(pautaService.abrirSessao(eq(1L), any()))
                .thenThrow(new ConflitoDeEstadoException("Sessão já aberta."));

        mockMvc.perform(post("/api/v1/pautas/1/sessao"))
                .andExpect(status().isConflict());
    }

    @Test
    void deveApurarResultadoERetornar200() throws Exception {
        ResultadoVotacaoResponse resultado = new ResultadoVotacaoResponse(
                1L, "Título", StatusSessao.FECHADA, 11, 8, 3,
                Resultado.APROVADA, false, Instant.parse("2026-08-27T20:26:00Z"));
        when(pautaService.apurar(1L)).thenReturn(resultado);

        mockMvc.perform(get("/api/v1/pautas/1/resultado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotos").value(11))
                .andExpect(jsonPath("$.resultado").value("APROVADA"))
                .andExpect(jsonPath("$.parcial").value(false));
    }

    @Test
    void deveRetornar404QuandoPautaNaoExisteAoApurar() throws Exception {
        when(pautaService.apurar(99L))
                .thenThrow(new RecursoNaoEncontradoException("Pauta 99 não encontrada."));

        mockMvc.perform(get("/api/v1/pautas/99/resultado"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar422QuandoRegraDeNegocioViolada() throws Exception {
        when(pautaService.abrirSessao(eq(1L), any()))
                .thenThrow(new RegraDeNegocioException("A duração da sessão não pode ser negativa."));

        mockMvc.perform(post("/api/v1/pautas/1/sessao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"duracao\":\"-PT1M\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornar409QuandoViolacaoDeIntegridade() throws Exception {
        when(pautaService.cadastrar(any())).thenThrow(new DataIntegrityViolationException("duplicado"));

        CriarPautaRequest request = new CriarPautaRequest("Título válido", "descrição");

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar400QuandoCorpoMalFormado() throws Exception {
        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ nao-e-json-valido"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar405QuandoMetodoNaoSuportado() throws Exception {
        mockMvc.perform(delete("/api/v1/pautas"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void deveRetornar500QuandoErroInesperado() throws Exception {
        when(pautaService.cadastrar(any())).thenThrow(new IllegalStateException("falha inesperada"));

        CriarPautaRequest request = new CriarPautaRequest("Título válido", "descrição");

        mockMvc.perform(post("/api/v1/pautas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deveRetornarTelaDeVotacaoERetornar200() throws Exception {
        TelaResponse tela = new TelaResponse(
                TipoTela.FORMULARIO,
                "Título",
                List.of(ItemTela.texto("descrição"), ItemTela.inputTexto("Identificação", "associadoId")),
                List.of(
                        new BotaoTela("Sim", "/api/v1/pautas/1/votos", Map.of("voto", "SIM")),
                        new BotaoTela("Não", "/api/v1/pautas/1/votos", Map.of("voto", "NAO"))));
        when(pautaService.telaVotacao(1L)).thenReturn(tela);

        mockMvc.perform(get("/api/v1/pautas/1/tela-votacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("FORMULARIO"))
                .andExpect(jsonPath("$.botoes[0].url").value("/api/v1/pautas/1/votos"))
                .andExpect(jsonPath("$.botoes[0].body.voto").value("SIM"));
    }

    @Test
    void deveRetornar404QuandoPautaNaoExisteAoBuscarTelaDeVotacao() throws Exception {
        when(pautaService.telaVotacao(99L))
                .thenThrow(new RecursoNaoEncontradoException("Pauta 99 não encontrada."));

        mockMvc.perform(get("/api/v1/pautas/99/tela-votacao"))
                .andExpect(status().isNotFound());
    }
}
