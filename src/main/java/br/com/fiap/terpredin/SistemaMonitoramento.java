package br.com.fiap.terpredin;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.fiap.terpredin.dominio.Alerta;
import br.com.fiap.terpredin.dominio.DadosTerritorio;
import br.com.fiap.terpredin.dominio.EstrategiaPrevisao;
import br.com.fiap.terpredin.dominio.Medicao;
import br.com.fiap.terpredin.dominio.NivelAlerta;
import br.com.fiap.terpredin.dominio.Sensor;
import br.com.fiap.terpredin.dominio.SensorCargaRede;
import br.com.fiap.terpredin.dominio.SensorTemperaturaAmbiente;
import br.com.fiap.terpredin.dominio.SensorTemperaturaTransformador;
import br.com.fiap.terpredin.dominio.SubestacaoEnergia;
import br.com.fiap.terpredin.dominio.TipoSensor;
import br.com.fiap.terpredin.excecao.DadosInvalidosException;
import br.com.fiap.terpredin.excecao.FonteIndisponivelException;
import br.com.fiap.terpredin.servico.ConfiguracaoSistema;
import br.com.fiap.terpredin.servico.FabricaPrevisao;
import br.com.fiap.terpredin.servico.ResultadoPrevisao;
import br.com.fiap.terpredin.servico.ServicoMonitoramento;
import br.com.fiap.terpredin.servico.TipoPrevisao;

/**
 * Menu principal de console. O Spring Boot executa o método run() ao iniciar.
 * Toda exceção é capturada aqui e vira uma mensagem clara para o usuário.
 */
@Component
public class SistemaMonitoramento implements CommandLineRunner {

    /** Código de acesso para alterar limiares (informado no README para a correção). */
    private static final String CODIGO_ACESSO = "gs2026";

    private final Scanner scanner = new Scanner(System.in);
    private final ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();
    private final ServicoMonitoramento servico;
    private final SubestacaoEnergia subestacao;
    private final DadosTerritorio territorio;

    /** O Spring entrega o ServicoMonitoramento pronto, já ligado ao banco. */
    public SistemaMonitoramento(ServicoMonitoramento servico) {
        this.servico = servico;
        subestacao = new SubestacaoEnergia("SE-LESTE-01", "Subestação Leste", "Zona Leste", 40.0);
        subestacao.adicionarSensor(new SensorTemperaturaAmbiente("TA-LESTE-01"));
        subestacao.adicionarSensor(new SensorCargaRede("CR-LESTE-01"));
        subestacao.adicionarSensor(new SensorTemperaturaTransformador("TT-LESTE-01"));
        territorio = new DadosTerritorio("Zona Leste", 250_000, 12, CODIGO_ACESSO);
    }

    @Override
    public void run(String... args) {
        System.out.println("==============================================");
        System.out.println(" Territory Predictive Insights | Calor e energia");
        System.out.println(" " + subestacao.getNome() + " - " + territorio.getRegiao());
        System.out.println("==============================================");

        boolean executando = true;
        while (executando) {
            exibirMenu();
            if (!scanner.hasNextLine()) {
                executando = false;
                continue;
            }
            String opcao = scanner.nextLine().trim();
            try {
                switch (opcao) {
                    case "1" -> verificarSensores();
                    case "2" -> rodarPrevisoes();
                    case "3" -> verHistorico();
                    case "4" -> gerenciarDadosTerritorio();
                    case "5" -> exibirStatusCompleto();
                    case "0" -> executando = false;
                    default -> System.out.println("Opção inválida. Digite um número de 0 a 5.");
                }
            } catch (DadosInvalidosException e) {
                System.out.println("[DADOS INVÁLIDOS] " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("[ERRO INESPERADO] " + e.getMessage()
                        + " A operação foi cancelada e o sistema continua rodando.");
            }
        }
        System.out.println("Sistema encerrado.");
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("1 - Verificar sensores (novas leituras gravadas no banco)");
        System.out.println("2 - Rodar previsões");
        System.out.println("3 - Ver histórico");
        System.out.println("4 - Gerenciar dados do território");
        System.out.println("5 - Status completo");
        System.out.println("0 - Sair");
        System.out.print("Escolha: ");
    }

    // ---------- 1. Verificar sensores ----------

    private void verificarSensores() {
        System.out.println("\n--- Autoteste da subestação ---");
        System.out.println("Status: " + subestacao.verificarStatus().getDescricao());

        int rodadas = lerInteiro("Quantas rodadas de leitura? (1 a 20): ");
        if (rodadas < 1 || rodadas > 20) {
            throw new DadosInvalidosException("O número de rodadas deve ficar entre 1 e 20 (informado: " + rodadas + ").");
        }

        int gravadas = 0;
        for (int rodada = 1; rodada <= rodadas; rodada++) {
            System.out.println("\n--- Rodada " + rodada + " de " + rodadas + " ---");
            for (Sensor sensor : subestacao.getSensores()) {
                try {
                    Medicao medicao = servico.registrarLeitura(sensor);
                    System.out.println(medicao);
                    gravadas++;
                } catch (FonteIndisponivelException e) {
                    System.out.println("[SENSOR INDISPONÍVEL] " + e.getMessage());
                }
            }
        }
        System.out.println("\n" + gravadas + " medição(ões) gravada(s) no banco.");
    }

    // ---------- 2. Rodar previsões ----------

    private void rodarPrevisoes() {
        EstrategiaPrevisao estrategia = FabricaPrevisao.criarPadrao();
        System.out.println("\n--- Previsões: " + estrategia.getNome() + " ---");

        int alertasEmitidos = 0;
        for (Sensor sensor : subestacao.getSensores()) {
            ResultadoPrevisao resultado = servico.gerarPrevisao(sensor, estrategia);
            System.out.println(resultado.resumo());
            System.out.println();
            if (resultado.gerouAlerta()) {
                alertasEmitidos++;
            }
        }
        System.out.println(alertasEmitidos + " alerta(s) emitido(s) nesta rodada. "
                + "Os alertas apoiam a decisão da equipe; a ação final é sempre de quem opera a rede.");
    }

    // ---------- 3. Ver histórico ----------

    private void verHistorico() {
        System.out.println("\n--- Histórico ---");
        System.out.println("1 - Medições por sensor");
        System.out.println("2 - Alertas já emitidos");
        System.out.print("Escolha: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> listarMedicoesPorSensor();
            case "2" -> listarAlertas();
            default -> System.out.println("Opção inválida.");
        }
    }

    private void listarMedicoesPorSensor() {
        Sensor sensor = escolher("Sensor", subestacao.getSensores(), Sensor::toString);
        List<Medicao> historico = servico.buscarHistoricoPorSensor(sensor.getId());
        if (historico.isEmpty()) {
            System.out.println("Nenhuma medição gravada para " + sensor + ". Use a opção 1 para fazer leituras.");
            return;
        }
        System.out.println(historico.size() + " medição(ões) de " + sensor + ":");
        historico.forEach(medicao -> System.out.println("  " + medicao));
    }

    private void listarAlertas() {
        List<Alerta> alertas = servico.listarAlertas();
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta emitido até agora.");
            return;
        }
        System.out.println(alertas.size() + " alerta(s) emitido(s), do mais recente para o mais antigo:");
        alertas.forEach(alerta -> System.out.println("  " + alerta));
    }

    // ---------- 4. Gerenciar dados do território ----------

    private void gerenciarDadosTerritorio() {
        System.out.println("\n--- Dados do território ---");
        System.out.println("1 - Ver dados e limiares");
        System.out.println("2 - Alterar limiar de alerta (exige código de acesso)");
        System.out.println("3 - Alterar população atendida");
        System.out.println("4 - Alterar quantidade de equipamentos críticos");
        System.out.println("5 - Trocar a estratégia de previsão");
        System.out.println("6 - Alterar a janela de previsão");
        System.out.print("Escolha: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> exibirDadosELimiares();
            case "2" -> alterarLimiar();
            case "3" -> {
                territorio.setPopulacaoAtendida(lerInteiro("Nova população atendida: "));
                System.out.println("População atendida atualizada.");
            }
            case "4" -> {
                territorio.setEquipamentosCriticos(lerInteiro("Nova quantidade de equipamentos críticos: "));
                System.out.println("Quantidade de equipamentos críticos atualizada.");
            }
            case "5" -> {
                TipoPrevisao tipo = escolher("Estratégia de previsão", List.of(TipoPrevisao.values()),
                        TipoPrevisao::getDescricao);
                configuracao.setEstrategiaPadrao(tipo);
                System.out.println("As próximas previsões vão usar: " + tipo.getDescricao() + ".");
            }
            case "6" -> {
                configuracao.setJanelaPrevisao(lerInteiro("Nova janela (quantidade de leituras, 2 a 50): "));
                System.out.println("Janela de previsão atualizada para " + configuracao.getJanelaPrevisao() + " leituras.");
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    private void exibirDadosELimiares() {
        System.out.println("Território: " + territorio);
        System.out.println("Limiares (ATENÇÃO / ALERTA / CRÍTICO):");
        for (TipoSensor tipo : TipoSensor.values()) {
            Map<NivelAlerta, Double> limiares = configuracao.getLimiares(tipo);
            System.out.println("  " + tipo.getDescricao() + ": "
                    + tipo.formatar(limiares.get(NivelAlerta.ATENCAO)) + " / "
                    + tipo.formatar(limiares.get(NivelAlerta.ALERTA)) + " / "
                    + tipo.formatar(limiares.get(NivelAlerta.CRITICO)));
        }
        System.out.println("Estratégia de previsão: " + configuracao.getEstrategiaPadrao().getDescricao()
                + " | janela: últimas " + configuracao.getJanelaPrevisao() + " leituras");
    }

    private void alterarLimiar() {
        TipoSensor tipo = escolher("Tipo de sensor", List.of(TipoSensor.values()), TipoSensor::getDescricao);
        NivelAlerta nivel = escolher("Nível", List.of(NivelAlerta.values()), NivelAlerta::getRotulo);
        System.out.println("Limiar atual: " + tipo.formatar(configuracao.getLimiar(tipo, nivel)));
        double novoValor = lerDecimal("Novo limiar (" + tipo.getUnidade() + "): ");
        System.out.print("Código de acesso: ");
        String codigo = scanner.nextLine().trim();
        territorio.alterarLimiar(codigo, tipo, nivel, novoValor);
        System.out.println("Limiar de " + nivel.getRotulo() + " para " + tipo.getDescricao()
                + " atualizado para " + tipo.formatar(novoValor) + ".");
    }

    // ---------- 5. Status completo ----------

    private void exibirStatusCompleto() {
        System.out.println("\n--- Status completo ---");
        subestacao.verificarStatus();
        System.out.println("Componente: " + subestacao);
        System.out.println("Capacidade: " + subestacao.getCapacidadeMva() + " MVA");
        System.out.println("Sensores instalados:");
        for (Sensor sensor : subestacao.getSensores()) {
            System.out.println("  " + sensor);
        }
        exibirDadosELimiares();
        System.out.println("Banco de dados: " + servico.contarMedicoes() + " medição(ões) e "
                + servico.contarAlertas() + " alerta(s) gravados");
    }

    // ---------- Leitura de entradas ----------

    private <T> T escolher(String titulo, List<T> opcoes, Function<T, String> rotulo) {
        System.out.println(titulo + ":");
        for (int i = 0; i < opcoes.size(); i++) {
            System.out.println("  " + (i + 1) + " - " + rotulo.apply(opcoes.get(i)));
        }
        int escolha = lerInteiro("Escolha: ");
        if (escolha < 1 || escolha > opcoes.size()) {
            throw new DadosInvalidosException("Opção " + escolha + " não existe. Escolha entre 1 e " + opcoes.size() + ".");
        }
        return opcoes.get(escolha - 1);
    }

    private int lerInteiro(String rotulo) {
        System.out.print(rotulo);
        String texto = scanner.nextLine().trim();
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("\"" + texto + "\" não é um número inteiro válido.");
        }
    }

    private double lerDecimal(String rotulo) {
        System.out.print(rotulo);
        String texto = scanner.nextLine().trim().replace(',', '.');
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new DadosInvalidosException("\"" + texto + "\" não é um número válido. Use, por exemplo, 35,5.");
        }
    }
}