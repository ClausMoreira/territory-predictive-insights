package br.com.fiap.terpredin.dominio;

import java.util.Objects;
import java.util.Random;

import br.com.fiap.terpredin.excecao.DadosInvalidosException;
import br.com.fiap.terpredin.excecao.FonteIndisponivelException;

/**
 * Base comum dos sensores simulados: guarda o id, sorteia falhas e
 * delega às filhas a geração de um valor realista para cada tipo.
 */
public abstract class SensorSimulado implements Sensor {

    /** Chance de 5% de o sensor não responder em uma leitura. */
    protected static final double TAXA_FALHA_PADRAO = 0.05;

    private final String id;
    private final double taxaFalha;
    private final Random random;

    /**
     * @param id        identificador do sensor
     * @param random    gerador de números (permite semente fixa nos testes)
     * @param taxaFalha chance de falha entre 0 (nunca falha) e 1 (sempre falha)
     */
    protected SensorSimulado(String id, Random random, double taxaFalha) {
        if (id == null || id.isBlank()) {
            throw new DadosInvalidosException("O sensor precisa de um identificador.");
        }
        if (taxaFalha < 0 || taxaFalha > 1) {
            throw new DadosInvalidosException("A taxa de falha do sensor deve estar entre 0 e 1.");
        }
        this.id = id;
        this.random = Objects.requireNonNull(random, "O gerador de números não pode ser nulo.");
        this.taxaFalha = taxaFalha;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean verificarFuncionamento() {
        return random.nextDouble() >= taxaFalha;
    }

    @Override
    public double lerValor() throws FonteIndisponivelException {
        if (!verificarFuncionamento()) {
            throw new FonteIndisponivelException("O sensor " + id + " (" + getTipo().getDescricao()
                    + ") não respondeu à leitura. Verifique a conexão do equipamento.");
        }
        return Math.round(gerarValor() * 10.0) / 10.0;
    }

    /** Sorteia um valor uniforme entre min e max. */
    protected double sortearEntre(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    /** Cada tipo de sensor gera valores na sua faixa realista. */
    protected abstract double gerarValor();

    @Override
    public String toString() {
        return id + " (" + getTipo().getDescricao() + ")";
    }
}