package analisefundamental;

import analisefundamental.fabrica.FabricaAcoes;
import analisefundamental.modelo.*;
import java.util.*;

/**
 * Programa principal que demonstra TODO o sistema funcionando.
 * VERSÃO FINAL: Lista de ações limpa e menu protegido contra erros.
 */
public class Principal {

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("           RODRIGO SILVA - ANÁLISE FUNDAMENTAL v3.1");
        System.out.println("          Sistema Completo de Stock Picking (Offline)");
        System.out.println("=".repeat(80));
        System.out.println();

        // 1. Configuração inicial
        double taxaTesouro = ClienteApi.obterTaxaTesouro();
        System.out.printf("💰 Taxa Livre de Risco (US 10Y): %.2f%%\n\n", taxaTesouro * 100);

        // 2. Criar portefólio
        Portefolio portefolio = new Portefolio("Rodrigo Silva", taxaTesouro, 100000.0);

        // 3. Analisar TODAS as ações reais
        System.out.println("🔍 ANALISANDO TODAS AS AÇÕES DISPONÍVEIS...\n");

        // LISTA LIMPA: Apenas empresas reais para análise séria
        String[] todasAcoes = {
                "AAPL", "MSFT", "JPM", "KO", "NEE",  // Clássicas
                "TSLA", "NVG.LS", "NVO", "GOOGL"     // Outras interessantes
        };

        for (String ticker : todasAcoes) {
            System.out.println("-".repeat(60));
            System.out.println("Processando: " + ticker);

            try {
                Map<String, Object> dados = ClienteApi.obterDadosAcao(ticker);

                // Se a API falhar ou a ação não existir, ignoramos silenciosamente aqui
                if (dados.containsKey("erro") || dados.isEmpty()) {
                    System.out.println("   ⚠️  Ação não encontrada ou erro na API - IGNORADA");
                    continue;
                }

                // Criar ação via Factory Pattern
                Acao acao = FabricaAcoes.criarAcao(dados);

                // Adicionar ao portefólio
                portefolio.adicionarAcao(acao);

                // Análise rápida na consola
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
                System.out.println("   Erro não tratado: " + e.getMessage());
            }
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ ANÁLISE CONCLUÍDA!");
        System.out.println("=".repeat(80));

        // 4. Gerar relatório completo
        System.out.println("\n" + portefolio.gerarRelatorioCompleto());

        // 5. Menu interativo
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
            System.out.println("1. Analisar nova ação");
            System.out.println("2. Ver Ranking Atual");
            System.out.println("3. Sair");
            System.out.print("\nEscolha uma opção: ");

            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                int opcao = Integer.parseInt(input);

                switch (opcao) {
                    case 1:
                        analisarNovaAcao(scanner, portefolio, taxaTesouro);
                        break;
                    case 2:
                        System.out.println(portefolio.gerarRelatorioCompleto());
                        break;
                    case 3:
                        continuar = false;
                        break;
                    default:
                        System.out.println("❌ Opção inválida! Escolha 1, 2 ou 3.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Por favor, digite apenas números (ex: 1).");
            } catch (Exception e) {
                System.out.println("❌ Erro inesperado: " + e.getMessage());
            }
        }
        scanner.close();
    }//executarMenuInterativo

    private static void analisarNovaAcao(Scanner scanner, Portefolio portefolio, double taxaTesouro) {
        System.out.print("\n📈 Digite o ticker da ação (ex: AAPL): ");
        String ticker = scanner.nextLine().toUpperCase().trim();

        if (ticker.isEmpty()) return;

        try {
            Map<String, Object> dados = ClienteApi.obterDadosAcao(ticker);

            if (dados.containsKey("erro")) {
                System.out.println("❌ Erro: Ação não encontrada ou problema na API.");
                return;
            }

            Acao acao = FabricaAcoes.criarAcao(dados);

            System.out.println("\n--- RESULTADO PRELIMINAR ---");
            System.out.println(acao.toString());
            System.out.printf("Preço Justo: $%.2f\n", acao.calcularPrecoJusto(taxaTesouro));

            portefolio.adicionarAcao(acao);
            System.out.println("✅ Adicionada ao portefólio com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro ao analisar ação: " + e.getMessage());
        }
    }//analisarNovaAcao

}//classe Principal