package br.com.fiap.terpredin.dominio;

import br.com.fiap.terpredin.excecao.DadosInvalidosException;

/**
 * Qualquer elemento físico monitorado no território
 * (subestação, transformador, equipamento público).
 */
public abstract class ComponenteTerritorial {

    private final String id;
    private String nome;
    private String regiao;
    private StatusOperacional status;

    protected ComponenteTerritorial(String id, String nome, String regiao) {
        this.id = exigirTexto(id, "id");
        setNome(nome);
        setRegiao(regiao);
        this.status = StatusOperacional.NAO_VERIFICADO;
    }

    /** Cada tipo de componente decide como avaliar a própria situação. */
    public abstract StatusOperacional verificarStatus();

    protected void setStatus(StatusOperacional status) {
        if (status == null) {
            throw new DadosInvalidosException("O status do componente não pode ser nulo.");
        }
        this.status = status;
    }

    public final void setNome(String nome) {
        this.nome = exigirTexto(nome, "nome");
    }

    public final void setRegiao(String regiao) {
        this.regiao = exigirTexto(regiao, "região");
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException("O campo " + campo + " do componente territorial é obrigatório.");
        }
        return valor.trim();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public StatusOperacional getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return nome + " [" + id + "] - " + regiao + " - " + status.getDescricao();
    }
}