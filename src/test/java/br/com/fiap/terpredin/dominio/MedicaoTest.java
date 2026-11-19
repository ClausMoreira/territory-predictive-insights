package br.com.fiap.terpredin.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.fiap.terpredin.excecao.DadosInvalidosException;

class MedicaoTest {

    @Test
    void deveCriarMedicaoValidaComOsDadosInformados() {
        // Arrange
        LocalDateTime agora = LocalDateTime.of(2026, 11, 19, 14, 0);

        // Act
        Medicao medicao = new Medicao("TA-LESTE-01", TipoSensor.TEMPERATURA_AMBIENTE, 34.5, agora);

        // Assert
        assertEquals("TA-LESTE-01", medicao.getSensorId());
        assertEquals(TipoSensor.TEMPERATURA_AMBIENTE, medicao.getTipoSensor());
        assertEquals(34.5, medicao.getValor(), 0.001);
        assertEquals(agora, medicao.getDataHora());
    }

    @Test
    void deveLancarDadosInvalidosQuandoValorEstaForaDaFaixaDoSensor() {
        // Arrange
        double temperaturaImpossivel = 80.0;
        LocalDateTime agora = LocalDateTime.now();

        // Act
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class,
                () -> new Medicao("TA-LESTE-01", TipoSensor.TEMPERATURA_AMBIENTE, temperaturaImpossivel, agora));

        // Assert
        assertTrue(erro.getMessage().contains("fora de faixa"));
    }

    @Test
    void deveLancarDadosInvalidosQuandoCargaDaRedeForNegativa() {
        // Arrange
        double cargaNegativa = -5.0;
        LocalDateTime agora = LocalDateTime.now();

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao("CR-LESTE-01", TipoSensor.CARGA_REDE, cargaNegativa, agora));
    }

    @Test
    void deveLancarDadosInvalidosQuandoSensorNaoTemIdentificador() {
        // Arrange
        String idVazio = " ";
        LocalDateTime agora = LocalDateTime.now();

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao(idVazio, TipoSensor.CARGA_REDE, 85.0, agora));
    }
}
