package br.com.fiap.terpredin.servico;

/**
 * Estratégias de previsão disponíveis. A FabricaPrevisao sabe criar cada uma.
 */
public enum TipoPrevisao {

    MEDIA_MOVEL("Média móvel"),
    COMPARACAO("Comparação com período anterior");

    private final String descricao;

    TipoPrevisao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
