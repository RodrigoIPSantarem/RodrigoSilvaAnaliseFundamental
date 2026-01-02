// Carteira.java
package sistemacotacoes.gestao;

import sistemacotacoes.modelo.Ativo;
import sistemacotacoes.enums.CriterioOrdenacao;
import sistemacotacoes.enums.TipoAtivo;
import sistemacotacoes.util.BoolEMensagem;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe que gere uma coleção de ativos financeiros.
 * 
 * Demonstra: 
 * - COMPOSIÇÃO (contém List<Ativo>)
 * - USO DE COMPARATORS
 * - RETORNOS COMPOSTOS (BoolEMensagem)
 */
public class Carteira {
    
    private List<Ativo> mAtivos;
    private int mCapacidadeMaxima;
    private String mNome;

    //--------------------------------------------------
    // Construtores
    //--------------------------------------------------
    public Carteira() {
        this.mAtivos = new ArrayList<>();
        this.mCapacidadeMaxima = 50;  // Limite por defeito
        this.mNome = "Minha Carteira";
    }//construtor Carteira

    public Carteira(String pNome, int pCapacidade) {
        this.mAtivos = new ArrayList<>();
        this.mCapacidadeMaxima = pCapacidade;
        this.mNome = pNome;
    }//construtor Carteira

    //--------------------------------------------------
    // Adicionar/Remover Ativos (com BoolEMensagem)
    //--------------------------------------------------
    
    public BoolEMensagem adicionar(Ativo pAtivo) {
        if (pAtivo == null) {
            return new BoolEMensagem(false, "Ativo inválido (null)");
        }//if
        
        if (mAtivos.size() >= mCapacidadeMaxima) {
            return new BoolEMensagem(false, 
                String.format("Carteira cheia (%d/%d)", mAtivos.size(), mCapacidadeMaxima));
        }//if
        
        if (mAtivos.contains(pAtivo)) {
            return new BoolEMensagem(false, 
                String.format("%s já existe na carteira", pAtivo.getTicker()));
        }//if
        
        mAtivos.add(pAtivo);
        return new BoolEMensagem(true, 
            String.format("✅ %s adicionado com sucesso", pAtivo.getTicker()));
    }//adicionar

    public BoolEMensagem remover(Ativo pAtivo) {
        if (pAtivo == null) {
            return new BoolEMensagem(false, "Ativo inválido (null)");
        }//if
        
        if (!mAtivos.contains(pAtivo)) {
            return new BoolEMensagem(false, 
                String.format("%s não encontrado na carteira", pAtivo.getTicker()));
        }//if
        
        mAtivos.remove(pAtivo);
        return new BoolEMensagem(true, 
            String.format("🗑️ %s removido com sucesso", pAtivo.getTicker()));
    }//remover

    public BoolEMensagem removerPorTicker(String pTicker) {
        Optional<Ativo> encontrado = mAtivos.stream()
            .filter(a -> a.getTicker().equalsIgnoreCase(pTicker))
            .findFirst();
            
        if (encontrado.isPresent()) {
            return remover(encontrado.get());
        }//if
        return new BoolEMensagem(false, pTicker + " não encontrado");
    }//removerPorTicker

    //--------------------------------------------------
    // Ordenações (usando Enum CriterioOrdenacao)
    //--------------------------------------------------
    
    public void ordenar(CriterioOrdenacao pCriterio) {
        mAtivos.sort(pCriterio.getComparador());
    }//ordenar

    public void ordenarPorPreco() {
        ordenar(CriterioOrdenacao.POR_PRECO_DESC);
    }//ordenarPorPreco

    public void ordenarPorVariacao() {
        ordenar(CriterioOrdenacao.POR_VARIACAO);
    }//ordenarPorVariacao

    public void ordenarPorRisco() {
        ordenar(CriterioOrdenacao.POR_RISCO_DESC);
    }//ordenarPorRisco

    public void ordenarPorTicker() {
        ordenar(CriterioOrdenacao.POR_TICKER);
    }//ordenarPorTicker

    //--------------------------------------------------
    // Filtros
    //--------------------------------------------------
    
    public List<Ativo> filtrarPorTipo(TipoAtivo pTipo) {
        return mAtivos.stream()
            .filter(a -> a.obterTipo() == pTipo)
            .collect(Collectors.toList());
    }//filtrarPorTipo

    public List<Ativo> filtrarEmAlta() {
        return mAtivos.stream()
            .filter(Ativo::estaEmAlta)
            .collect(Collectors.toList());
    }//filtrarEmAlta

    public List<Ativo> filtrarEmQueda() {
        return mAtivos.stream()
            .filter(Ativo::estaEmQueda)
            .collect(Collectors.toList());
    }//filtrarEmQueda

    //--------------------------------------------------
    // Estatísticas
    //--------------------------------------------------
    
    public double calcularValorTotal() {
        return mAtivos.stream()
            .mapToDouble(Ativo::getPreco)
            .sum();
    }//calcularValorTotal

    public double calcularRiscoMedio() {
        if (mAtivos.isEmpty()) return 0.0;
        return mAtivos.stream()
            .mapToDouble(Ativo::calcularRisco)
            .average()
            .orElse(0.0);
    }//calcularRiscoMedio

    public double calcularVariacaoMedia() {
        if (mAtivos.isEmpty()) return 0.0;
        return mAtivos.stream()
            .mapToDouble(Ativo::getVariacao)
            .average()
            .orElse(0.0);
    }//calcularVariacaoMedia

    public Ativo obterMaisArriscado() {
        return mAtivos.stream()
            .max(Comparator.comparingDouble(Ativo::calcularRisco))
            .orElse(null);
    }//obterMaisArriscado

    public Ativo obterMenosArriscado() {
        return mAtivos.stream()
            .min(Comparator.comparingDouble(Ativo::calcularRisco))
            .orElse(null);
    }//obterMenosArriscado

    //--------------------------------------------------
    // Listar e Exibir
    //--------------------------------------------------
    
    public void listar() {
        System.out.println("\n" + "═".repeat(80));
        System.out.printf("  📊 %s (%d/%d ativos)\n", mNome, mAtivos.size(), mCapacidadeMaxima);
        System.out.println("═".repeat(80));
        
        if (mAtivos.isEmpty()) {
            System.out.println("  (Carteira vazia)");
        } else {
            System.out.printf("%-12s | %-6s | %-12s | %-8s | %-8s | %s\n",
                "TICKER", "TIPO", "PREÇO", "VAR%", "RISCO", "RECOMENDAÇÃO");
            System.out.println("─".repeat(80));
            
            for (Ativo a : mAtivos) {
                System.out.printf("%-12s | %-6s | $%-11.2f | %+7.2f%% | %-8.2f | %s\n",
                    a.getTicker(),
                    a.obterTipo().getNome(),
                    a.getPreco(),
                    a.getVariacao(),
                    a.calcularRisco(),
                    a.obterRecomendacao()
                );
            }//for
        }//else
        
        System.out.println("═".repeat(80));
    }//listar

    public void listarResumo() {
        System.out.println("\n📈 RESUMO DA CARTEIRA:");
        System.out.printf("  • Total de ativos: %d\n", mAtivos.size());
        System.out.printf("  • Variação média: %+.2f%%\n", calcularVariacaoMedia());
        System.out.printf("  • Risco médio: %.2f\n", calcularRiscoMedio());
        
        Ativo maisArriscado = obterMaisArriscado();
        if (maisArriscado != null) {
            System.out.printf("  • Mais arriscado: %s (Risco: %.2f)\n", 
                maisArriscado.getTicker(), maisArriscado.calcularRisco());
        }//if
    }//listarResumo

    //--------------------------------------------------
    // Getters
    //--------------------------------------------------
    public List<Ativo> getAtivos() { return new ArrayList<>(mAtivos); }
    public int getQuantidade() { return mAtivos.size(); }
    public int getCapacidadeMaxima() { return mCapacidadeMaxima; }
    public String getNome() { return mNome; }
    public boolean estaVazia() { return mAtivos.isEmpty(); }
    public boolean estaCheia() { return mAtivos.size() >= mCapacidadeMaxima; }

    //--------------------------------------------------
    // toString
    //--------------------------------------------------
    @Override
    public String toString() {
        return String.format("Carteira[%s, %d/%d ativos]", 
            mNome, mAtivos.size(), mCapacidadeMaxima);
    }//toString

}//classe Carteira
