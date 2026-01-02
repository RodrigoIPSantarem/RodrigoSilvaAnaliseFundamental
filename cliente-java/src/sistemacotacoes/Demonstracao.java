// Demonstracao.java
package sistemacotacoes;

import sistemacotacoes.modelo.*;
import sistemacotacoes.gestao.Carteira;
import sistemacotacoes.enums.*;
import sistemacotacoes.fabrica.FabricaAtivos;
import sistemacotacoes.util.BoolEMensagem;
import java.util.*;

/**
 * Classe de demonstração que mostra TODOS os conceitos POO implementados.
 * Executa sem necessidade da API Python.
 */
public class Demonstracao {
    
    public static void main(String[] args) {
        mostrarBanner();
        
        demonstrarHerancaPolimorfismo();
        demonstrarInterface();
        demonstrarComposicao();
        demonstrarComparators();
        demonstrarFactory();
        demonstrarEnums();
        demonstrarBoolEMensagem();
        
        mostrarResumoConceitos();
    }//main

    //--------------------------------------------------
    // 1. HERANÇA + POLIMORFISMO
    //--------------------------------------------------
    private static void demonstrarHerancaPolimorfismo() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  1️⃣  HERANÇA + POLIMORFISMO");
        System.out.println("═".repeat(70));
        
        // Criar objetos de diferentes tipos (todos são Ativo)
        Ativo apple = new Acao("AAPL", "Apple Inc.", 230.50, 1.25, 54000000);
        Ativo bitcoin = new Cripto("BTC-USD", "Bitcoin", 98500.00, 4.80, 28000000000L);
        Ativo sp500 = new ETF("IVV", "iShares S&P 500", 585.20, 0.45, 5000000);
        
        System.out.println("\n📌 Lista polimórfica (todos são Ativo, mas tipos diferentes):");
        System.out.println("─".repeat(70));
        
        List<Ativo> ativos = Arrays.asList(apple, bitcoin, sp500);
        for (Ativo a : ativos) {
            System.out.printf("  %s → Tipo Real: %s\n", a.getTicker(), a.getClass().getSimpleName());
        }//for
        
        // Demonstrar polimorfismo no cálculo de risco
        System.out.println("\n📌 POLIMORFISMO: calcularRisco() com mesma variação (-5%):");
        System.out.println("─".repeat(70));
        
        Ativo acaoTeste = new Acao("TEST", "Teste", 100, -5.0, 1000);
        Ativo criptoTeste = new Cripto("TEST-USD", "Teste", 100, -5.0, 1000);
        Ativo etfTeste = new ETF("TESTETF", "Teste", 100, -5.0, 1000);
        
        System.out.printf("  • Ação:   Risco = %.1f  (variação × 1.0)\n", acaoTeste.calcularRisco());
        System.out.printf("  • Cripto: Risco = %.1f (variação × 3.0) ← MAIS ARRISCADO!\n", criptoTeste.calcularRisco());
        System.out.printf("  • ETF:    Risco = %.1f  (variação × 0.5) ← MENOS ARRISCADO!\n", etfTeste.calcularRisco());
    }//demonstrarHerancaPolimorfismo

    //--------------------------------------------------
    // 2. INTERFACE
    //--------------------------------------------------
    private static void demonstrarInterface() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  2️⃣  INTERFACE (Analisavel)");
        System.out.println("═".repeat(70));
        
        System.out.println("\n📌 Todos os Ativos implementam a interface Analisavel:");
        System.out.println("   - calcularRisco()");
        System.out.println("   - obterRecomendacao()");
        
        Ativo[] ativos = {
            new Acao("KO", "Coca-Cola", 62.40, -6.50, 12000000),
            new Cripto("ETH-USD", "Ethereum", 3450.00, -8.20, 15000000000L),
            new ETF("QQQ", "Nasdaq 100", 520.30, -2.10, 4500000)
        };
        
        System.out.println("\n📌 obterRecomendacao() - cada tipo retorna algo diferente:");
        System.out.println("─".repeat(70));
        
        for (Ativo a : ativos) {
            System.out.printf("  %s (%s): %s\n", 
                a.getTicker(), 
                a.obterTipo().getNome(),
                a.obterRecomendacao());
        }//for
    }//demonstrarInterface

    //--------------------------------------------------
    // 3. COMPOSIÇÃO
    //--------------------------------------------------
    private static void demonstrarComposicao() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  3️⃣  COMPOSIÇÃO (Carteira contém List<Ativo>)");
        System.out.println("═".repeat(70));
        
        Carteira carteira = new Carteira("Demonstração POO", 10);
        
        System.out.println("\n📌 Carteira é composta por uma lista de Ativos:");
        
        carteira.adicionar(new Acao("AAPL", "Apple", 230.50, 1.25, 54000000));
        carteira.adicionar(new Cripto("BTC-USD", "Bitcoin", 98500.00, 4.80, 28000000000L));
        carteira.adicionar(new ETF("IVV", "S&P 500", 585.20, 0.45, 5000000));
        carteira.adicionar(new Acao("KO", "Coca-Cola", 62.40, -6.50, 12000000));
        
        carteira.listar();
    }//demonstrarComposicao

    //--------------------------------------------------
    // 4. COMPARATORS (Enum CriterioOrdenacao)
    //--------------------------------------------------
    private static void demonstrarComparators() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  4️⃣  COMPARATORS (CriterioOrdenacao com inner classes)");
        System.out.println("═".repeat(70));
        
        List<Ativo> lista = new ArrayList<>(Arrays.asList(
            new Acao("MSFT", "Microsoft", 420.75, -2.30, 32000000),
            new Acao("AAPL", "Apple", 230.50, 1.25, 54000000),
            new Cripto("BTC-USD", "Bitcoin", 98500.00, 4.80, 28000000000L),
            new ETF("IVV", "S&P 500", 585.20, 0.45, 5000000)
        ));
        
        System.out.println("\n📌 Ordenar por TICKER (A-Z):");
        lista.sort(CriterioOrdenacao.POR_TICKER.getComparador());
        for (Ativo a : lista) System.out.println("  → " + a.getTicker());
        
        System.out.println("\n📌 Ordenar por PREÇO (decrescente):");
        lista.sort(CriterioOrdenacao.POR_PRECO_DESC.getComparador());
        for (Ativo a : lista) System.out.printf("  → %s ($%.2f)\n", a.getTicker(), a.getPreco());
        
        System.out.println("\n📌 Ordenar por RISCO (crescente):");
        lista.sort(CriterioOrdenacao.POR_RISCO.getComparador());
        for (Ativo a : lista) System.out.printf("  → %s (Risco: %.2f)\n", a.getTicker(), a.calcularRisco());
    }//demonstrarComparators

    //--------------------------------------------------
    // 5. FACTORY PATTERN
    //--------------------------------------------------
    private static void demonstrarFactory() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  5️⃣  FACTORY PATTERN (FabricaAtivos)");
        System.out.println("═".repeat(70));
        
        System.out.println("\n📌 Criar ativos usando a Factory (não o construtor direto):");
        System.out.println("─".repeat(70));
        
        // Criação com tipo explícito
        Ativo a1 = FabricaAtivos.criarAtivo(TipoAtivo.ACAO, "NVDA", "NVIDIA", 140.0, 2.5, 50000000);
        Ativo a2 = FabricaAtivos.criarAtivo(TipoAtivo.CRIPTO, "SOL-USD", "Solana", 180.0, 5.0, 2000000000L);
        
        System.out.printf("  FabricaAtivos.criarAtivo(ACAO, ...) → %s\n", a1.getClass().getSimpleName());
        System.out.printf("  FabricaAtivos.criarAtivo(CRIPTO, ...) → %s\n", a2.getClass().getSimpleName());
        
        System.out.println("\n📌 Detecção automática de tipo pelo ticker:");
        System.out.println("─".repeat(70));
        
        String[] tickers = {"GOOGL", "BTC-USD", "QQQ", "ETH-USD", "TSLA", "IVV"};
        for (String t : tickers) {
            TipoAtivo tipo = FabricaAtivos.detectarTipo(t);
            System.out.printf("  %s → detectado como %s\n", t, tipo.getNome());
        }//for
    }//demonstrarFactory

    //--------------------------------------------------
    // 6. ENUMS
    //--------------------------------------------------
    private static void demonstrarEnums() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  6️⃣  ENUMS (TipoAtivo, CriterioOrdenacao)");
        System.out.println("═".repeat(70));
        
        System.out.println("\n📌 Enum TipoAtivo (com atributos):");
        System.out.println("─".repeat(70));
        
        for (TipoAtivo t : TipoAtivo.values()) {
            System.out.printf("  %s: %s (Multiplicador Risco: %.1f×)\n", 
                t.name(), t.getDescricao(), t.getMultiplicadorRisco());
        }//for
        
        System.out.println("\n📌 Enum CriterioOrdenacao (com Comparators):");
        System.out.println("─".repeat(70));
        
        for (CriterioOrdenacao c : CriterioOrdenacao.values()) {
            System.out.printf("  %s → Comparator<%s>\n", c.name(), "Ativo");
        }//for
    }//demonstrarEnums

    //--------------------------------------------------
    // 7. BoolEMensagem
    //--------------------------------------------------
    private static void demonstrarBoolEMensagem() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  7️⃣  BoolEMensagem (Retornos compostos)");
        System.out.println("═".repeat(70));
        
        Carteira c = new Carteira("Teste", 2);
        
        System.out.println("\n📌 Métodos retornam BoolEMensagem (sucesso + mensagem):");
        System.out.println("─".repeat(70));
        
        BoolEMensagem r1 = c.adicionar(new Acao("AAPL", "Apple", 230, 1.0, 1000));
        System.out.println("  " + r1);
        
        BoolEMensagem r2 = c.adicionar(new Acao("MSFT", "Microsoft", 420, -2.0, 2000));
        System.out.println("  " + r2);
        
        // Carteira cheia (capacidade = 2)
        BoolEMensagem r3 = c.adicionar(new Acao("GOOGL", "Google", 180, 0.5, 3000));
        System.out.println("  " + r3);
        
        // Duplicado
        BoolEMensagem r4 = c.adicionar(new Acao("AAPL", "Apple", 230, 1.0, 1000));
        System.out.println("  " + r4);
    }//demonstrarBoolEMensagem

    //--------------------------------------------------
    // Banner e Resumo
    //--------------------------------------------------
    private static void mostrarBanner() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                       ║");
        System.out.println("║     📚  DEMONSTRAÇÃO DE CONCEITOS POO - RODRIGO SILVA                ║");
        System.out.println("║                                                                       ║");
        System.out.println("║     Sistema de Cotações - Projeto Final                              ║");
        System.out.println("║                                                                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════════════╝");
    }//mostrarBanner

    private static void mostrarResumoConceitos() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  ✅  RESUMO - CONCEITOS POO DEMONSTRADOS");
        System.out.println("═".repeat(70));
        System.out.println("  ┌─────────────────────────────────────────────────────────────────┐");
        System.out.println("  │  ✅ Classe Abstrata      → Ativo (não instanciável)            │");
        System.out.println("  │  ✅ Herança              → Acao, Cripto, ETF extends Ativo     │");
        System.out.println("  │  ✅ Polimorfismo         → calcularRisco() diferente por tipo  │");
        System.out.println("  │  ✅ Interface            → Analisavel                          │");
        System.out.println("  │  ✅ Composição           → Carteira contém List<Ativo>         │");
        System.out.println("  │  ✅ Comparable           → Ordenação natural por Ticker        │");
        System.out.println("  │  ✅ Comparators          → CriterioOrdenacao (inner classes)   │");
        System.out.println("  │  ✅ Factory Pattern      → FabricaAtivos.criarAtivo()          │");
        System.out.println("  │  ✅ Enums com atributos  → TipoAtivo, CriterioOrdenacao        │");
        System.out.println("  │  ✅ BoolEMensagem        → Retornos compostos (estilo prof.)   │");
        System.out.println("  │  ✅ Encapsulamento       → Prefixos m/p, protected, getters    │");
        System.out.println("  │  ✅ Consumo API          → ClienteApi + Flask/yfinance         │");
        System.out.println("  └─────────────────────────────────────────────────────────────────┘");
        System.out.println();
    }//mostrarResumoConceitos

}//classe Demonstracao
