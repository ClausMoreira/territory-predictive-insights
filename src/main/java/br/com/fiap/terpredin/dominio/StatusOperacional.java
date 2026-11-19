package br.com.fiap.terpredin.dominio;

/**
 * Situação operacional de um componente do território.
 */
public enum StatusOperacional {

    NAO_VERIFICADO("Não verificado"),
    NORMAL("Normal: todos os sensores respondendo"),
    DEGRADADO("Degradado: parte dos sensores sem resposta"),
    INDISPONIVEL("Indisponível: nenhum sensor respondendo");

    private final String descricao;

    StatusOperacional(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}