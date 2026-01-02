// Principal.java
package sistemacotacoes;

import sistemacotacoes.api.ClienteApi;
import sistemacotacoes.gestao.Carteira;
import sistemacotacoes.modelo.Ativo;
import sistemacotacoes.enums.TipoAtivo;
import sistemacotacoes.enums.CriterioOrdenacao;
import sistemacotacoes.util.BoolEMensagem;
import sistemacotacoes.util.GestorFicheiros;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principal do Sistema de Cotações.
 * Interface de linha de comandos para interação com o utilizador.
 * 
 * FUNCIONALIDADES:
 * - Adicionar/Remover ativos
 * - Ver/Ordenar/Filtrar carteira
 * - Guardar/Carregar carteira de ficheiros
 */
public class Principal {

    private static Carteira carteira = new Carteira("Rodrigo Silva", 20);
    private static ClienteApi api = new ClienteApi();
    private static Scanner scanner = new Scanner(System.in);
    
    // Diretório para guardar ficheiros (atual)
    private static final String DIRETORIO_DADOS = ".";

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
        
        // Perguntar se quer guardar antes de sair
        if (!carteira.estaVazia()) {
            System.out.print("\n💾 Deseja guardar a carteira antes de sair? (S/N): ");
            String resposta = scanner.nextLine().trim().toUpperCase();
            if (resposta.equals("S") || resposta.equals("SIM")) {
                menuGuardar();
            }//if
        }//if
        
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
            System.out.println("║  ─────────────────────────────     ║");
            System.out.println("║  7. 💾 Guardar Carteira            ║");
            System.out.println("║  8. 📂 Carregar Carteira           ║");
            System.out.println("║  ─────────────────────────────     ║");
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
                case "7": menuGuardar(); break;
                case "8": menuCarregar(); break;
                case "0": return;
                default: System.out.println("❌ Opção inválida! Escolha entre 0-8.");
            }//switch
        }//while
    }//menuPrincipal

    //--------------------------------------------------
    // Submenu: Guardar Carteira
    //--------------------------------------------------
    private static void menuGuardar() {
        if (carteira.estaVazia()) {
            System.out.println("❌ Carteira vazia! Nada para guardar.");
            return;
        }//if
        
        while (true) {
            System.out.println("\n── 💾 GUARDAR CARTEIRA ──");
            System.out.println("1. Guardar como CSV (compatível com Excel)");
            System.out.println("2. Guardar como TXT (formato legível)");
            System.out.println("0. Voltar");
            System.out.print("Formato: ");
            
            String formato = scanner.nextLine().trim();
            
            if (formato.equals("0")) {
                return;
            }//if
            
            if (!formato.equals("1") && !formato.equals("2")) {
                System.out.println("❌ Formato inválido! Escolha 1, 2 ou 0.");
                continue;
            }//if
            
            // Pedir nome do ficheiro
            System.out.print("Nome do ficheiro (sem extensão): ");
            String nomeFicheiro = scanner.nextLine().trim();
            
            if (nomeFicheiro.isEmpty()) {
                nomeFicheiro = "carteira_rodrigo";
            }//if
            
            // Remover caracteres inválidos
            nomeFicheiro = nomeFicheiro.replaceAll("[^a-zA-Z0-9_-]", "_");
            
            BoolEMensagem resultado;
            
            if (formato.equals("1")) {
                resultado = GestorFicheiros.guardarCSV(carteira, nomeFicheiro);
            } else {
                resultado = GestorFicheiros.guardarTXT(carteira, nomeFicheiro);
            }//else
            
            System.out.println(resultado.getMensagem());
            
            if (resultado.sucesso()) {
                return;
            }//if
            // Se falhou, o loop continua
        }//while
    }//menuGuardar

    //--------------------------------------------------
    // Submenu: Carregar Carteira
    //--------------------------------------------------
    private static void menuCarregar() {
        while (true) {
            System.out.println("\n── 📂 CARREGAR CARTEIRA ──");
            
            // Listar ficheiros disponíveis
            List<String> ficheiros = GestorFicheiros.listarFicheirosCarteira(DIRETORIO_DADOS);
            
            if (!ficheiros.isEmpty()) {
                System.out.println("Ficheiros encontrados:");
                for (String f : ficheiros) {
                    System.out.println("  • " + f);
                }//for
                System.out.println();
            }//if
            
            System.out.println("1. Carregar ficheiro CSV");
            System.out.println("2. Carregar ficheiro TXT");
            System.out.println("0. Voltar");
            System.out.print("Formato: ");
            
            String formato = scanner.nextLine().trim();
            
            if (formato.equals("0")) {
                return;
            }//if
            
            if (!formato.equals("1") && !formato.equals("2")) {
                System.out.println("❌ Formato inválido! Escolha 1, 2 ou 0.");
                continue;
            }//if
            
            // Pedir nome do ficheiro
            String extensao = formato.equals("1") ? ".csv" : ".txt";
            System.out.print("Nome do ficheiro (com ou sem " + extensao + "): ");
            String nomeFicheiro = scanner.nextLine().trim();
            
            if (nomeFicheiro.isEmpty()) {
                System.out.println("❌ Nome do ficheiro não pode estar vazio!");
                continue;
            }//if
            
            // Avisar se carteira não está vazia
            if (!carteira.estaVazia()) {
                System.out.println("\n⚠️  ATENÇÃO: A carteira já contém " + carteira.getQuantidade() + " ativo(s).");
                System.out.println("Os ativos carregados serão ADICIONADOS aos existentes.");
                System.out.print("Continuar? (S/N): ");
                String confirma = scanner.nextLine().trim().toUpperCase();
                if (!confirma.equals("S") && !confirma.equals("SIM")) {
                    continue;
                }//if
            }//if
            
            BoolEMensagem resultado;
            
            if (formato.equals("1")) {
                resultado = GestorFicheiros.carregarCSV(carteira, nomeFicheiro);
            } else {
                resultado = GestorFicheiros.carregarTXT(carteira, nomeFicheiro);
            }//else
            
            System.out.println(resultado.getMensagem());
            
            if (resultado.sucesso()) {
                // Mostrar carteira após carregar
                carteira.listar();
                return;
            }//if
            // Se falhou, o loop continua
        }//while
    }//menuCarregar

    //--------------------------------------------------
    // Submenu: Adicionar (COM LOOP)
    //--------------------------------------------------
    private static void menuAdicionar() {
        while (true) {
            System.out.println("\n── ADICIONAR ATIVO ──");
            System.out.println("1. Ação (ex: AAPL, MSFT)");
            System.out.println("2. Criptomoeda (ex: BTC-USD)");
            System.out.println("3. ETF (ex: IVV, QQQ)");
            System.out.println("4. Detecção Automática");
            System.out.println("0. Voltar");
            System.out.print("Tipo: ");
            
            String tipo = scanner.nextLine().trim();
            
            // Opção de voltar
            if (tipo.equals("0")) {
                return;
            }//if
            
            // Validar tipo
            if (!tipo.equals("1") && !tipo.equals("2") && !tipo.equals("3") && !tipo.equals("4")) {
                System.out.println("❌ Tipo inválido! Escolha entre 1-4 ou 0 para voltar.");
                continue; // Volta ao início do loop
            }//if
            
            // Pedir ticker (com loop de validação)
            String ticker = pedirTicker();
            if (ticker == null) {
                continue; // Utilizador cancelou, volta ao menu de tipo
            }//if
            
            // Buscar ativo na API
            Ativo ativo = null;
            
            switch (tipo) {
                case "1": ativo = api.buscarAtivo(ticker, TipoAtivo.ACAO); break;
                case "2": ativo = api.buscarAtivo(ticker, TipoAtivo.CRIPTO); break;
                case "3": ativo = api.buscarAtivo(ticker, TipoAtivo.ETF); break;
                case "4": ativo = api.buscarAtivoAuto(ticker); break;
            }//switch

            if (ativo != null) {
                BoolEMensagem resultado = carteira.adicionar(ativo);
                System.out.println(resultado);
                
                // Perguntar se quer adicionar mais
                if (!continuarAdicionar()) {
                    return;
                }//if
            } else {
                System.out.println("❌ Não foi possível obter dados. Tente novamente.");
            }//else
        }//while
    }//menuAdicionar

    //--------------------------------------------------
    // Pedir Ticker (com validação)
    //--------------------------------------------------
    private static String pedirTicker() {
        while (true) {
            System.out.print("Ticker (ou 0 para voltar): ");
            String ticker = scanner.nextLine().trim().toUpperCase();
            
            if (ticker.equals("0")) {
                return null; // Sinaliza cancelamento
            }//if
            
            if (ticker.isEmpty()) {
                System.out.println("❌ Ticker não pode estar vazio!");
                continue;
            }//if
            
            // Validar que não contém vírgulas ou espaços (múltiplos tickers)
            if (ticker.contains(",") || ticker.contains(" ")) {
                System.out.println("❌ Insira apenas UM ticker de cada vez!");
                continue;
            }//if
            
            // Validar caracteres válidos (letras, números, hífen)
            if (!ticker.matches("[A-Z0-9\\-\\.]+")) {
                System.out.println("❌ Ticker contém caracteres inválidos!");
                continue;
            }//if
            
            return ticker;
        }//while
    }//pedirTicker

    //--------------------------------------------------
    // Perguntar se quer continuar a adicionar
    //--------------------------------------------------
    private static boolean continuarAdicionar() {
        System.out.print("\nAdicionar outro ativo? (S/N): ");
        String resposta = scanner.nextLine().trim().toUpperCase();
        return resposta.equals("S") || resposta.equals("SIM");
    }//continuarAdicionar

    //--------------------------------------------------
    // Submenu: Remover (COM LOOP)
    //--------------------------------------------------
    private static void menuRemover() {
        while (true) {
            if (carteira.estaVazia()) {
                System.out.println("❌ Carteira vazia! Nada para remover.");
                return;
            }//if
            
            // Mostrar ativos atuais
            System.out.println("\n── REMOVER ATIVO ──");
            System.out.println("Ativos na carteira:");
            for (Ativo a : carteira.getAtivos()) {
                System.out.println("  • " + a.getTicker());
            }//for
            
            System.out.print("\nTicker a remover (ou 0 para voltar): ");
            String ticker = scanner.nextLine().trim().toUpperCase();
            
            if (ticker.equals("0")) {
                return;
            }//if
            
            if (ticker.isEmpty()) {
                System.out.println("❌ Ticker não pode estar vazio!");
                continue;
            }//if
            
            BoolEMensagem resultado = carteira.removerPorTicker(ticker);
            System.out.println(resultado);
            
            if (resultado.sucesso()) {
                // Perguntar se quer remover mais
                if (carteira.estaVazia()) {
                    System.out.println("Carteira agora está vazia.");
                    return;
                }//if
                
                System.out.print("Remover outro? (S/N): ");
                String resposta = scanner.nextLine().trim().toUpperCase();
                if (!resposta.equals("S") && !resposta.equals("SIM")) {
                    return;
                }//if
            }//if
            // Se falhou, o loop continua automaticamente
        }//while
    }//menuRemover

    //--------------------------------------------------
    // Submenu: Ordenar (COM LOOP)
    //--------------------------------------------------
    private static void menuOrdenar() {
        if (carteira.estaVazia()) {
            System.out.println("❌ Carteira vazia! Nada para ordenar.");
            return;
        }//if
        
        while (true) {
            System.out.println("\n── ORDENAR POR ──");
            System.out.println("1. Ticker (A-Z)");
            System.out.println("2. Preço (maior primeiro)");
            System.out.println("3. Variação (pior primeiro)");
            System.out.println("4. Risco (maior primeiro)");
            System.out.println("0. Voltar");
            System.out.print("Critério: ");
            
            String criterio = scanner.nextLine().trim();
            
            switch (criterio) {
                case "0":
                    return;
                case "1": 
                    carteira.ordenarPorTicker();
                    System.out.println("✅ Ordenado por Ticker");
                    carteira.listar();
                    return;
                case "2": 
                    carteira.ordenarPorPreco(); 
                    System.out.println("✅ Ordenado por Preço");
                    carteira.listar();
                    return;
                case "3": 
                    carteira.ordenarPorVariacao(); 
                    System.out.println("✅ Ordenado por Variação");
                    carteira.listar();
                    return;
                case "4": 
                    carteira.ordenarPorRisco(); 
                    System.out.println("✅ Ordenado por Risco");
                    carteira.listar();
                    return;
                default: 
                    System.out.println("❌ Critério inválido! Escolha entre 1-4 ou 0 para voltar.");
                    // Loop continua
            }//switch
        }//while
    }//menuOrdenar

    //--------------------------------------------------
    // Submenu: Filtrar (COM LOOP)
    //--------------------------------------------------
    private static void menuFiltrar() {
        if (carteira.estaVazia()) {
            System.out.println("❌ Carteira vazia! Nada para filtrar.");
            return;
        }//if
        
        while (true) {
            System.out.println("\n── FILTRAR POR ──");
            System.out.println("1. Ações");
            System.out.println("2. Criptomoedas");
            System.out.println("3. ETFs");
            System.out.println("4. Em Alta (>3%)");
            System.out.println("5. Em Queda (<-3%)");
            System.out.println("0. Voltar");
            System.out.print("Filtro: ");
            
            String filtro = scanner.nextLine().trim();
            
            if (filtro.equals("0")) {
                return;
            }//if
            
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
                    System.out.println("❌ Filtro inválido! Escolha entre 1-5 ou 0 para voltar.");
                    continue; // Loop continua
            }//switch
            
            System.out.println("\n── " + descricao + " (" + resultado.size() + " encontrados) ──");
            if (resultado.isEmpty()) {
                System.out.println("  (Nenhum ativo encontrado com este filtro)");
            } else {
                for (Ativo a : resultado) {
                    System.out.println("  " + a);
                }//for
            }//else
            
            // Perguntar se quer aplicar outro filtro
            System.out.print("\nAplicar outro filtro? (S/N): ");
            String resposta = scanner.nextLine().trim().toUpperCase();
            if (!resposta.equals("S") && !resposta.equals("SIM")) {
                return;
            }//if
        }//while
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
