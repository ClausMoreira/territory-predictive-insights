package br.com.fiap.terpredin.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.com.fiap.terpredin.dominio.Alerta;
import br.com.fiap.terpredin.dominio.Medicao;
import br.com.fiap.terpredin.dominio.NivelAlerta;
import br.com.fiap.terpredin.dominio.TipoSensor;

class ExportadorCsvTest {

    /** Pasta temporária criada pelo JUnit e apagada ao fim de cada teste. */
    @TempDir
    Path pastaTemporaria;

    @Test
    void deveGerarCabecalhoEUmaLinhaPorMedicao() throws IOException {
        // Arrange
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 19, 14, 0);
        List<Medicao> medicoes = List.of(
                new Medicao("CR-LESTE-01", TipoSensor.CARGA_REDE, 85.5, inicio),
                new Medicao("CR-LESTE-01", TipoSensor.CARGA_REDE, 91.0, inicio.plusHours(1)));
        Path destino = pastaTemporaria.resolve("medicoes.csv");

        // Act
        ExportadorCsv.exportarMedicoes(medicoes, destino);
        List<String> linhas = Files.readAllLines(destino, StandardCharsets.UTF_8);

        // Assert
        assertEquals(3, linhas.size());
        assertEquals("data_hora;sensor;tipo;valor;unidade", linhas.get(0).replace("\uFEFF", ""));
        assertEquals("2026-11-19 14:00:00;CR-LESTE-01;Carga da rede elétrica;85,5;%", linhas.get(1));
    }

    @Test
    void deveExportarAlertasComNivelEMotivo() throws IOException {
        // Arrange
        Alerta alerta = new Alerta("TA-LESTE-01", TipoSensor.TEMPERATURA_AMBIENTE, NivelAlerta.CRITICO,
                38.4, 38.0, "Média móvel", LocalDateTime.of(2026, 11, 19, 15, 0));
        Path destino = pastaTemporaria.resolve("subpasta").resolve("alertas.csv");

        // Act
        ExportadorCsv.exportarAlertas(List.of(alerta), destino);
        List<String> linhas = Files.readAllLines(destino, StandardCharsets.UTF_8);

        // Assert: a subpasta foi criada e a linha traz nível, valor previsto e limite
        assertEquals(2, linhas.size());
        assertTrue(linhas.get(1).startsWith("2026-11-19 15:00:00;CRÍTICO;TA-LESTE-01;"));
        assertTrue(linhas.get(1).contains(";38,4;38,0;"));
    }

    @Test
    void deveColocarEntreAspasOCampoQueContemPontoEVirgula() {
        // Act
        String campo = ExportadorCsv.escapar("carga alta; avaliar remanejamento");

        // Assert
        assertEquals("\"carga alta; avaliar remanejamento\"", campo);
    }
}
