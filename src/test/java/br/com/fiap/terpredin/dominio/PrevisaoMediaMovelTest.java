package br.com.fiap.terpredin.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.fiap.terpredin.excecao.DadosInvalidosException;

class PrevisaoMediaMovelTest {

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
    void deveCalcularAMediaApenasDasUltimasLeiturasDaJanela() {
        // Arrange: as duas primeiras leituras (70 e 80) ficam fora da janela de 5
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(5);
        List<Medicao> historico = historicoDeCarga(70, 80, 90, 100, 60, 70, 80);

        // Act
        double valorPrevisto = previsao.prever(historico);

        // Assert: (90 + 100 + 60 + 70 + 80) / 5 = 80
        assertEquals(80.0, valorPrevisto, 0.001);
    }

    @Test
    void deveLancarDadosInvalidosQuandoHaMenosDeCincoLeituras() {
        // Arrange
        PrevisaoMediaMovel previsao = new PrevisaoMediaMovel(5);
        List<Medicao> historicoCurto = historicoDeCarga(85, 87, 90, 92);

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> previsao.prever(historicoCurto));

        // Assert
        assertTrue(erro.getMessage().contains("Histórico insuficiente"));
    }

    @Test
    void deveRecusarJanelaMenorQueDuasLeituras() {
        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> new PrevisaoMediaMovel(1));
    }
}
