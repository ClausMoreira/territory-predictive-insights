package br.com.fiap.terpredin.servico;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import br.com.fiap.terpredin.dominio.Alerta;
import br.com.fiap.terpredin.dominio.Medicao;

/**
 * Exporta o histórico consultado no menu para arquivos CSV (bônus da GS).
 * Usa ponto e vírgula como separador e vírgula decimal, o padrão do Excel em português,
 * e grava em UTF-8 com BOM para os acentos aparecerem corretamente.
 */
public final class ExportadorCsv {

    private static final String SEPARADOR = ";";
    private static final String BOM_UTF8 = "\uFEFF";
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExportadorCsv() {
        // classe utilitária: não deve ser instanciada
    }

    public static Path exportarMedicoes(List<Medicao> medicoes, Path destino) throws IOException {
        prepararPasta(destino);
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            escritor.write(BOM_UTF8 + "data_hora;sensor;tipo;valor;unidade");
            escritor.newLine();
            for (Medicao medicao : medicoes) {
                escritor.write(String.join(SEPARADOR,
                        medicao.getDataHora().format(FORMATO_DATA),
                        escapar(medicao.getSensorId()),
                        escapar(medicao.getTipoSensor().getDescricao()),
                        formatarNumero(medicao.getValor()),
                        escapar(medicao.getTipoSensor().getUnidade())));
                escritor.newLine();
            }
        }
        return destino;
    }

    public static Path exportarAlertas(List<Alerta> alertas, Path destino) throws IOException {
        prepararPasta(destino);
        try (BufferedWriter escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            escritor.write(BOM_UTF8 + "data_hora;nivel;sensor;tipo;valor_previsto;limite;estrategia;motivo");
            escritor.newLine();
            for (Alerta alerta : alertas) {
                escritor.write(String.join(SEPARADOR,
                        alerta.getDataHora().format(FORMATO_DATA),
                        escapar(alerta.getNivel().getRotulo()),
                        escapar(alerta.getSensorId()),
                        escapar(alerta.getTipoSensor().getDescricao()),
                        formatarNumero(alerta.getValorPrevisto()),
                        formatarNumero(alerta.getLimiteEstourado()),
                        escapar(alerta.getEstrategia()),
                        escapar(alerta.getMotivo())));
                escritor.newLine();
            }
        }
        return destino;
    }

    /** Coloca o campo entre aspas quando ele contém separador, aspas ou quebra de linha. */
    static String escapar(String campo) {
        if (campo.contains(SEPARADOR) || campo.contains("\"") || campo.contains("\n")) {
            return "\"" + campo.replace("\"", "\"\"") + "\"";
        }
        return campo;
    }

    private static String formatarNumero(double valor) {
        return String.format(PT_BR, "%.1f", valor);
    }

    private static void prepararPasta(Path destino) throws IOException {
        Path pasta = destino.toAbsolutePath().getParent();
        if (pasta != null) {
            Files.createDirectories(pasta);
        }
    }
}