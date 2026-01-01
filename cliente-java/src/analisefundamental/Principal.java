package analisefundamental;

import analisefundamental.fabrica.FabricaAcoes;
import analisefundamental.modelo.*;
import java.util.*;

/**
 * Programa principal que demonstra TODO o sistema funcionando.
 * VERSÃO CORRIGIDA: Usa 'gerarRelatorioCompleto' conforme definido no Portefolio.
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

        // 2. Criar portefólio (Usando o construtor completo que tens no Portefolio.java)
        Portefolio portefolio = new Portefolio("Rodrigo Silva", taxaTesouro, 100000.0);

        // 3. Analisar TODAS as ações
        System.out.println("🔍 ANALISANDO TODAS AS AÇÕES DISPONÍVEIS...\n");

        String[] todasAcoes = {
                "AAPL", "MSFT", "JPM", "KO", "NEE",
                "TSLA", "DIL", "DEBT", "LOSS", "FALL"
        };

        for (String ticker : todasAcoes) {
            System.out.println("-".repeat(60));
            System.out.println("Processando: " + ticker);

            try {
                Map<String, Object> dados = ClienteApi.obterDadosAcao(ticker);
                Acao acao = FabricaAcoes.criarAcao(dados);
                portefolio.adicionarAcao(acao);

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

        // CORREÇÃO AQUI: Mudado de 'gerarRelatorio' para 'gerarRelatorioCompleto'
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
                String input = scanner.nextLine();
                if (input.isEmpty()) continue;
                int opcao = Integer.parseInt(input);

                switch (opcao) {
                    case 1:
                        analisarNovaAcao(scanner, portefolio, taxaTesouro);
                        break;
                    case 2:
                        // CORREÇÃO AQUI TAMBÉM
                        System.out.println(portefolio.gerarRelatorioCompleto());
                        break;
                    case 3:
                        continuar = false;
                        break;
                    default:
                        System.out.println("❌ Opção inválida!");
                }
            } catch (Exception e) {
                System.out.println("❌ Erro: " + e.getMessage());
            }
        }
        scanner.close();
    }//executarMenuInterativo

    private static void analisarNovaAcao(Scanner scanner, Portefolio portefolio, double taxaTesouro) {
        System.out.print("\n📈 Digite o ticker da ação: ");
        String ticker = scanner.nextLine().toUpperCase().trim();

        if (ticker.isEmpty()) return;

        try {
            Map<String, Object> dados = ClienteApi.obterDadosAcao(ticker);
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