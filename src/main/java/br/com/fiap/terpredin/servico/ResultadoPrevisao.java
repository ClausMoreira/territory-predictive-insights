package br.com.fiap.terpredin.servico;

import br.com.fiap.terpredin.dominio.Alerta;
import br.com.fiap.terpredin.dominio.TipoSensor;

/**
 * Resultado de uma previsão para um sensor: o valor previsto, o que ele significa
 * e o alerta emitido (se houver). Quando o histórico é curto, o sistema se cala
 * e o resultado traz apenas o motivo.
 *
 * @param valorPrevisto nulo quando o sistema se calou
 * @param alerta        nulo quando nenhum limiar foi atingido
 */
public record ResultadoPrevisao(String sensorId, TipoSensor tipo, String estrategia, int leiturasNoBanco,
                                Double valorPrevisto, String explicacao, Alerta alerta) {

    public static ResultadoPrevisao semPrevisao(String sensorId, TipoSensor tipo, String estrategia,
                                                int leiturasNoBanco, String motivo) {
        return new ResultadoPrevisao(sensorId, tipo, estrategia, leiturasNoBanco, null, motivo, null);
    }

    public boolean houvePrevisao() {
        return valorPrevisto != null;
    }

    public boolean gerouAlerta() {
        return alerta != null;
    }

    /** Texto pronto para o menu: valor previsto, significado e alerta. */
    public String resumo() {
        StringBuilder texto = new StringBuilder();
        texto.append(sensorId).append(" (").append(tipo.getDescricao()).append(") | ")
                .append(leiturasNoBanco).append(" leitura(s) no banco\n");
        if (!houvePrevisao()) {
            return texto.append("  Sem previsão: ").append(explicacao).toString();
        }
        texto.append("  Previsão (").append(estrategia).append("): ").append(tipo.formatar(valorPrevisto)).append("\n");
        texto.append("  Significado: ").append(explicacao).append("\n");
        if (gerouAlerta()) {
            texto.append("  >> ").append(alerta.getNivel().getRotulo()).append(" emitido e gravado. ")
                    .append(alerta.getMotivo());
        } else {
            texto.append("  Dentro dos limites: nenhum alerta emitido.");
        }
        return texto.toString();
    }
}
