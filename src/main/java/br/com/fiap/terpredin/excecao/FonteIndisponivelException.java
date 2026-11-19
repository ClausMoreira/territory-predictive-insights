package br.com.fiap.terpredin.excecao;

/**
 * Lançada quando uma fonte de dados (sensor ou gerador de leituras) falha.
 *
 * É checked (Exception) porque falha de equipamento é algo esperado no
 * mundo real: o compilador obriga quem lê um sensor a tratar essa situação.
 */
public class FonteIndisponivelException extends Exception {

    private static final long serialVersionUID = 1L;

    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FonteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}