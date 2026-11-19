package br.com.fiap.terpredin.dominio;

import java.util.Random;

/**
 * Temperatura do óleo do transformador, entre 55 e 100 °C.
 */
public class SensorTemperaturaTransformador extends SensorSimulado {

    private static final double MINIMO_SIMULADO = 55.0;
    private static final double MAXIMO_SIMULADO = 100.0;

    public SensorTemperaturaTransformador(String id) {
        super(id, new Random(), TAXA_FALHA_PADRAO);
    }

    /** Construtor usado nos testes, com gerador e taxa de falha controlados. */
    public SensorTemperaturaTransformador(String id, Random random, double taxaFalha) {
        super(id, random, taxaFalha);
    }

    @Override
    public TipoSensor getTipo() {
        return TipoSensor.TEMPERATURA_TRANSFORMADOR;
    }

    @Override
    protected double gerarValor() {
        return sortearEntre(MINIMO_SIMULADO, MAXIMO_SIMULADO);
    }
}