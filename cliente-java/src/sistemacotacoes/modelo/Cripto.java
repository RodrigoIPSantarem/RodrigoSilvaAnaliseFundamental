// Cripto.java
package sistemacotacoes.modelo;

import sistemacotacoes.enums.TipoAtivo;

/**
 * Representa uma Criptomoeda.
 * Exemplos: BTC-USD (Bitcoin), ETH-USD (Ethereum)
 * 
 * Demonstra: HERANÇA + POLIMORFISMO
 * O cálculo de risco é 3× maior devido à alta volatilidade.
 */
public class Cripto extends Ativo {

    // Multiplicador de risco para criptomoedas
    private static final double MULTIPLICADOR_RISCO = 3.0;

    //--------------------------------------------------
    // Construtor
    //--------------------------------------------------
    public Cripto(
        String pTicker, 
        String pNome, 
        double pPreco, 
        double pVariacao, 
        long pVolume
    ) {
        super(pTicker, pNome, pPreco, pVariacao, pVolume);
    }//construtor Cripto

    //--------------------------------------------------
    // Implementação Polimórfica
    //--------------------------------------------------
    
    /**
     * POLIMORFISMO: Risco de cripto = variação × 3.0
     * Criptomoedas são muito mais voláteis que ações tradicionais.
     */
    @Override
    public double calcularRisco() {
        return Math.abs(this.mVariacao) * MULTIPLICADOR_RISCO;
    }//calcularRisco

    /**
     * POLIMORFISMO: Recomendação específica para criptomoedas.
     */
    @Override
    public String obterRecomendacao() {
        if (this.mVariacao < -10.0) {
            return "🔥 CRASH - Risco extremo, possível oportunidade";
        }//if
        if (this.mVariacao > 10.0) {
            return "🚀 PUMP - Cuidado com correções";
        }//if
        return "⚡ ESPECULAÇÃO - Alta volatilidade";
    }//obterRecomendacao

    /**
     * Retorna o tipo do ativo.
     */
    @Override
    public TipoAtivo obterTipo() {
        return TipoAtivo.CRIPTO;
    }//obterTipo

}//classe Cripto
