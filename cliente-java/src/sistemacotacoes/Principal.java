// Principal.java
package sistemacotacoes;

import sistemacotacoes.api.ClienteApi;
import sistemacotacoes.gestao.Carteira;
import sistemacotacoes.modelo.Ativo;
import sistemacotacoes.enums.TipoAtivo;
import sistemacotacoes.enums.CriterioOrdenacao;
import sistemacotacoes.util.BoolEMensagem;
import java.util.Scanner;

/**
 * Classe principal do Sistema de Cotações.
 * Interface de linha de comandos para interação com o utilizador.
 */
public class Principal {

    private static Carteira carteira = new Carteira("Rodrigo Silva", 20);
    private static ClienteApi api = new ClienteApi();
    private static Scanner scanner = new Scanner(System.in);

    //--------------------------------------------------
    // Main
    //--------------------------------------------------
    public static void main(String[] args) {
        mostrarBanner();
        
        // Verificar se API está disponível
        if (!api.apiDisponivel()) {
            System.out.println("⚠️  AVISO: API Python não está a correr!");
            System.out.println("   Execute: python app.py");
            System.out.println("   (Continuando em modo demonstração...)\n");
        }//if

        menuPrincipal();
        
        System.out.println("\n👋 Obrigado por usar o Sistema de Cotações!");
    }//main

    //--------------------------------------------------
    // Menu Principal
    //--------------------------------------------------
    private static void menuPrincipal() {
        while (true) {
            System.out.println("\n╔════════════════════════════════════╗");
            System.out.println("║     📊 SISTEMA DE COTAÇÕES         ║");
            System.out.println("╠════════════════════════════════════╣");
            System.out.println("║  1. Adicionar Ativo                ║");
            System.out.println("║  2. Remover Ativo                  ║");
            System.out.println("║  3. Ver Carteira                   ║");
            System.out.println("║  4. Ordenar Carteira               ║");
            System.out.println("║  5. Filtrar por Tipo               ║");
            System.out.println("║  6. Ver Resumo/Estatísticas        ║");
            System.out.println("║  0. Sair                           ║");
            System.out.println("╚════════════════════════════════════╝");
            System.out.print("Escolha: ");

            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1": menuAdicionar(); break;
                case "2": menuRemover(); break;
                case "3": carteira.listar(); break;
                case "4": menuOrdenar(); break;
                case "5": menuFiltrar(); break;
                case "6": carteira.listarResumo(); break;
                case "0": return;
                default: System.out.println("❌ Opção inválida!");
            }//switch
        }//while
    }//menuPrincipal

    //--------------------------------------------------
    // Submenu: Adicionar
    //--------------------------------------------------
    private static void menuAdicionar() {
        System.out.println("\n── ADICIONAR ATIVO ──");
        System.out.println("1. Ação (ex: AAPL, MSFT)");
        System.out.println("2. Criptomoeda (ex: BTC-USD)");
        System.out.println("3. ETF (ex: IVV, QQQ)");
        System.out.println("4. Detecção Automática");
        System.out.print("Tipo: ");
        
        String tipo = scanner.nextLine().trim();
        
        System.out.print("Ticker: ");
        String ticker = scanner.nextLine().trim().toUpperCase();
        
        if (ticker.isEmpty()) {
            System.out.println("❌ Ticker inválido!");
            return;
        }//if

        Ativo ativo = null;
        
        switch (tipo) {
            case "1": ativo = api.buscarAtivo(ticker, TipoAtivo.ACAO); break;
            case "2": ativo = api.buscarAtivo(ticker, TipoAtivo.CRIPTO); break;
            case "3": ativo = api.buscarAtivo(ticker, TipoAtivo.ETF); break;
            case "4": ativo = api.buscarAtivoAuto(ticker); break;
            default: System.out.println("❌ Tipo inválido!"); return;
        }//switch

        if (ativo != null) {
            BoolEMensagem resultado = carteira.adicionar(ativo);
            System.out.println(resultado);
        }//if
    }//menuAdicionar

    //--------------------------------------------------
    // Submenu: Remover
    //--------------------------------------------------
    private static void menuRemover() {
        if (carteira.estaVazia()) {
            System.out.println("❌ Carteira vazia!");
            return;
        }//if
        
        System.out.print("Ticker a remover: ");
        String ticker = scanner.nextLine().trim().toUpperCase();
        
        BoolEMensagem resultado = carteira.removerPorTicker(ticker);
        System.out.println(resultado);
    }//menuRemover

    //--------------------------------------------------
    // Submenu: Ordenar
    //--------------------------------------------------
    private static void menuOrdenar() {
        System.out.println("\n── ORDENAR POR ──");
        System.out.println("1. Ticker (A-Z)");
        System.out.println("2. Preço (maior primeiro)");
        System.out.println("3. Variação (pior primeiro)");
        System.out.println("4. Risco (maior primeiro)");
        System.out.print("Critério: ");
        
        String criterio = scanner.nextLine().trim();
        
        switch (criterio) {
            case "1": 
                carteira.ordenarPorTicker();
                System.out.println("✅ Ordenado por Ticker");
                break;
            case "2": 
                carteira.ordenarPorPreco(); 
                System.out.println("✅ Ordenado por Preço");
                break;
            case "3": 
                carteira.ordenarPorVariacao(); 
                System.out.println("✅ Ordenado por Variação");
                break;
            case "4": 
                carteira.ordenarPorRisco(); 
                System.out.println("✅ Ordenado por Risco");
                break;
            default: 
                System.out.println("❌ Critério inválido!");
        }//switch
        
        carteira.listar();
    }//menuOrdenar

    //--------------------------------------------------
    // Submenu: Filtrar
    //--------------------------------------------------
    private static void menuFiltrar() {
        System.out.println("\n── FILTRAR POR ──");
        System.out.println("1. Ações");
        System.out.println("2. Criptomoedas");
        System.out.println("3. ETFs");
        System.out.println("4. Em Alta (>3%)");
        System.out.println("5. Em Queda (<-3%)");
        System.out.print("Filtro: ");
        
        String filtro = scanner.nextLine().trim();
        
        java.util.List<Ativo> resultado;
        String descricao;
        
        switch (filtro) {
            case "1": 
                resultado = carteira.filtrarPorTipo(TipoAtivo.ACAO);
                descricao = "AÇÕES";
                break;
            case "2": 
                resultado = carteira.filtrarPorTipo(TipoAtivo.CRIPTO);
                descricao = "CRIPTOMOEDAS";
                break;
            case "3": 
                resultado = carteira.filtrarPorTipo(TipoAtivo.ETF);
                descricao = "ETFs";
                break;
            case "4": 
                resultado = carteira.filtrarEmAlta();
                descricao = "EM ALTA";
                break;
            case "5": 
                resultado = carteira.filtrarEmQueda();
                descricao = "EM QUEDA";
                break;
            default: 
                System.out.println("❌ Filtro inválido!");
                return;
        }//switch
        
        System.out.println("\n── " + descricao + " (" + resultado.size() + " encontrados) ──");
        for (Ativo a : resultado) {
            System.out.println("  " + a);
        }//for
    }//menuFiltrar

    //--------------------------------------------------
    // Banner Inicial
    //--------------------------------------------------
    private static void mostrarBanner() {
        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                               ║");
        System.out.println("║     📈  RODRIGO SILVA - SISTEMA DE COTAÇÕES  📉              ║");
        System.out.println("║                                                               ║");
        System.out.println("║     Projeto Final POO - Análise de Ativos Financeiros        ║");
        System.out.println("║                                                               ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }//mostrarBanner

}//classe Principal
