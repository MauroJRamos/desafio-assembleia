package br.com.mauroramos.assembleia.integracao.userinfo;

import br.com.mauroramos.assembleia.common.error.IntegracaoIndisponivelException;
import br.com.mauroramos.assembleia.common.error.RecursoNaoEncontradoException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@ConditionalOnProperty(prefix = "assembleia.integracao.user-info", name = "fake", havingValue = "false")
public class UserInfoClientReal implements UserInfoClient {

    private final RestClient restClient;

    public UserInfoClientReal(UserInfoProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(properties))
                .build();
    }

    @Override
    public StatusElegibilidade consultar(String cpf) {
        try {
            UserInfoResponse resposta = restClient.get()
                    .uri("/users/{cpf}", cpf)
                    .retrieve()
                    .body(UserInfoResponse.class);
            return resposta.status();
        } catch (HttpClientErrorException.NotFound ex) {
            throw new RecursoNaoEncontradoException("CPF " + cpf + " não encontrado no serviço de elegibilidade.");
        } catch (RestClientException ex) {
            throw new IntegracaoIndisponivelException("Serviço de elegibilidade de voto indisponível.");
        }
    }

    private static ClientHttpRequestFactory requestFactory(UserInfoProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMillis = (int) properties.timeout().toMillis();
        factory.setConnectTimeout(timeoutMillis);
        factory.setReadTimeout(timeoutMillis);
        return factory;
    }

    private record UserInfoResponse(StatusElegibilidade status) {
    }
}
