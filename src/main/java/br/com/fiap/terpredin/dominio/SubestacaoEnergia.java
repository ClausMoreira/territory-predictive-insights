package br.com.fiap.terpredin.dominio;

import java.util.ArrayList;
import java.util.List;

import br.com.fiap.terpredin.excecao.DadosInvalidosException;

/**
 * Subestação de energia atende a uma região e reune os sensores instalados nela.
 */
public class SubestacaoEnergia extends ComponenteTerritorial {

    private final double capacidadeMva;
    private final List<Sensor> sensores = new ArrayList<>();

    public SubestacaoEnergia(String id, String nome, String regiao, double capacidadeMva) {
        super(id, nome, regiao);
        if (capacidadeMva <= 0) {
            throw new DadosInvalidosException("A capacidade da subestação deve ser maior que zero (informado: "
                    + capacidadeMva + " MVA).");
        }
        this.capacidadeMva = capacidadeMva;
    }

    public void adicionarSensor(Sensor sensor) {
        if (sensor == null) {
            throw new DadosInvalidosException("Não é possível instalar um sensor nulo na subestação.");
        }
        sensores.add(sensor);
    }

    /**
     * NORMAL se todos os sensores respondem, 
     * DEGRADADO se só parte responde
     * INDISPONIVEL se nenhum responde (ou se não tem sensores instalados).
     */
    @Override
    public StatusOperacional verificarStatus() {
        long funcionando = sensores.stream().filter(Sensor::verificarFuncionamento).count();
        StatusOperacional novo;
        if (sensores.isEmpty() || funcionando == 0) {
            novo = StatusOperacional.INDISPONIVEL;
        } else if (funcionando < sensores.size()) {
            novo = StatusOperacional.DEGRADADO;
        } else {
            novo = StatusOperacional.NORMAL;
        }
        setStatus(novo);
        return novo;
    }

    /** Devolve uma cópia somente leitura */
    public List<Sensor> getSensores() {
        return List.copyOf(sensores);
    }

    public double getCapacidadeMva() {
        return capacidadeMva;
    }
}