package br.com.mauroramos.assembleia;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: garante que o contexto Spring sobe com a configuracao atual.
 */
@SpringBootTest
class AssembleiaApplicationTests {

    @Test
    void contextLoads() {
        // Intencionalmente vazio: falha se o ApplicationContext nao inicializar.
    }
}
