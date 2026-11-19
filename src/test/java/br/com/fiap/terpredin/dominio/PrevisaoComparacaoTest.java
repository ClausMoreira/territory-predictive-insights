package br.com.fiap.terpredin.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PrevisaoComparacaoTest {

    /** Cria um histórico de carga da rede com uma leitura por hora, na ordem informada. */
    private List<Medicao> historicoDeCarga(double... valores) {
        List<Medicao> historico = new ArrayList<>();
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 19, 8, 0);
        for (int i = 0; i < valores.length; i++) {
            historico.add(new Medicao("CR-LESTE-01", TipoSensor.CARGA_REDE, valores[i], inicio.plusHours(i)));
        }
        return historico;
    }

    @Test
    void deveProjetarTendenciaDeAlta() {
        // Arrange: período anterior com média 80, período recente com média 90
        PrevisaoComparacao previsao = new PrevisaoComparacao(3);
        List<Medicao> historico = historicoDeCarga(80, 80, 80, 90, 90, 90);

        // Act
        double valorPrevisto = previsao.prever(historico);

        // Assert: 90 + (90 - 80) = 100
        assertEquals(100.0, valorPrevisto, 0.001);
    }

    @Test
    void deveProjetarTendenciaDeQueda() {
        // Arrange: período anterior com média 90, período recente com média 80
        PrevisaoComparacao previsao = new PrevisaoComparacao(3);
        List<Medicao> historico = historicoDeCarga(90, 90, 90, 80, 80, 80);

        // Act
        double valorPrevisto = previsao.prever(historico);

        // Assert: 80 + (80 - 90) = 70
        assertEquals(70.0, valorPrevisto, 0.001);
    }

    @Test
    void deveReduzirOsPeriodosQuandoOHistoricoEhCurto() {
        // Arrange: janela 5, mas só 5 leituras, então cada período usa 2 leituras
        PrevisaoComparacao previsao = new PrevisaoComparacao(5);
        List<Medicao> historico = historicoDeCarga(70, 70, 80, 90, 90);

        // Act
        double valorPrevisto = previsao.prever(historico);

        // Assert: recente (90, 90) = 90; anterior (70, 80) = 75; 90 + 15 = 105
        assertEquals(105.0, valorPrevisto, 0.001);
    }

    @Test
    void deveTerComportamentoDiferenteDaMediaMovelNoMesmoHistorico() {
        // Arrange
        List<Medicao> historico = historicoDeCarga(80, 80, 80, 90, 90, 90);
        EstrategiaPrevisao mediaMovel = new PrevisaoMediaMovel(3);
        EstrategiaPrevisao comparacao = new PrevisaoComparacao(3);

        // Act
        double previstoMediaMovel = mediaMovel.prever(historico);
        double previstoComparacao = comparacao.prever(historico);

        // Assert: a média móvel repete o patamar (90); a comparação projeta a alta (100)
        assertEquals(90.0, previstoMediaMovel, 0.001);
        assertNotEquals(previstoMediaMovel, previstoComparacao, 0.001);
    }
}
