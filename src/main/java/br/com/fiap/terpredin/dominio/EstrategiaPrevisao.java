package br.com.fiap.terpredin.dominio;
import java.util.List;
import br.com.fiap.terpredin.excecao.DadosInvalidosException;

/**
 * Base das estratégias de previsão.
 *
 * O prever() desta classe valida o histórico e devolve a previsão mais simples
 * possível: o último valor lido. As filhas sobrescrevem prever(), chamam
 * super.prever() para reaproveitar a validação e aplicam o próprio cálculo.
 */
public abstract class EstrategiaPrevisao {

    /** Abaixo disso o sistema para: poucas leituras não vão sustentar uma previsão. */
    public static final int MINIMO_LEITURAS = 5;

    private final String nome;
    private final int janela;

    protected EstrategiaPrevisao(String nome, int janela) {
        if (nome == null || nome.isBlank()) {
            throw new DadosInvalidosException("A estratégia de previsão precisa de um nome.");
        }
        if (janela < 2) {
            throw new DadosInvalidosException("A janela de previsão deve ter ao menos 2 leituras (informado: "
                    + janela + ").");
        }
        this.nome = nome;
        this.janela = janela;
    }

    /**
     * @param historico medições de 1 sensor, em ordem cronológica, começando pela mais antiga primeiro
     * @return valor previsto para a próxima leitura
     */
    public double prever(List<Medicao> historico) {
        int quantidade = (historico == null) ? 0 : historico.size();
        if (quantidade < MINIMO_LEITURAS) {
            throw new DadosInvalidosException("Histórico insuficiente: " + quantidade
                    + " leitura(s) no banco. São necessárias pelo menos " + MINIMO_LEITURAS
                    + " para uma previsão confiável.");
        }
        return historico.get(quantidade - 1).getValor();
    }

    /** Explicação simples do que o valor previsto significa. */
    public abstract String explicar(double valorPrevisto, TipoSensor tipo);

    /** Últimas N medições do histórico em ordem cronológiac. */
    protected List<Medicao> ultimas(List<Medicao> historico, int quantidade) {
        int inicio = Math.max(0, historico.size() - quantidade);
        return historico.subList(inicio, historico.size());
    }

    /** Média simples dos valores de uma lista de medições. */
    protected double media(List<Medicao> medicoes) {
        return medicoes.stream().mapToDouble(Medicao::getValor).average().orElse(0.0);
    }

    public String getNome() {
        return nome;
    }

    public int getJanela() {
        return janela;
    }
}
