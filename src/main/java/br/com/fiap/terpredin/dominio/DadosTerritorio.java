package br.com.fiap.terpredin.dominio;
import br.com.fiap.terpredin.excecao.DadosInvalidosException;
import br.com.fiap.terpredin.servico.ConfiguracaoSistema;

/**
 * Dados do territórioOs atributos são privados e só mudam por métodos
 * que validam. Alterar limiares exige o código de acesso da equipe.
 * O território é descrito por região e equipamentos.
 */
public final class DadosTerritorio {

    private final String regiao;
    private int populacaoAtendida;
    private int equipamentosCriticos;
    private final String codigoAcesso;

    /**
     * @param regiao               nome da região, por exemplo: "Zona Leste"
     * @param populacaoAtendida    número estimado de moradores atendidos
     * @param equipamentosCriticos UBS, escolas e outros equipamentos que dependem de energia
     * @param codigoAcesso         código exigido para alterar limiares (min. 4 caracteres)
     */
    public DadosTerritorio(String regiao, int populacaoAtendida, int equipamentosCriticos, String codigoAcesso) {
        if (regiao == null || regiao.isBlank()) {
            throw new DadosInvalidosException("A região do território é obrigatória.");
        }
        if (codigoAcesso == null || codigoAcesso.length() < 4) {
            throw new DadosInvalidosException("O código de acesso deve ter pelo menos 4 caracteres.");
        }
        this.regiao = regiao.trim();
        this.codigoAcesso = codigoAcesso;
        setPopulacaoAtendida(populacaoAtendida);
        setEquipamentosCriticos(equipamentosCriticos);
    }

    public void setPopulacaoAtendida(int populacaoAtendida) {
        if (populacaoAtendida < 0) {
            throw new DadosInvalidosException("A população atendida não pode ser negativa (informado: "
                    + populacaoAtendida + ").");
        }
        this.populacaoAtendida = populacaoAtendida;
    }

    public void setEquipamentosCriticos(int equipamentosCriticos) {
        if (equipamentosCriticos < 0) {
            throw new DadosInvalidosException("A quantidade de equipamentos críticos não pode ser negativa (informado: "
                    + equipamentosCriticos + ").");
        }
        this.equipamentosCriticos = equipamentosCriticos;
    }

    /**
     * Altera um limiar de alerta somente se o código de acesso for o correto.
     * A validação do valor fica com ConfiguracaoSistema.
     */
    public void alterarLimiar(String codigoInformado, TipoSensor tipo, NivelAlerta nivel, double novoValor) {
        validarCodigo(codigoInformado);
        ConfiguracaoSistema.getInstancia().definirLimiar(tipo, nivel, novoValor);
    }

    private void validarCodigo(String codigoInformado) {
        if (!codigoAcesso.equals(codigoInformado)) {
            throw new DadosInvalidosException("Código de acesso incorreto: os limiares não foram alterados.");
        }
    }

    public String getRegiao() {
        return regiao;
    }

    public int getPopulacaoAtendida() {
        return populacaoAtendida;
    }

    public int getEquipamentosCriticos() {
        return equipamentosCriticos;
    }

    @Override
    public String toString() {
        return regiao + " | população atendida: " + populacaoAtendida
                + " | equipamentos críticos: " + equipamentosCriticos;
    }
}