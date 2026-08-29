package br.com.mauroramos.assembleia.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "assembleia.sessao")
public record SessaoProperties(Duration duracaoPadrao) {
}
