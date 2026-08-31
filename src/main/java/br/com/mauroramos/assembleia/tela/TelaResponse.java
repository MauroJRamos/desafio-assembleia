package br.com.mauroramos.assembleia.tela;

import java.util.List;

public record TelaResponse(
        TipoTela tipo,
        String titulo,
        List<ItemTela> itens,
        List<BotaoTela> botoes
) {
}
