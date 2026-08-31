package br.com.mauroramos.assembleia.common.observabilidade;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void deveGerarCorrelationIdQuandoHeaderAusente() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pautas");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] correlationIdDuranteChain = new String[1];

        filter.doFilter(request, response, (req, res) -> correlationIdDuranteChain[0] = MDC.get(CorrelationIdFilter.MDC_KEY));

        assertThat(correlationIdDuranteChain[0]).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo(correlationIdDuranteChain[0]);
        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void devePropagarCorrelationIdRecebidoNoHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/pautas");
        request.addHeader(CorrelationIdFilter.HEADER, "id-do-cliente-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] correlationIdDuranteChain = new String[1];

        filter.doFilter(request, response, (req, res) -> correlationIdDuranteChain[0] = MDC.get(CorrelationIdFilter.MDC_KEY));

        assertThat(correlationIdDuranteChain[0]).isEqualTo("id-do-cliente-123");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("id-do-cliente-123");
    }
}
