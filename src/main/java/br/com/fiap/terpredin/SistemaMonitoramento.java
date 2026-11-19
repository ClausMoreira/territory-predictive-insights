package br.com.fiap.terpredin;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import br.com.fiap.terpredin.dominio.DadosTerritorio;
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

/**
 * Menu principal de console. Spring Boot executa o método run() quando iniciar.
 * Toda exceção é capturada aqui e vira uma mensagem pro usuário.
 */
@Component
public class SistemaMonitoramento implements CommandLineRunner {

    /** Código de acesso para alterar limiares. */
    private static final String CODIGO_ACESSO = "gs2026";

    private final Scanner scanner = new Scanner(System.in);
    private final ConfiguracaoSistema configuracao = ConfiguracaoSistema.getInstancia();
    private final SubestacaoEnergia subestacao;
    private final DadosTerritorio territorio;

    public SistemaMonitoramento() {
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
        System.out.println("1 - Verificar sensores (nova leitura)");
        System.out.println("2 - Rodar previsões");
        System.out.println("3 - Ver histórico");
        System.out.println("4 - Gerenciar dados do território");
        System.out.println("5 - Status completo");
        System.out.println("0 - Sair");
        System.out.print("Escolha: ");
    }

    // 1. Verificar sensores

    private void verificarSensores() {
        System.out.println("\n--- Autoteste da subestação ---");
        System.out.println("Status: " + subestacao.verificarStatus().getDescricao());

        System.out.println("\n--- Leitura dos sensores ---");
        for (Sensor sensor : subestacao.getSensores()) {
            try {
                double valor = sensor.lerValor();
                Medicao medicao = new Medicao(sensor.getId(), sensor.getTipo(), valor, LocalDateTime.now());
                System.out.println(medicao);
            } catch (FonteIndisponivelException e) {
                System.out.println("[SENSOR INDISPONÍVEL] " + e.getMessage());
            }
        }
    }

    // 2. Rodar previsões 

    private void rodarPrevisoes() {
        System.out.println("Em construção: as previsões entram na Fase 4.");
    }

    // 3. Ver histórico 

    private void verHistorico() {
        System.out.println("Em construção: o histórico entra na Fase 3, junto com o banco de dados.");
    }

    // 4. Gerenciar dados do território

    private void gerenciarDadosTerritorio() {
        System.out.println("\n--- Dados do território ---");
        System.out.println("1 - Ver dados e limiares");
        System.out.println("2 - Alterar limiar de alerta (exige código de acesso)");
        System.out.println("3 - Alterar população atendida");
        System.out.println("4 - Alterar quantidade de equipamentos críticos");
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
        System.out.println("Janela de previsão: últimas " + configuracao.getJanelaPrevisao() + " leituras");
    }

    private void alterarLimiar() {
        TipoSensor tipo = escolher("Tipo de sensor", TipoSensor.values(), TipoSensor::getDescricao);
        NivelAlerta nivel = escolher("Nível", NivelAlerta.values(), NivelAlerta::getRotulo);
        System.out.println("Limiar atual: " + tipo.formatar(configuracao.getLimiar(tipo, nivel)));
        double novoValor = lerDecimal("Novo limiar (" + tipo.getUnidade() + "): ");
        System.out.print("Código de acesso: ");
        String codigo = scanner.nextLine().trim();
        territorio.alterarLimiar(codigo, tipo, nivel, novoValor);
        System.out.println("Limiar de " + nivel.getRotulo() + " para " + tipo.getDescricao()
                + " atualizado para " + tipo.formatar(novoValor) + ".");
    }

    // 5. Status completo

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
    }

    // Leitura de entradas

    private <E extends Enum<E>> E escolher(String titulo, E[] opcoes, Function<E, String> rotulo) {
        System.out.println(titulo + ":");
        for (int i = 0; i < opcoes.length; i++) {
            System.out.println("  " + (i + 1) + " - " + rotulo.apply(opcoes[i]));
        }
        int escolha = lerInteiro("Escolha: ");
        if (escolha < 1 || escolha > opcoes.length) {
            throw new DadosInvalidosException("Opção " + escolha + " não existe. Escolha entre 1 e " + opcoes.length + ".");
        }
        return opcoes[escolha - 1];
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