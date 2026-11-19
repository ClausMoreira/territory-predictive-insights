package br.com.fiap.terpredin.dominio;

import java.util.Random;

/**
 * Temperatura do ar na região, entre 26 e 40 °C em cenário de onda de calor.
 */
public class SensorTemperaturaAmbiente extends SensorSimulado {

    private static final double MINIMO_SIMULADO = 26.0;
    private static final double MAXIMO_SIMULADO = 40.0;

    public SensorTemperaturaAmbiente(String id) {
        super(id, new Random(), TAXA_FALHA_PADRAO);
    }

    /** Construtor usado nos testes, com gerador e taxa de falha controlados. */
    public SensorTemperaturaAmbiente(String id, Random random, double taxaFalha) {
        super(id, random, taxaFalha);
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.TEMPERATURA_AMBIENTE;
    }

    @Override
    protected double gerarValor() {
        return sortearEntre(MINIMO_SIMULADO, MAXIMO_SIMULADO);
    }
}