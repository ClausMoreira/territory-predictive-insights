package br.com.fiap.terpredin.servico;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import br.com.fiap.terpredin.dominio.NivelAlerta;
import br.com.fiap.terpredin.dominio.TipoSensor;
import br.com.fiap.terpredin.excecao.DadosInvalidosException;

/**
 * Singleton: única instância que guarda os limiares de alerta e a janela de previsão.
 */
public final class ConfiguracaoSistema {

    private static ConfiguracaoSistema instancia;

    private final Map<TipoSensor, EnumMap<NivelAlerta, Double>> limiares = new EnumMap<>(TipoSensor.class);
    private int janelaPrevisao;

    private ConfiguracaoSistema() {
        restaurarPadroes();
    }

    public static synchronized ConfiguracaoSistema getInstancia() {
        if (instancia == null) {
            instancia = new ConfiguracaoSistema();
        }
        return instancia;
    }

    /** Volta aos limiares iniciais da equipe. Também usado nos testes. */
    public void restaurarPadroes() {
        registrar(TipoSensor.TEMPERATURA_AMBIENTE, 32.0, 35.0, 38.0);
        registrar(TipoSensor.CARGA_REDE, 80.0, 90.0, 100.0);
        registrar(TipoSensor.TEMPERATURA_TRANSFORMADOR, 75.0, 85.0, 95.0);
        janelaPrevisao = 5;
    }

    private void registrar(TipoSensor tipo, double atencao, double alerta, double critico) {
        EnumMap<NivelAlerta, Double> valores = new EnumMap<>(NivelAlerta.class);
        valores.put(NivelAlerta.ATENCAO, atencao);
        valores.put(NivelAlerta.ALERTA, alerta);
        valores.put(NivelAlerta.CRITICO, critico);
        limiares.put(tipo, valores);
    }

    public double getLimiar(TipoSensor tipo, NivelAlerta nivel) {
        return limiares.get(tipo).get(nivel);
    }

    public Map<NivelAlerta, Double> getLimiares(TipoSensor tipo) {
        return Collections.unmodifiableMap(limiares.get(tipo));
    }

    /**
     * Altera um limiar, garantindo que ele esteja na faixa do sensor
     * e que a ordem ATENÇÃO < ALERTA < CRÍTICO continue valida.
     */
    public void definirLimiar(TipoSensor tipo, NivelAlerta nivel, double valor) {
        if (tipo == null || nivel == null) {
            throw new DadosInvalidosException("Informe o tipo de sensor e o nível do limiar.");
        }
        if (!tipo.aceita(valor)) {
            throw new DadosInvalidosException("Limiar inválido: " + tipo.formatar(valor)
                    + " está fora da faixa possível para " + tipo.getDescricao() + ".");
        }
        EnumMap<NivelAlerta, Double> novos = new EnumMap<>(limiares.get(tipo));
        novos.put(nivel, valor);
        double atencao = novos.get(NivelAlerta.ATENCAO);
        double alerta = novos.get(NivelAlerta.ALERTA);
        double critico = novos.get(NivelAlerta.CRITICO);
        if (!(atencao < alerta && alerta < critico)) {
            throw new DadosInvalidosException("Limiar inválido para " + tipo.getDescricao()
                    + ": é preciso manter ATENÇÃO < ALERTA < CRÍTICO (ficaria "
                    + tipo.formatar(atencao) + " / " + tipo.formatar(alerta) + " / " + tipo.formatar(critico) + ").");
        }
        limiares.put(tipo, novos);
    }

    /**
     * Compara um valor previsto com os limiares e devolve o nível mais grave atingido,
     * ou vazio se o valor estiver abaixo de todos.
     */
    public Optional<NivelAlerta> classificar(TipoSensor tipo, double valorPrevisto) {
        NivelAlerta atingido = null;
        for (NivelAlerta nivel : NivelAlerta.values()) {
            if (valorPrevisto >= getLimiar(tipo, nivel)) {
                atingido = nivel;
            }
        }
        return Optional.ofNullable(atingido);
    }

    public int getJanelaPrevisao() {
        return janelaPrevisao;
    }

    public void setJanelaPrevisao(int janelaPrevisao) {
        if (janelaPrevisao < 2 || janelaPrevisao > 50) {
            throw new DadosInvalidosException("A janela de previsão deve ficar entre 2 e 50 leituras (informado: "
                    + janelaPrevisao + ").");
        }
        this.janelaPrevisao = janelaPrevisao;
    }
}
