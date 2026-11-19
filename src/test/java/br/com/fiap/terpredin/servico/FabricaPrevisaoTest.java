package br.com.fiap.terpredin.servico;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.fiap.terpredin.dominio.EstrategiaPrevisao;
import br.com.fiap.terpredin.dominio.PrevisaoComparacao;
import br.com.fiap.terpredin.dominio.PrevisaoMediaMovel;
import br.com.fiap.terpredin.excecao.DadosInvalidosException;

class FabricaPrevisaoTest {

    private final ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();

    @BeforeEach
    void restaurarAntes() {
        configuracao.restaurarPadroes();
    }

    @AfterEach
    void restaurarDepois() {
        configuracao.restaurarPadroes();
    }

    @Test
    void deveCriarMediaMovelComAJanelaDoConfiguracaoSistema() {
        // Arrange
        configuracao.setJanelaPrevisao(7);

        // Act
        EstrategiaPrevisao estrategia = FabricaPrevisao.criar(TipoPrevisao.MEDIA_MOVEL);

        // Assert
        assertInstanceOf(PrevisaoMediaMovel.class, estrategia);
        assertEquals(7, estrategia.getJanela());
    }

    @Test
    void deveCriarAEstrategiaPadraoDefinidaNaConfiguracao() {
        // Arrange
        configuracao.setEstrategiaPadrao(TipoPrevisao.COMPARACAO);

        // Act
        EstrategiaPrevisao estrategia = FabricaPrevisao.criarPadrao();

        // Assert
        assertInstanceOf(PrevisaoComparacao.class, estrategia);
    }

    @Test
    void deveLancarDadosInvalidosQuandoOTipoNaoEhInformado() {
        // Act + Assert
        assertThrows(DadosInvalidosException.class, () -> FabricaPrevisao.criar(null));
    }
}
