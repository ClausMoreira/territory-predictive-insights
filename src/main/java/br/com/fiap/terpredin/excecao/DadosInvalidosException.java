package br.com.fiap.terpredin.excecao;
/**
 * Lançada quando um dado do território violar uma regra da validação:
 * medição fora da faixa do sensor, população negativa, limiar inválido
 * ou histórico insuficiente para uma previsão confiável.
 *
 * RuntimeException porque representa entrada incorreta,
 * detectada no ponto de validação e capturada (no menu com mensagem clara).
 */
public class DadosInvalidosException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}