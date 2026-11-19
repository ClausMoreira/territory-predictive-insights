package br.com.fiap.terpredin.excecao;
/**
 * Lançada quando um dado do território viola uma regra de validação:
 * medição fora da faixa do sensor, população negativa, limiar inválido
 * ou histórico insuficiente para uma previsão confiável.
 *
 * É unchecked (RuntimeException) porque representa entrada incorreta,
 * detectada no ponto de validação e capturada no menu com mensagem clara.
 */
public class DadosInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}