package br.com.mauroramos.assembleia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Ponto de entrada da aplicacao.
 *
 * <p>A API expoe as operacoes de gestao de pautas e sessoes de votacao descritas no
 * desafio tecnico. As camadas de dominio (controller / service / repository) sao
 * adicionadas de forma incremental nas etapas seguintes &mdash; ver {@code docs/ANALISE-ARQUITETURA.md}.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AssembleiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssembleiaApplication.class, args);
    }
}
