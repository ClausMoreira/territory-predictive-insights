package br.com.fiap.terpredin.servico;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntFunction;

import br.com.fiap.terpredin.dominio.EstrategiaPrevisao;
import br.com.fiap.terpredin.dominio.PrevisaoComparacao;
import br.com.fiap.terpredin.dominio.PrevisaoMediaMovel;
import br.com.fiap.terpredin.excecao.DadosInvalidosException;

/**
 * Factory: único lugar que sabe criar estratégias de previsão.
 *
 * Cada tipo fica registrado em um mapa com o construtor correspondente, então
 * escolher a estratégia é uma consulta ao mapa, sem if por tipo nem instanceof.
 * Para adicionar uma nova estratégia, basta uma linha no bloco static.
 */
public final class FabricaPrevisao {

    private static final Map<TipoPrevisao, IntFunction<EstrategiaPrevisao>> CONSTRUTORES =
            new EnumMap<>(TipoPrevisao.class);

    static {
        CONSTRUTORES.put(TipoPrevisao.MEDIA_MOVEL, PrevisaoMediaMovel::new);
        CONSTRUTORES.put(TipoPrevisao.COMPARACAO, PrevisaoComparacao::new);
    }

    private FabricaPrevisao() {
        // classe utilitária: não deve ser instanciada
    }

    /** Cria a estratégia definida como padrão no ConfiguracaoSistema. */
    public static EstrategiaPrevisao criarPadrao() {
        return criar(ConfiguracaoSistema.getInstancia().getEstrategiaPadrao());
    }

    /** Cria a estratégia pedida usando a janela definida no ConfiguracaoSistema. */
    public static EstrategiaPrevisao criar(TipoPrevisao tipo) {
        return criar(tipo, ConfiguracaoSistema.getInstancia().getJanelaPrevisao());
    }

    public static EstrategiaPrevisao criar(TipoPrevisao tipo, int janela) {
        IntFunction<EstrategiaPrevisao> construtor = (tipo == null) ? null : CONSTRUTORES.get(tipo);
        if (construtor == null) {
            throw new DadosInvalidosException("Não existe estratégia de previsão registrada para: " + tipo + ".");
        }
        return construtor.apply(janela);
    }
}
