package br.com.fiap.terpredin.dominio;

/**
 * Níveis de alerta em ordem crescente de gravidade.
 * A decisão final é sempre da equipe responsável, embora a orientação sugira uma ação.
 */
public enum NivelAlerta {

    ATENCAO("ATENÇÃO", "acompanhar as próximas leituras e avisar a equipe de plantão."),
    ALERTA("ALERTA", "preparar ações preventivas, remanejar carga entre subestações "
            + "e orientar a população a reduzir o consumo no horário de pico."),
    CRITICO("CRÍTICO", "avaliar o acionamento imediato do plano de contingência "
            + "e a abertura de pontos de resfriamento para a população.");

    private final String rotulo;
    private final String orientacao;

    NivelAlerta(String rotulo, String orientacao) {
        this.rotulo = rotulo;
        this.orientacao = orientacao;
    }

    public String getRotulo() {
        return rotulo;
    }

    public String getOrientacao() {
        return orientacao;
    }
}
