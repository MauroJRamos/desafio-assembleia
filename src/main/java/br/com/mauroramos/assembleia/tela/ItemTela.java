package br.com.mauroramos.assembleia.tela;

public record ItemTela(String tipo, String texto, String chave) {

    public static ItemTela texto(String texto) {
        return new ItemTela("TEXTO", texto, null);
    }

    public static ItemTela inputTexto(String texto, String chave) {
        return new ItemTela("INPUT_TEXTO", texto, chave);
    }
}
