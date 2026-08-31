package br.com.mauroramos.assembleia.integracao.userinfo;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "assembleia.integracao.user-info")
public record UserInfoProperties(String baseUrl, boolean fake, Duration timeout) {
}
