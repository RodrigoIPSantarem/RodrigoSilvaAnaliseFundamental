// CriterioOrdenacao.java
package sistemacotacoes.enums;

import sistemacotacoes.modelo.Ativo;
import java.util.Comparator;

/**
 * Enum com Comparators embutidos para ordenação de ativos.
 * 
 * Baseado em CriteriaWithComparators.java das aulas.
 * Demonstra: ENUM + COMPARATORS + INNER CLASSES
 */
public enum CriterioOrdenacao {
    POR_TICKER(new ComparadorTicker()),
    POR_PRECO(new ComparadorPreco()),
    POR_PRECO_DESC(new ComparadorPreco().reversed()),
    POR_VARIACAO(new ComparadorVariacao()),
    POR_VARIACAO_DESC(new ComparadorVariacao().reversed()),
    POR_VOLUME(new ComparadorVolume()),
    POR_RISCO(new ComparadorRisco()),
    POR_RISCO_DESC(new ComparadorRisco().reversed());

    private final Comparator<Ativo> mComparador;

    //--------------------------------------------------
    // Construtor do Enum
    //--------------------------------------------------
    CriterioOrdenacao(Comparator<Ativo> pComparador) {
        this.mComparador = pComparador;
    }//construtor

    //--------------------------------------------------
    // Getter do Comparator
    //--------------------------------------------------
    public Comparator<Ativo> getComparador() {
        return this.mComparador;
    }//getComparador

    //--------------------------------------------------
    // Inner Classes - Comparadores
    //--------------------------------------------------
    
    static class ComparadorTicker implements Comparator<Ativo> {
        @Override
        public int compare(Ativo pO1, Ativo pO2) {
            return pO1.getTicker().compareToIgnoreCase(pO2.getTicker());
        }//compare
    }//ComparadorTicker

    static class ComparadorPreco implements Comparator<Ativo> {
        @Override
        public int compare(Ativo pO1, Ativo pO2) {
            return Double.compare(pO1.getPreco(), pO2.getPreco());
        }//compare
    }//ComparadorPreco

    static class ComparadorVariacao implements Comparator<Ativo> {
        @Override
        public int compare(Ativo pO1, Ativo pO2) {
            return Double.compare(pO1.getVariacao(), pO2.getVariacao());
        }//compare
    }//ComparadorVariacao

    static class ComparadorVolume implements Comparator<Ativo> {
        @Override
        public int compare(Ativo pO1, Ativo pO2) {
            return Long.compare(pO1.getVolume(), pO2.getVolume());
        }//compare
    }//ComparadorVolume

    static class ComparadorRisco implements Comparator<Ativo> {
        @Override
        public int compare(Ativo pO1, Ativo pO2) {
            return Double.compare(pO1.calcularRisco(), pO2.calcularRisco());
        }//compare
    }//ComparadorRisco

}//enum CriterioOrdenacao
