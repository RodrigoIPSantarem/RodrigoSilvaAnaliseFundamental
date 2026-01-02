// GestorFicheiros.java
package sistemacotacoes.util;

import sistemacotacoes.modelo.*;
import sistemacotacoes.enums.TipoAtivo;
import sistemacotacoes.fabrica.FabricaAtivos;
import sistemacotacoes.gestao.Carteira;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitária para guardar e carregar carteiras de/para ficheiros.
 * 
 * Demonstra: FILE I/O + SERIALIZAÇÃO + TRATAMENTO DE EXCEÇÕES
 * 
 * Formatos suportados:
 * - CSV (.csv) - Compatível com Excel
 * - Texto (.txt) - Formato próprio legível
 */
public class GestorFicheiros {

    // Separador para ficheiros CSV
    private static final String SEPARADOR_CSV = ";";
    
    // Extensões suportadas
    public static final String EXTENSAO_CSV = ".csv";
    public static final String EXTENSAO_TXT = ".txt";

    //--------------------------------------------------
    // GUARDAR CARTEIRA
    //--------------------------------------------------
    
    /**
     * Guarda a carteira num ficheiro CSV.
     * Formato: TICKER;TIPO;NOME;PRECO;VARIACAO;VOLUME
     * 
     * @param pCarteira A carteira a guardar
     * @param pCaminhoFicheiro Caminho completo do ficheiro
     * @return BoolEMensagem com resultado da operação
     */
    public static BoolEMensagem guardarCSV(Carteira pCarteira, String pCaminhoFicheiro) {
        // Garantir extensão .csv
        String caminho = garantirExtensao(pCaminhoFicheiro, EXTENSAO_CSV);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminho))) {
            // Escrever cabeçalho
            writer.println("TICKER;TIPO;NOME;PRECO;VARIACAO;VOLUME");
            
            // Escrever cada ativo
            for (Ativo a : pCarteira.getAtivos()) {
                String linha = String.format("%s;%s;%s;%.2f;%.4f;%d",
                    a.getTicker(),
                    a.obterTipo().name(),  // ACAO, CRIPTO, ETF
                    a.getNome(),
                    a.getPreco(),
                    a.getVariacao(),
                    a.getVolume()
                );
                writer.println(linha);
            }//for
            
            return new BoolEMensagem(true, 
                String.format("✅ Carteira guardada em: %s (%d ativos)", 
                    caminho, pCarteira.getQuantidade()));
                    
        } catch (IOException e) {
            return new BoolEMensagem(false, 
                "❌ Erro ao guardar ficheiro: " + e.getMessage());
        }//catch
    }//guardarCSV

    /**
     * Guarda a carteira num ficheiro de texto formatado.
     * Mais legível para humanos.
     */
    public static BoolEMensagem guardarTXT(Carteira pCarteira, String pCaminhoFicheiro) {
        String caminho = garantirExtensao(pCaminhoFicheiro, EXTENSAO_TXT);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(caminho))) {
            // Cabeçalho
            writer.println("╔════════════════════════════════════════════════════════════════╗");
            writer.println("║          CARTEIRA - RODRIGO SILVA ANÁLISE FUNDAMENTAL         ║");
            writer.println("╚════════════════════════════════════════════════════════════════╝");
            writer.println();
            writer.printf("Nome: %s%n", pCarteira.getNome());
            writer.printf("Total de ativos: %d%n", pCarteira.getQuantidade());
            writer.println();
            writer.println("─".repeat(70));
            writer.printf("%-12s %-8s %-20s %12s %10s%n", 
                "TICKER", "TIPO", "NOME", "PREÇO", "VAR%");
            writer.println("─".repeat(70));
            
            // Dados
            for (Ativo a : pCarteira.getAtivos()) {
                writer.printf("%-12s %-8s %-20s %12.2f %+9.2f%%%n",
                    a.getTicker(),
                    a.obterTipo().getNome(),
                    truncar(a.getNome(), 20),
                    a.getPreco(),
                    a.getVariacao()
                );
            }//for
            
            writer.println("─".repeat(70));
            writer.println();
            writer.println("[DADOS PARA IMPORTAÇÃO - NÃO EDITAR ABAIXO DESTA LINHA]");
            writer.println("@DATA_START");
            
            // Dados em formato parseável
            for (Ativo a : pCarteira.getAtivos()) {
                writer.printf("%s|%s|%s|%.2f|%.4f|%d%n",
                    a.getTicker(),
                    a.obterTipo().name(),
                    a.getNome(),
                    a.getPreco(),
                    a.getVariacao(),
                    a.getVolume()
                );
            }//for
            
            writer.println("@DATA_END");
            
            return new BoolEMensagem(true, 
                String.format("✅ Carteira guardada em: %s (%d ativos)", 
                    caminho, pCarteira.getQuantidade()));
                    
        } catch (IOException e) {
            return new BoolEMensagem(false, 
                "❌ Erro ao guardar ficheiro: " + e.getMessage());
        }//catch
    }//guardarTXT

    //--------------------------------------------------
    // CARREGAR CARTEIRA
    //--------------------------------------------------
    
    /**
     * Carrega ativos de um ficheiro CSV para a carteira.
     */
    public static BoolEMensagem carregarCSV(Carteira pCarteira, String pCaminhoFicheiro) {
        String caminho = garantirExtensao(pCaminhoFicheiro, EXTENSAO_CSV);
        File ficheiro = new File(caminho);
        
        if (!ficheiro.exists()) {
            return new BoolEMensagem(false, 
                "❌ Ficheiro não encontrado: " + caminho);
        }//if
        
        int contadorAdicionados = 0;
        int contadorErros = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ficheiro))) {
            String linha;
            boolean primeiraLinha = true;
            
            while ((linha = reader.readLine()) != null) {
                // Ignorar cabeçalho
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue;
                }//if
                
                // Ignorar linhas vazias
                if (linha.trim().isEmpty()) continue;
                
                try {
                    Ativo ativo = parsearLinhaCSV(linha);
                    if (ativo != null) {
                        BoolEMensagem resultado = pCarteira.adicionar(ativo);
                        if (resultado.sucesso()) {
                            contadorAdicionados++;
                        } else {
                            contadorErros++;
                        }//else
                    }//if
                } catch (Exception e) {
                    contadorErros++;
                }//catch
            }//while
            
            return new BoolEMensagem(true, 
                String.format("✅ Carregados %d ativos de: %s (Erros: %d)", 
                    contadorAdicionados, caminho, contadorErros));
                    
        } catch (IOException e) {
            return new BoolEMensagem(false, 
                "❌ Erro ao ler ficheiro: " + e.getMessage());
        }//catch
    }//carregarCSV

    /**
     * Carrega ativos de um ficheiro TXT para a carteira.
     */
    public static BoolEMensagem carregarTXT(Carteira pCarteira, String pCaminhoFicheiro) {
        String caminho = garantirExtensao(pCaminhoFicheiro, EXTENSAO_TXT);
        File ficheiro = new File(caminho);
        
        if (!ficheiro.exists()) {
            return new BoolEMensagem(false, 
                "❌ Ficheiro não encontrado: " + caminho);
        }//if
        
        int contadorAdicionados = 0;
        int contadorErros = 0;
        boolean dentroDosDados = false;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ficheiro))) {
            String linha;
            
            while ((linha = reader.readLine()) != null) {
                // Procurar marcador de início de dados
                if (linha.equals("@DATA_START")) {
                    dentroDosDados = true;
                    continue;
                }//if
                
                // Procurar marcador de fim de dados
                if (linha.equals("@DATA_END")) {
                    break;
                }//if
                
                // Processar apenas linhas dentro da zona de dados
                if (dentroDosDados && !linha.trim().isEmpty()) {
                    try {
                        Ativo ativo = parsearLinhaTXT(linha);
                        if (ativo != null) {
                            BoolEMensagem resultado = pCarteira.adicionar(ativo);
                            if (resultado.sucesso()) {
                                contadorAdicionados++;
                            } else {
                                contadorErros++;
                            }//else
                        }//if
                    } catch (Exception e) {
                        contadorErros++;
                    }//catch
                }//if
            }//while
            
            if (!dentroDosDados) {
                return new BoolEMensagem(false, 
                    "❌ Ficheiro não contém dados válidos (falta @DATA_START)");
            }//if
            
            return new BoolEMensagem(true, 
                String.format("✅ Carregados %d ativos de: %s (Erros: %d)", 
                    contadorAdicionados, caminho, contadorErros));
                    
        } catch (IOException e) {
            return new BoolEMensagem(false, 
                "❌ Erro ao ler ficheiro: " + e.getMessage());
        }//catch
    }//carregarTXT

    //--------------------------------------------------
    // MÉTODOS AUXILIARES
    //--------------------------------------------------
    
    /**
     * Parseia uma linha CSV e cria o Ativo correspondente.
     * Formato: TICKER;TIPO;NOME;PRECO;VARIACAO;VOLUME
     */
    private static Ativo parsearLinhaCSV(String pLinha) {
        String[] partes = pLinha.split(SEPARADOR_CSV);
        if (partes.length < 6) return null;
        
        String ticker = partes[0].trim();
        TipoAtivo tipo = TipoAtivo.valueOf(partes[1].trim());
        String nome = partes[2].trim();
        double preco = Double.parseDouble(partes[3].trim().replace(",", "."));
        double variacao = Double.parseDouble(partes[4].trim().replace(",", "."));
        long volume = Long.parseLong(partes[5].trim());
        
        return FabricaAtivos.criarAtivo(tipo, ticker, nome, preco, variacao, volume);
    }//parsearLinhaCSV

    /**
     * Parseia uma linha TXT e cria o Ativo correspondente.
     * Formato: TICKER|TIPO|NOME|PRECO|VARIACAO|VOLUME
     */
    private static Ativo parsearLinhaTXT(String pLinha) {
        String[] partes = pLinha.split("\\|");
        if (partes.length < 6) return null;
        
        String ticker = partes[0].trim();
        TipoAtivo tipo = TipoAtivo.valueOf(partes[1].trim());
        String nome = partes[2].trim();
        double preco = Double.parseDouble(partes[3].trim().replace(",", "."));
        double variacao = Double.parseDouble(partes[4].trim().replace(",", "."));
        long volume = Long.parseLong(partes[5].trim());
        
        return FabricaAtivos.criarAtivo(tipo, ticker, nome, preco, variacao, volume);
    }//parsearLinhaTXT

    /**
     * Garante que o caminho tem a extensão correta.
     */
    private static String garantirExtensao(String pCaminho, String pExtensao) {
        if (!pCaminho.toLowerCase().endsWith(pExtensao)) {
            return pCaminho + pExtensao;
        }//if
        return pCaminho;
    }//garantirExtensao

    /**
     * Trunca uma string se for maior que o tamanho máximo.
     */
    private static String truncar(String pTexto, int pMax) {
        if (pTexto.length() <= pMax) return pTexto;
        return pTexto.substring(0, pMax - 3) + "...";
    }//truncar

    /**
     * Lista ficheiros de carteira disponíveis no diretório atual.
     */
    public static List<String> listarFicheirosCarteira(String pDiretorio) {
        List<String> ficheiros = new ArrayList<>();
        File dir = new File(pDiretorio);
        
        if (dir.exists() && dir.isDirectory()) {
            File[] lista = dir.listFiles((d, nome) -> 
                nome.endsWith(EXTENSAO_CSV) || nome.endsWith(EXTENSAO_TXT)
            );
            
            if (lista != null) {
                for (File f : lista) {
                    ficheiros.add(f.getName());
                }//for
            }//if
        }//if
        
        return ficheiros;
    }//listarFicheirosCarteira

}//classe GestorFicheiros
