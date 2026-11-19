package br.com.fiap.terpredin.dominio;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import br.com.fiap.terpredin.excecao.DadosInvalidosException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Uma leitura de sensor gravada no histórico. Só nasce válida:
 * o construtor rejeita valores fora da faixa do tipo de sensor.
 */
@Entity
@Table(name = "medicao")
public class Medicao {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sensorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSensor tipoSensor;

    @Column(nullable = false)
    private double valor;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    /** Exigido pelo JPA. Não use no código da aplicação. */
    protected Medicao() {
    }

    public Medicao(String sensorId, TipoSensor tipoSensor, double valor, LocalDateTime dataHora) {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("A medição precisa indicar o sensor de origem.");
        }
        if (tipoSensor == null) {
            throw new DadosInvalidosException("A medição precisa indicar o tipo de sensor.");
        }
        if (!tipoSensor.aceita(valor)) {
            throw new DadosInvalidosException("Medição fora de faixa: " + tipoSensor.formatar(valor)
                    + " não é um valor possível para " + tipoSensor.getDescricao()
                    + " (faixa aceita: " + tipoSensor.formatar(tipoSensor.getMinimo())
                    + " a " + tipoSensor.formatar(tipoSensor.getMaximo()) + ").");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("A medição precisa de data e hora.");
        }
        this.sensorId = sensorId;
        this.tipoSensor = tipoSensor;
        this.valor = valor;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public TipoSensor getTipoSensor() {
        return tipoSensor;
    }

    public double getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return "[" + dataHora.format(FORMATO) + "] " + sensorId + " | "
                + tipoSensor.getDescricao() + ": " + tipoSensor.formatar(valor);
    }
}