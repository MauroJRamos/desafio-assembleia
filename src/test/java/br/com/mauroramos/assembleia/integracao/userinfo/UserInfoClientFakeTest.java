package br.com.mauroramos.assembleia.integracao.userinfo;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoClientFakeTest {

    private final UserInfoClientFake client = new UserInfoClientFake();

    @Test
    void deveSortearAmbosOsStatusEmVariasChamadas() {
        Set<StatusElegibilidade> observados = EnumSet.noneOf(StatusElegibilidade.class);

        for (int i = 0; i < 200; i++) {
            observados.add(client.consultar("12345678901"));
        }

        assertThat(observados).containsExactlyInAnyOrder(StatusElegibilidade.values());
    }
}
