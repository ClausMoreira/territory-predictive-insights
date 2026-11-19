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
 * Alerta emitido quando uma previsão atinge um limiar.
 * Guarda o porquê: qual sensor, valor previsto, limite estourado e estratégia usada.
 */
@Entity
@Table(name = "alerta")
public class Alerta {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sensorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSensor tipoSensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAlerta nivel;

    @Column(nullable = false)
    private double valorPrevisto;

    @Column(nullable = false)
    private double limiteEstourado;

    @Column(nullable = false)
    private String estrategia;

    @Column(nullable = false, length = 600)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    /** Exigido pelo JPA. Não use no código da aplicação. */
    protected Alerta() {
    }

    public Alerta(String sensorId, TipoSensor tipoSensor, NivelAlerta nivel, double valorPrevisto,
                  double limiteEstourado, String estrategia, LocalDateTime dataHora) {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("O alerta precisa indicar o sensor de origem.");
        }
        if (tipoSensor == null || nivel == null) {
            throw new DadosInvalidosException("O alerta precisa do tipo de sensor e do nível.");
        }
        if (estrategia == null || estrategia.isBlank()) {
            throw new DadosInvalidosException("O alerta precisa indicar a estratégia de previsão usada.");
        }
        if (dataHora == null) {
            throw new DadosInvalidosException("O alerta precisa de data e hora.");
        }
        this.sensorId = sensorId;
        this.tipoSensor = tipoSensor;
        this.nivel = nivel;
        this.valorPrevisto = valorPrevisto;
        this.limiteEstourado = limiteEstourado;
        this.estrategia = estrategia;
        this.dataHora = dataHora;
        this.motivo = montarMotivo();
    }

    private String montarMotivo() {
        return "Sensor " + sensorId + " (" + tipoSensor.getDescricao() + "): a previsão de "
                + tipoSensor.formatar(valorPrevisto) + " pela estratégia " + estrategia
                + " atinge o limite de " + nivel.getRotulo() + " (" + tipoSensor.formatar(limiteEstourado)
                + "). Sugestão para a equipe: " + nivel.getOrientacao();
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

    public NivelAlerta getNivel() {
        return nivel;
    }

    public double getValorPrevisto() {
        return valorPrevisto;
    }

    public double getLimiteEstourado() {
        return limiteEstourado;
    }

    public String getEstrategia() {
        return estrategia;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return "[" + dataHora.format(FORMATO) + "] " + nivel.getRotulo() + " | " + motivo;
    }
}
