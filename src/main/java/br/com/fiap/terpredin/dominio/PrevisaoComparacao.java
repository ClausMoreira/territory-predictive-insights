package br.com.fiap.terpredin.dominio;

import java.util.List;

/**
 * Compara a média do período mais recente com a do período anterior e projeta
 * a mesma variação para a próxima leitura. Captura tendência de alta ou de queda,
 * o que a média móvel sozinha não mostra.
 *
 * Com histórico curto, o tamanho de cada período é reduzido para caber nas
 * leituras disponíveis (metade do histórico, no máximo a janela configurada).
 */
public class PrevisaoComparacao extends EstrategiaPrevisao {

    private double ultimaMediaRecente;
    private double ultimaMediaAnterior;

    public PrevisaoComparacao(int janela) {
        super("Comparação com período anterior", janela);
    }

    @Override
    public double prever(List<Medicao> historico) {
        super.prever(historico); // reaproveita a validação do mínimo de leituras
        int tamanhoPeriodo = Math.min(getJanela(), historico.size() / 2);
        int fim = historico.size();

        List<Medicao> periodoRecente = historico.subList(fim - tamanhoPeriodo, fim);
        List<Medicao> periodoAnterior = historico.subList(fim - 2 * tamanhoPeriodo, fim - tamanhoPeriodo);

        ultimaMediaRecente = media(periodoRecente);
        ultimaMediaAnterior = media(periodoAnterior);
        double variacao = ultimaMediaRecente - ultimaMediaAnterior;
        return ultimaMediaRecente + variacao;
    }

    @Override
    public String explicar(double valorPrevisto, TipoSensor tipo) {
        double variacao = ultimaMediaRecente - ultimaMediaAnterior;
        String tendencia = variacao > 0 ? "alta" : (variacao < 0 ? "queda" : "estabilidade");
        return "A média recente (" + tipo.formatar(ultimaMediaRecente) + ") comparada ao período anterior ("
                + tipo.formatar(ultimaMediaAnterior) + ") indica tendência de " + tendencia
                + ". Se a variação se repetir, a próxima medição deve chegar a " + tipo.formatar(valorPrevisto) + ".";
    }
}
