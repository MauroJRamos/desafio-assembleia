package br.com.mauroramos.assembleia.tela;

import java.util.Map;

public record BotaoTela(String texto, String url, Map<String, Object> body) {
}
