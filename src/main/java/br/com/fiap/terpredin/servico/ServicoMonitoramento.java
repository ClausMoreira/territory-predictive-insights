package br.com.fiap.terpredin.servico;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.fiap.terpredin.dominio.Alerta;
import br.com.fiap.terpredin.dominio.EstrategiaPrevisao;
import br.com.fiap.terpredin.dominio.Medicao;
import br.com.fiap.terpredin.dominio.NivelAlerta;
import br.com.fiap.terpredin.dominio.Sensor;
import br.com.fiap.terpredin.excecao.DadosInvalidosException;
import br.com.fiap.terpredin.excecao.FonteIndisponivelException;
import br.com.fiap.terpredin.persistencia.AlertaRepository;
import br.com.fiap.terpredin.persistencia.MedicaoRepository;

/**
 * Regras de negócio do monitoramento: grava leituras, consulta o histórico
 * e transforma previsões em alertas.
 * Os repositories chegam pelo construtor, o que permite trocá-los por mocks nos testes.
 */
@Service
public class ServicoMonitoramento {

    private final MedicaoRepository medicaoRepository;
    private final AlertaRepository alertaRepository;
    private final ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();

    public ServicoMonitoramento(MedicaoRepository medicaoRepository, AlertaRepository alertaRepository) {
        this.medicaoRepository = medicaoRepository;
        this.alertaRepository = alertaRepository;
    }

    /**
     * Lê o sensor, valida o valor (o construtor de Medicao rejeita valores fora de faixa)
     * e grava a medição no banco.
     */
    public Medicao registrarLeitura(Sensor sensor) throws FonteIndisponivelException {
        if (sensor == null) {
            throw new DadosInvalidosException("Informe o sensor que deve ser lido.");
        }
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valor, LocalDateTime.now());
        return medicaoRepository.save(medicao);
    }

    /**
     * Roda a estratégia sobre o histórico do banco, compara o valor previsto com os
     * limiares do ConfiguracaoSistema e grava um alerta quando algum nível é atingido.
     * Com menos leituras que o mínimo, o sistema se cala em vez de arriscar um palpite.
     */
    public ResultadoPrevisao gerarPrevisao(Sensor sensor, EstrategiaPrevisao estrategia) {
        if (sensor == null || estrategia == null) {
            throw new DadosInvalidosException("Informe o sensor e a estratégia de previsão.");
        }
        List<Medicao> historico = buscarHistoricoPorSensor(sensor.getId());

        if (historico.size() < EstrategiaPrevisao.MINIMO_LEITURAS) {
            return ResultadoPrevisao.semPrevisao(sensor.getId(), sensor.getTipo(), estrategia.getNome(),
                    historico.size(), "o sistema se cala porque há só " + historico.size()
                            + " leitura(s) no banco e são necessárias pelo menos "
                            + EstrategiaPrevisao.MINIMO_LEITURAS + " para uma previsão confiável.");
        }

        double valorPrevisto = estrategia.prever(historico);
        String explicacao = estrategia.explicar(valorPrevisto, sensor.getTipo());
        Optional<NivelAlerta> nivelAtingido = configuracao.classificar(sensor.getTipo(), valorPrevisto);

        Alerta alerta = null;
        if (nivelAtingido.isPresent()) {
            NivelAlerta nivel = nivelAtingido.get();
            double limite = configuracao.getLimiar(sensor.getTipo(), nivel);
            alerta = alertaRepository.save(new Alerta(sensor.getId(), sensor.getTipo(), nivel, valorPrevisto,
                    limite, estrategia.getNome(), LocalDateTime.now()));
        }
        return new ResultadoPrevisao(sensor.getId(), sensor.getTipo(), estrategia.getNome(), historico.size(),
                valorPrevisto, explicacao, alerta);
    }

    /** Histórico gravado de um sensor, em ordem cronológica. */
    public List<Medicao> buscarHistoricoPorSensor(String sensorId) {
        return medicaoRepository.findBySensorIdOrderByDataHoraAscIdAsc(sensorId);
    }

    /** Alertas já emitidos, do mais recente para o mais antigo. */
    public List<Alerta> listarAlertas() {
        return alertaRepository.findAllByOrderByDataHoraDescIdDesc();
    }

    public long contarMedicoes() {
        return medicaoRepository.count();
    }

    public long contarAlertas() {
        return alertaRepository.count();
    }
}
