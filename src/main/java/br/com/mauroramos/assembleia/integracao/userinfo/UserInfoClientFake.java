package br.com.mauroramos.assembleia.integracao.userinfo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

// Servico real (user-info.herokuapp.com) foi descontinuado (Heroku removeu o free tier);
// esta e a implementacao default (fake=true no application.yml).
@Component
@ConditionalOnProperty(
        prefix = "assembleia.integracao.user-info", name = "fake", havingValue = "true", matchIfMissing = true)
public class UserInfoClientFake implements UserInfoClient {

    @Override
    public StatusElegibilidade consultar(String cpf) {
        return ThreadLocalRandom.current().nextBoolean()
                ? StatusElegibilidade.ABLE_TO_VOTE
                : StatusElegibilidade.UNABLE_TO_VOTE;
    }
}
