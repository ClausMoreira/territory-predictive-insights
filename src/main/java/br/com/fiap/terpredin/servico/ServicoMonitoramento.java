package br.com.fiap.terpredin.servico;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import br.com.fiap.terpredin.dominio.Alerta;
import br.com.fiap.terpredin.dominio.Medicao;
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

    public ServicoMonitoramento(MedicaoRepository medicaoRepository, AlertaRepository alertaRepository) {
        this.medicaoRepository = medicaoRepository;
        this.alertaRepository = alertaRepository;
    }

    /**
     * Lê o sensor, valida o valor e grava a medição no banco.
     */
    public Medicao registrarLeitura(Sensor sensor) throws FonteIndisponivelException {
        if (sensor == null) {
            throw new DadosInvalidosException("Informe o sensor que deve ser lido.");
        }
        double valor = sensor.lerValor();
        Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valor, LocalDateTime.now());
        return medicaoRepository.save(medicao);
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