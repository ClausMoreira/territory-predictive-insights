package br.com.fiap.terpredin.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.terpredin.dominio.Alerta;
import br.com.fiap.terpredin.dominio.Medicao;
import br.com.fiap.terpredin.dominio.NivelAlerta;
import br.com.fiap.terpredin.dominio.PrevisaoMediaMovel;
import br.com.fiap.terpredin.dominio.Sensor;
import br.com.fiap.terpredin.dominio.SensorCargaRede;
import br.com.fiap.terpredin.dominio.TipoSensor;
import br.com.fiap.terpredin.excecao.FonteIndisponivelException;
import br.com.fiap.terpredin.persistencia.AlertaRepository;
import br.com.fiap.terpredin.persistencia.MedicaoRepository;

/**
 * Testa a lógica de alerta sem banco: os repositories são mocks do Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ServicoMonitoramentoTest {

    private static final String SENSOR_ID = "CR-LESTE-01";

    @Mock
    private MedicaoRepository medicaoRepository;

    @Mock
    private AlertaRepository alertaRepository;

    @InjectMocks
    private ServicoMonitoramento servico;

    private final Sensor sensorCarga = new SensorCargaRede(SENSOR_ID);
    private final PrevisaoMediaMovel mediaMovel = new PrevisaoMediaMovel(5);

    @BeforeEach
    void restaurarLimiaresAntes() {
        ConfiguracaoSistema.getInstancia().restaurarPadroes();
    }

    @AfterEach
    void restaurarLimiaresDepois() {
        ConfiguracaoSistema.getInstancia().restaurarPadroes();
    }

    /** Histórico de carga da rede com uma leitura por hora. */
    private List<Medicao> historicoDeCarga(double... valores) {
        List<Medicao> historico = new ArrayList<>();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 19, 8, 0);
        for (int i = 0; i < valores.length; i++) {
            historico.add(new Medicao(SENSOR_ID, TipoSensor.CARGA_REDE, valores[i], inicio.plusHours(i)));
        }
        return historico;
    }

    @Test
    void deveEmitirAlertaCriticoQuandoPrevisaoAtingeOLimiteCritico() {
        // Arrange: média de 98, 100, 102, 104 e 106 = 102% (CRÍTICO a partir de 100%)
        when(medicaoRepository.findBySensorIdOrderByDataHoraAscIdAsc(SENSOR_ID))
                .thenReturn(historicoDeCarga(98, 100, 102, 104, 106));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        // Act
        ResultadoPrevisao resultado = servico.gerarPrevisao(sensorCarga, mediaMovel);

        // Assert
        assertEquals(102.0, resultado.valorPrevisto(), 0.001);
        assertTrue(resultado.gerouAlerta());
        assertEquals(NivelAlerta.CRITICO, resultado.alerta().getNivel());
        assertEquals(100.0, resultado.alerta().getLimiteEstourado(), 0.001);
        verify(alertaRepository).save(any(Alerta.class));
    }

    @Test
    void deveEscolherONivelMaisGraveAtingidoSemPularParaOCritico() {
        // Arrange: média = 92% (ALERTA a partir de 90%, CRÍTICO só a partir de 100%)
        when(medicaoRepository.findBySensorIdOrderByDataHoraAscIdAsc(SENSOR_ID))
                .thenReturn(historicoDeCarga(90, 91, 92, 93, 94));
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        // Act
        ResultadoPrevisao resultado = servico.gerarPrevisao(sensorCarga, mediaMovel);

        // Assert
        assertEquals(NivelAlerta.ALERTA, resultado.alerta().getNivel());
        assertEquals(90.0, resultado.alerta().getLimiteEstourado(), 0.001);
    }

    @Test
    void naoDeveEmitirAlertaQuandoPrevisaoFicaAbaixoDeTodosOsLimiares() {
        // Arrange: média = 64% (ATENÇÃO só a partir de 80%)
        when(medicaoRepository.findBySensorIdOrderByDataHoraAscIdAsc(SENSOR_ID))
                .thenReturn(historicoDeCarga(60, 62, 64, 66, 68));

        // Act
        ResultadoPrevisao resultado = servico.gerarPrevisao(sensorCarga, mediaMovel);

        // Assert
        assertTrue(resultado.houvePrevisao());
        assertFalse(resultado.gerouAlerta());
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void deveSeCalarQuandoOBancoTemMenosDeCincoLeituras() {
        // Arrange: valores altíssimos, mas só 3 leituras
        when(medicaoRepository.findBySensorIdOrderByDataHoraAscIdAsc(SENSOR_ID))
                .thenReturn(historicoDeCarga(105, 108, 110));

        // Act
        ResultadoPrevisao resultado = servico.gerarPrevisao(sensorCarga, mediaMovel);

        // Assert
        assertFalse(resultado.houvePrevisao());
        assertFalse(resultado.gerouAlerta());
        verify(alertaRepository, never()).save(any(Alerta.class));
    }

    @Test
    void deveLancarFonteIndisponivelENaoGravarNadaQuandoOSensorFalha() {
        // Arrange: taxa de falha 1 = o sensor nunca responde
        Sensor sensorComDefeito = new SensorCargaRede("CR-DEFEITO", new Random(), 1.0);

        // Act + Assert
        assertThrows(FonteIndisponivelException.class, () -> servico.registrarLeitura(sensorComDefeito));
        verify(medicaoRepository, never()).save(any(Medicao.class));
    }
}
