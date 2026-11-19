# Territory Predictive Insights · Calor e Energia

Global Solution 2º semestre/2026 · Programação Orientada a Objetos · FIAP · Turma 2CCPX

## Integrantes
| Nome | RM |
|---|---|
| Claus Moreira | RM565503 |
| Julia Lopes | RM566557 |

## O recorte: calor e energia

Quando uma onda de calor chega, quase tudo acontece ao mesmo tempo. Ventiladores e aparelhos de ar-condicionado ligam em milhares de casas, a carga da rede sobe e os transformadores, já aquecidos pelo próprio dia, passam a trabalhar no limite. É justamente aí que a energia costuma cair, no momento em que ela mais faz falta.

Nosso sistema acompanha esse cenário em uma subestação que atende uma região densamente povoada. Ele lê três sensores, guarda o histórico em banco de dados, estima como as próximas leituras devem se comportar e avisa a equipe quando algo se aproxima de um limite perigoso.

## Público beneficiado

Direcionamos o sistema à equipe de operação de uma cooperativa de energia ou aos órgãos responsáveis pelas subestações que atendem bairros densamente povoados. Quem sofre com o problema são os moradores dessas regiões, sobretudo idosos, pessoas que dependem de equipamentos elétricos em casa e serviços públicos como UBS e escolas, que ficam sem energia quando a rede sobrecarrega em dias de calor extremo.

**A decisão que o sistema apoia** é a de agir antes que a sobrecarga aconteça: remanejar carga entre subestações, fazer campanhas de redução de consumo no horário de pico e abrir pontos de resfriamento. O sistema sugere; quem decide é a equipe.

## Como executar

Basta ter o **JDK 21** instalado. O Maven já vem no repositório (Maven Wrapper) e o banco H2 é criado sozinho na primeira execução, sem nenhuma instalação extra.

```bash
git clone https://github.com/ClausMoreira/territory-predictive-insights.git
cd territory-predictive-insights
./mvnw clean test        # roda os 26 testes automatizados
./mvnw spring-boot:run   # abre o menu do sistema
```

No Prompt de Comando ou no PowerShell, troque `./mvnw` por `mvnw.cmd`.

**Acentos quebrados no terminal?** O terminal do Windows nem sempre usa UTF-8. Antes de iniciar o programa, execute `chcp.com 65001` no Git Bash ou `chcp 65001` no Prompt de Comando e no PowerShell.

**Onde ficam os dados?** Em `./data/territorio.mv.db`. É esse arquivo que mantém as medições e os alertas depois que o programa fecha. Para recomeçar do zero, basta apagar a pasta `data/`.

**Código de acesso.** Alterar um limiar de alerta (menu 4, opção 2) pede o código `gs2026`. Mantivemos essa proteção porque mudar um limite muda o momento em que a equipe será avisada.

### Um roteiro para conhecer o sistema

Para ver tudo funcionando em poucos minutos, sugerimos este caminho:

1. Abra a opção **2** logo no início. Sem histórico suficiente, o sistema prefere ficar em silêncio, e explica por quê.
2. Na opção **1**, peça **6** rodadas de leitura. Cada medição válida vai direto para o banco.
3. Volte à opção **2** e veja as previsões por média móvel, com os alertas e o motivo de cada um.
4. Em **4 → 5**, troque a estratégia para a comparação com o período anterior e rode a opção **2** de novo. Repare como a leitura de tendência muda o resultado.
5. Em **3 → 2**, confira os alertas já emitidos e, se quiser, exporte tudo para CSV.
6. Saia com **0**, abra o programa outra vez e consulte a opção **3**: o histórico continua lá.

## Como o código está organizado

```
src/main/java/br/com/fiap/terpredin/
├── TerritoryPredictiveInsightsApplication.java   (inicialização do Spring Boot)
├── SistemaMonitoramento.java                     (menu principal)
├── dominio/      ComponenteTerritorial, SubestacaoEnergia, Sensor, SensorSimulado,
│                 SensorTemperaturaAmbiente, SensorCargaRede, SensorTemperaturaTransformador,
│                 TipoSensor, StatusOperacional, DadosTerritorio, Medicao, Alerta, NivelAlerta,
│                 EstrategiaPrevisao, PrevisaoMediaMovel, PrevisaoComparacao
├── excecao/      DadosInvalidosException, FonteIndisponivelException
├── persistencia/ MedicaoRepository, AlertaRepository
└── servico/      ServicoMonitoramento, ConfiguracaoSistema, FabricaPrevisao, TipoPrevisao,
                  ResultadoPrevisao, ExportadorCsv
src/test/java/br/com/fiap/terpredin/
├── dominio/      MedicaoTest, DadosTerritorioTest, PrevisaoMediaMovelTest, PrevisaoComparacaoTest
└── servico/      ServicoMonitoramentoTest, FabricaPrevisaoTest, ExportadorCsvTest
```

## Decisões que tomamos

**Três sensores, uma mesma lógica.** Medimos a temperatura do ar, a carga da rede e a temperatura do transformador. Os três têm algo em comum que simplificou bastante o projeto: quanto maior o valor, maior o risco. Assim, a comparação com os limiares segue sempre a mesma regra.

| Sensor | ATENÇÃO | ALERTA | CRÍTICO |
|---|---|---|---|
| Temperatura ambiente | 32 °C | 35 °C | 38 °C |
| Carga da rede elétrica (% da capacidade) | 80% | 90% | 100% |
| Temperatura do transformador | 75 °C | 85 °C | 95 °C |

Os sensores são simulados com `java.util.Random`, em faixas típicas de uma onda de calor. Também demos a eles 5% de chance de falhar a cada leitura: equipamento real falha, e queríamos que o sistema soubesse lidar com isso. É aí que entra a `FonteIndisponivelException`.

**Um banco que não esquece.** Seguimos o caminho Spring Boot com Spring Data JPA. O H2 em memória seria o atalho mais curto, mas ele apaga tudo quando o programa fecha, e previsão sem histórico é só um palpite. Por isso gravamos o banco em arquivo. As consultas nascem do nome dos métodos dos repositories, então nenhuma linha de SQL é montada concatenando texto.

**Erros com nome e explicação.** Criamos duas exceções com papéis diferentes. A `DadosInvalidosException` aponta entradas que não fazem sentido, como 80 °C de temperatura do ar ou uma população negativa; ela é unchecked e chega ao menu como uma mensagem clara. A `FonteIndisponivelException` é checked, porque falha de equipamento não é exatamente uma surpresa: vai acontecer, e o compilador obriga quem lê um sensor a se preparar para isso. Em todo o projeto, nenhum `catch` fica vazio.

**Saber a hora de ficar em silêncio.** Com menos de 5 leituras de um sensor, o sistema não arrisca previsão nem alerta. Preferimos um "ainda não sei" honesto a um número sem base.

**Duas formas de olhar para frente.** A média móvel observa as últimas leituras e responde "qual é o patamar atual?". A comparação com o período anterior responde "para onde as coisas estão indo?", projetando a variação mais recente. Quando o histórico ainda é curto, ela reduz os períodos para trabalhar com o que existe.

**Pensado para ser testado.** O `ServicoMonitoramento` recebe os repositories pelo construtor. Esse detalhe permitiu trocar o banco por mocks do Mockito e testar a lógica de alerta isoladamente.

**Território, não pessoas.** Descrevemos o território por região e equipamentos: a subestação, a população estimada e a quantidade de serviços críticos. Nenhum dado pessoal entra no sistema.

**O bônus.** Escolhemos exportar o histórico consultado em CSV (menu 3). Usamos ponto e vírgula como separador e vírgula decimal, para que o arquivo abra direitinho no Excel em português, com acentos e tudo.

## Padrões criacionais
_(em construção)_

## Testes automatizados
_(em construção)_

## Reflexão crítica
_(em construção)_

## Uso de IA
_(em construção)_

## Divisão de tarefas

O enunciado sugere três frentes de trabalho. Como somos uma dupla, reorganizamos tudo em duas:

| Integrante | Responsabilidades |
|---|---|
| Claus Moreira | Domínio (`ComponenteTerritorial`, sensores, `Medicao`, `DadosTerritorio`), exceções customizadas, persistência (repositories e gravação no `ServicoMonitoramento`), menu principal, exportação em CSV e as seções de execução, organização e decisões deste README |
| Julia Lopes | `NivelAlerta`, `Alerta`, `EstrategiaPrevisao` e as duas estratégias, `ConfiguracaoSistema` (Singleton), geração de alertas no `ServicoMonitoramento`, `FabricaPrevisao` (Factory), testes automatizados e as seções de padrões, testes, reflexão crítica e uso de IA deste README |

Cada integrante fez os commits da própria parte, como mostra o histórico do repositório.

---

*Que os alertas cheguem a tempo.* 🌡️
