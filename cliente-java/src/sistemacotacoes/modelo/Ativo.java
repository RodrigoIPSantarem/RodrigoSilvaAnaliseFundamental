// Ativo.java
package sistemacotacoes.modelo;

import sistemacotacoes.enums.TipoAtivo;

/**
 * Classe abstrata que representa um ativo financeiro genérico.
 * 
 * Demonstra:
 * - CLASSE ABSTRATA (não pode ser instanciada diretamente)
 * - HERANÇA (Acao, Cripto, ETF herdam desta)
 * - INTERFACE (implementa Analisavel e Comparable)
 * - ENCAPSULAMENTO (atributos protected com prefixo m)
 */
public abstract class Ativo implements Analisavel, Comparable<Ativo> {
    
    // Atributos protegidos (acessíveis nas subclasses)
    // Prefixo "m" = member (membro de instância)
    protected String mTicker;
    protected String mNome;
    protected double mPreco;
    protected double mVariacao;  // Variação percentual
    protected long mVolume;

    //--------------------------------------------------
    // Construtor
    // Prefixo "p" = parameter (parâmetro)
    //--------------------------------------------------
    public Ativo(
        String pTicker, 
        String pNome, 
        double pPreco, 
        double pVariacao, 
        long pVolume
    ) {
        this.mTicker = pTicker;
        this.mNome = pNome;
        this.mPreco = pPreco;
        this.mVariacao = pVariacao;
        this.mVolume = pVolume;
    }//construtor Ativo

    //--------------------------------------------------
    // Métodos Concretos (iguais para todas as subclasses)
    //--------------------------------------------------
    
    /**
     * Retorna o estado da tendência baseado na variação.
     */
    public String obterTendencia() {
        if (mVariacao > 0) return "ALTA 🟢";
        if (mVariacao < 0) return "BAIXA 🔴";
        return "NEUTRO ⚪";
    }//obterTendencia

    /**
     * Verifica se o ativo está em queda significativa (>3%).
     */
    public boolean estaEmQueda() {
        return this.mVariacao < -3.0;
    }//estaEmQueda

    /**
     * Verifica se o ativo está em alta significativa (>3%).
     */
    public boolean estaEmAlta() {
        return this.mVariacao > 3.0;
    }//estaEmAlta

    //--------------------------------------------------
    // Métodos Abstratos (obrigatórios nas subclasses)
    //--------------------------------------------------
    
    @Override
    public abstract double calcularRisco();

    @Override
    public abstract String obterRecomendacao();

    /**
     * Retorna o tipo do ativo como Enum.
     */
    public abstract TipoAtivo obterTipo();

    //--------------------------------------------------
    // Comparable: Ordenação natural por Ticker
    //--------------------------------------------------
    @Override
    public int compareTo(Ativo pOutro) {
        return this.mTicker.compareToIgnoreCase(pOutro.mTicker);
    }//compareTo

    //--------------------------------------------------
    // equals e hashCode (baseados no ticker)
    //--------------------------------------------------
    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) return true;
        if (!(pObj instanceof Ativo)) return false;
        Ativo outro = (Ativo) pObj;
        return this.mTicker.equalsIgnoreCase(outro.mTicker);
    }//equals

    @Override
    public int hashCode() {
        return mTicker.toUpperCase().hashCode();
    }//hashCode

    //--------------------------------------------------
    // toString
    //--------------------------------------------------
    @Override
    public String toString() {
        return String.format(
            "%-10s | %-6s | $%-10.2f | %+7.2f%% | %s",
            mTicker,
            obterTipo().getNome(),
            mPreco,
            mVariacao,
            obterTendencia()
        );
    }//toString

    //--------------------------------------------------
    // Getters (para Comparators e acesso externo)
    //--------------------------------------------------
    public String getTicker() { return mTicker; }
    public String getNome() { return mNome; }
    public double getPreco() { return mPreco; }
    public double getVariacao() { return mVariacao; }
    public long getVolume() { return mVolume; }

}//classe Ativo
