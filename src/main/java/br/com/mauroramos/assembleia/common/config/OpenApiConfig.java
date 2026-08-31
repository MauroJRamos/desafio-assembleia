package br.com.mauroramos.assembleia.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Sessões de Votação em Assembleia")
                        .description("API REST para gerenciamento de sessões de votação em assembleias de cooperativas.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Mauro Ramos")
                                .email("mauroramosti@gmail.com")));
    }
}
