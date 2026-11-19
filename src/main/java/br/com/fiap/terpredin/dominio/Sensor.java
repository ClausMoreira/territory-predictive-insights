package br.com.fiap.terpredin.dominio;

import br.com.fiap.terpredin.excecao.FonteIndisponivelException;

/**
 * Contrato de qualquer sensor do território.
 */
public interface Sensor {

    /** Identificador único do sensor, por exemplo "TA-LESTE-01". */
    String getId();

    /** Lê o valor atual do sensor. */
    double lerValor() throws FonteIndisponivelException;

    /** Executa um autoteste e indica se o sensor está respondendo. */
    boolean verificarFuncionamento();

    /** Retorna o tipo do sensor, com unidade e faixa válida. */
    TipoSensor getTipo();
}