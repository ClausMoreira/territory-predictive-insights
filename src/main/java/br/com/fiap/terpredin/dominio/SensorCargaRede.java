package br.com.fiap.terpredin.dominio;

import java.util.Random;

/**
 * Carga da subestação em % da capacidade nominal, entre 60% e 110%.
 */
public class SensorCargaRede extends SensorSimulado {

    private static final double MINIMO_SIMULADO = 60.0;
    private static final double MAXIMO_SIMULADO = 110.0;

    public SensorCargaRede(String id) {
        super(id, new Random(), TAXA_FALHA_PADRAO);
    }

    /** Construtor usado nos testes, com gerador e taxa de falha controlados. */
    public SensorCargaRede(String id, Random random, double taxaFalha) {
        super(id, random, taxaFalha);
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.CARGA_REDE;
    }

    @Override
    protected double gerarValor() {
        return sortearEntre(MINIMO_SIMULADO, MAXIMO_SIMULADO);
    }
}