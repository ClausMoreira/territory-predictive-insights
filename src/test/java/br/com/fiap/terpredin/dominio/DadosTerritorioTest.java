package br.com.fiap.terpredin.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.fiap.terpredin.excecao.DadosInvalidosException;
import br.com.fiap.terpredin.servico.ConfiguracaoSistema;

class DadosTerritorioTest {

    private static final String CODIGO = "teste123";

    private DadosTerritorio territorio;
    private ConfiguracaoSistema configuracao;

    @BeforeEach
    void prepararTerritorio() {
        configuracao = ConfiguracaoSistema.getInstancia();
        configuracao.restaurarPadroes();
        territorio = new DadosTerritorio("Zona Leste", 250_000, 12, CODIGO);
    }

    @AfterEach
    void restaurarLimiares() {
        configuracao.restaurarPadroes();
    }

    @Test
    void deveLancarDadosInvalidosQuandoPopulacaoForNegativa() {
        // Arrange
        int populacaoNegativa = -10;

        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> territorio.setPopulacaoAtendida(populacaoNegativa));
        assertEquals(250_000, territorio.getPopulacaoAtendida());
    }

    @Test
    void deveRecusarAlteracaoDeLimiarComCodigoIncorreto() {
        // Arrange
        double limiarOriginal = configuracao.getLimiar(TipoSensor.CARGA_REDE, NivelAlerta.ALERTA);

        // Act
        assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiar("errado", TipoSensor.CARGA_REDE, NivelAlerta.ALERTA, 88.0));

        // Assert
        assertEquals(limiarOriginal, configuracao.getLimiar(TipoSensor.CARGA_REDE, NivelAlerta.ALERTA), 0.001);
    }

    @Test
    void deveAlterarLimiarQuandoCodigoEstaCorreto() {
        // Arrange
        double novoLimiar = 88.0;

        // Act
        territorio.alterarLimiar(CODIGO, TipoSensor.CARGA_REDE, NivelAlerta.ALERTA, novoLimiar);

        // Assert
        assertEquals(novoLimiar, configuracao.getLimiar(TipoSensor.CARGA_REDE, NivelAlerta.ALERTA), 0.001);
    }

    @Test
    void deveRecusarLimiarQueQuebraAOrdemEntreOsNiveis() {
        // Arrange: ATENÇÃO de carga acima do ALERTA (90%) quebra a ordem
        double atencaoAcimaDoAlerta = 95.0;

        // Act + Assert
        assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiar(CODIGO, TipoSensor.CARGA_REDE, NivelAlerta.ATENCAO, atencaoAcimaDoAlerta));
    }
}
