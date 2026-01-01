package analisefundamental;

import analisefundamental.fabrica.FabricaAcoes;
import analisefundamental.modelo.*;
import java.util.*;

/**
 * Programa principal que demonstra TODO o sistema funcionando.
 * NÃO precisa de API Python - funciona completamente offline.
 */
public class Principal {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("           RODRIGO SILVA - ANÁLISE FUNDAMENTAL v3.1");
        System.out.println("          Sistema Completo de Stock Picking (Offline)");
        System.out.println("=".repeat(80));
        System.out.println();

        // 1. Configuração inicial
        double taxaTesouro = ClienteApi.buscarTaxaTesouro();
        System.out.printf("💰 Taxa Livre de Risco (US 10Y): %.2f%%\n\n", taxaTesouro * 100);

        // 2. Criar portefólio
        Portefolio portefolio = new Portefolio("Rodrigo Silva", taxaTesouro, 100000.0);

        // 3. Analisar TODAS as ações do banco de dados
        System.out.println("🔍 ANALISANDO TODAS AS AÇÕES DISPONÍVEIS...\n");

        // Lista de todas as ações no sistema
        String[] todasAcoes = {
                "AAPL", "MSFT", "JPM", "KO", "NEE",  // Boas ações
                "TSLA", "DIL", "DEBT", "LOSS", "FALL" // Ações com problemas
        };

        for (String ticker : todasAcoes) {
            System.out.println("-".repeat(60));
            System.out.println("Processando: " + ticker);

            try {
                // Buscar dados (do banco local)
                Map<String, Object> dados = ClienteApi.buscarDadosAcao(ticker);

                // Criar ação via Factory Pattern
                Acao acao = FabricaAcoes.criarAcao(dados);

                // Adicionar ao portefólio
                portefolio.adicionarAcao(acao);

                // Análise rápida
                List<String> kills = acao.verificarKillSwitchesUniversais();
                if (!kills.isEmpty()) {
                    System.out.println("   Status: ❌ REJEITADA");
                    System.out.println("   Motivo: " + kills.get(0));
                } else {
                    double precoJusto = acao.calcularPrecoJusto(taxaTesouro);
                    double margem = acao.calcularMargemSeguranca(precoJusto);
                    double nota = acao.calcularNotaFinal();
                    var rec = acao.obterRecomendacao(margem, nota);

                    System.out.printf("   Status: %s (Nota: %.1f, Margem: %.1f%%)\n",
                            rec, nota, margem);
                }

            } catch (Exception e) {
                System.out.println("   Erro: " + e.getMessage());
            }
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ ANÁLISE CONCLUÍDA!");
        System.out.println("=".repeat(80));

        // 4. Gerar relatório completo
        System.out.println("\n" + portefolio.gerarRelatorioCompleto());

        // 5. Demonstrar análise detalhada de uma ação específica
        System.out.println("\n" + "=".repeat(80));
        System.out.println("ANÁLISE DETALHADA DE EXEMPLO: AAPL (Apple Inc.)");
        System.out.println("=".repeat(80));

        Acao apple = portefolio.encontrarAcao("AAPL");
        if (apple != null) {
            System.out.println(apple.gerarResumoAnalise(taxaTesouro));
        }

        // 6. Menu interativo para análise adicional
        executarMenuInterativo(portefolio, taxaTesouro);

        System.out.println("\n👋 Programa concluído com sucesso!");
    }//main

    private static void executarMenuInterativo(Portefolio portefolio, double taxaTesouro) {
        Scanner scanner = new Scanner(System.in);
        boolean continuar = true;

        while (continuar) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("MENU INTERATIVO DE ANÁLISE");
            System.out.println("=".repeat(60));
            System.out.println("1. Analisar ação específica");
            System.out.println("2. Ver estatísticas do portefólio");
            System.out.println("3. Ver todas as ações analisadas");
            System.out.println("4. Testar nova ação (dados fictícios)");
            System.out.println("5. Sair");
            System.out.print("\nEscolha uma opção: ");

            try {
                int opcao = Integer.parseInt(scanner.nextLine());

                switch (opcao) {
                    case 1:
                        analisarAcaoEspecifica(scanner, portefolio, taxaTesouro);
                        break;
                    case 2:
                        exibirEstatisticas(portefolio);
                        break;
                    case 3:
                        exibirTodasAcoes(portefolio, taxaTesouro);
                        break;
                    case 4:
                        testarNovaAcao(scanner, portefolio, taxaTesouro);
                        break;
                    case 5:
                        continuar = false;
                        break;
                    default:
                        System.out.println("❌ Opção inválida!");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor, insira um número válido.");
            } catch (Exception e) {
                System.out.println("❌ Erro: " + e.getMessage());
            }
        }

        scanner.close();
    }//executarMenuInterativo

    private static void analisarAcaoEspecifica(Scanner scanner, Portefolio portefolio, double taxaTesouro) {
        System.out.print("\n📈 Digite o ticker da ação para análise detalhada: ");
        String ticker = scanner.nextLine().toUpperCase().trim();

        Acao acao = portefolio.encontrarAcao(ticker);
        if (acao == null) {
            System.out.println("❌ Ação não encontrada no portefólio.");
            System.out.print("Deseja buscar e analisar esta ação? (s/n): ");
            String resposta = scanner.nextLine().toLowerCase();

            if (resposta.equals("s")) {
                try {
                    Map<String, Object> dados = ClienteApi.buscarDadosAcao(ticker);
                    acao = FabricaAcoes.criarAcao(dados);
                    portefolio.adicionarAcao(acao);
                    System.out.println("✅ Ação adicionada e analisada!");
                } catch (Exception e) {
                    System.out.println("❌ Erro ao analisar ação: " + e.getMessage());
                    return;
                }
            } else {
                return;
            }
        }

        // Exibir análise detalhada
        System.out.println("\n" + acao.gerarResumoAnalise(taxaTesouro));
    }//analisarAcaoEspecifica

    private static void exibirEstatisticas(Portefolio portefolio) {
        Map<String, Object> stats = portefolio.obterEstatisticas();

        System.out.println("\n📊 ESTATÍSTICAS DO PORTEFÓLIO:");
        System.out.println("-".repeat(40));
        System.out.printf("Total de Ações: %d\n", (int) stats.get("totalAcoes"));
        System.out.printf("Aprovadas: %d (%.1f%%)\n",
                (int) stats.get("aprovadas"), (double) stats.get("percentagemAprovadas"));
        System.out.printf("Em Vigilância: %d\n", (int) stats.get("vigiadas"));
        System.out.printf("Rejeitadas: %d\n", (int) stats.get("rejeitadas"));
        System.out.printf("Média de Notas: %.1f/100\n", (double) stats.get("mediaNota"));
    }//exibirEstatisticas

    private static void exibirTodasAcoes(Portefolio portefolio, double taxaTesouro) {
        System.out.println("\n📋 TODAS AS AÇÕES ANALISADAS:");
        System.out.println("=".repeat(80));

        int i = 1;
        for (Acao acao : portefolio.obterTodasAcoes()) {
            double pj = acao.calcularPrecoJusto(taxaTesouro);
            double margem = acao.calcularMargemSeguranca(pj);
            double nota = acao.calcularNotaFinal();
            var rec = acao.obterRecomendacao(margem, nota);

            System.out.printf("%d. %-6s | %-25s | Nota: %5.1f | Margem: %6.1f%% | %s\n",
                    i++, acao.obterTicker(),
                    acao.obterNome().substring(0, Math.min(25, acao.obterNome().length())),
                    nota, margem, rec);
        }
    }//exibirTodasAcoes

    private static void testarNovaAcao(Scanner scanner, Portefolio portefolio, double taxaTesouro) {
        System.out.print("\n🧪 TESTAR NOVA AÇÃO (dados fictícios)\n");
        System.out.print("Digite o ticker para teste: ");
        String ticker = scanner.nextLine().toUpperCase().trim();

        if (ticker.isEmpty()) {
            System.out.println("❌ Ticker não pode ser vazio.");
            return;
        }

        try {
            // Criar dados fictícios para o novo ticker
            Map<String, Object> dados = ClienteApi.buscarDadosAcao(ticker);

            // Permitir ao usuário ajustar alguns parâmetros
            System.out.println("\n⚠️  Ajustar parâmetros (deixe em branco para usar padrão):");

            System.out.print("Preço Atual [" + dados.get("precoAtual") + "]: ");
            String precoStr = scanner.nextLine();
            if (!precoStr.isEmpty()) {
                dados.put("precoAtual", Double.parseDouble(precoStr));
            }

            System.out.print("Beta [" + dados.get("beta") + "]: ");
            String betaStr = scanner.nextLine();
            if (!betaStr.isEmpty()) {
                dados.put("beta", Double.parseDouble(betaStr));
            }

            System.out.print("Setor [" + dados.get("setor") + "]: ");
            String setorStr = scanner.nextLine();
            if (!setorStr.isEmpty()) {
                dados.put("setor", setorStr);
            }

            // Criar e analisar a ação
            Acao novaAcao = FabricaAcoes.criarAcao(dados);
            portefolio.adicionarAcao(novaAcao);

            System.out.println("\n✅ Ação criada e analisada com sucesso!");
            System.out.println(novaAcao.gerarResumoAnalise(taxaTesouro));

        } catch (Exception e) {
            System.out.println("❌ Erro ao criar ação: " + e.getMessage());
        }
    }//testarNovaAcao

}//classe Principal