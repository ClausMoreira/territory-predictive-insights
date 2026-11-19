package br.com.fiap.terpredin.dominio;

/**
 * Tipos de sensor do recorte "Calor e energia".
 * Cada tipo conhece sua unidade e a faixa fisicamente possível de leitura,
 * usada para rejeitar medições absurdas.
 */
public enum TipoSensor {

    TEMPERATURA_AMBIENTE("Temperatura ambiente", "°C", -10.0, 55.0),
    CARGA_REDE("Carga da rede elétrica", "%", 0.0, 150.0),
    TEMPERATURA_TRANSFORMADOR("Temperatura do transformador", "°C", -10.0, 150.0);

    private final String descricao;
    private final String unidade;
    private final double minimo;
    private final double maximo;

    TipoSensor(String descricao, String unidade, double minimo, double maximo) {
        this.descricao = descricao;
        this.unidade = unidade;
        this.minimo = minimo;
        this.maximo = maximo;
    }

    /** Indica se o valor está dentro da faixa fisicamente possível para este tipo. */
    public boolean aceita(double valor) {
        return !Double.isNaN(valor) && valor >= minimo && valor <= maximo;
    }

    /** Formata o valor com uma casa decimal e a unidade, por exemplo "34,2 °C". */
    public String formatar(double valor) {
        return String.format("%.1f %s", valor, unidade);
    }

    public String getDescricao() {
        return descricao;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getMinimo() {
        return minimo;
    }

    public double getMaximo() {
        return maximo;
    }
}