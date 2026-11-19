package br.com.fiap.terpredin.dominio;

import java.util.List;

/**
 * Prevê a próxima leitura como a média das últimas N medições do histórico.
 */
public class PrevisaoMediaMovel extends EstrategiaPrevisao {

    public PrevisaoMediaMovel(int janela) {
        super("Média móvel", janela);
    }

    @Override
    public double prever(List<Medicao> historico) {
        super.prever(historico); // reaproveita a validação do mínimo de leituras
        return media(ultimas(historico, getJanela()));
    }

    @Override
    public String explicar(double valorPrevisto, TipoSensor tipo) {
        return "Pela média das últimas " + getJanela() + " leituras, a próxima medição de "
                + tipo.getDescricao().toLowerCase() + " deve ficar em torno de " + tipo.formatar(valorPrevisto)
                + ", se o comportamento recente se mantiver.";
    }
}
