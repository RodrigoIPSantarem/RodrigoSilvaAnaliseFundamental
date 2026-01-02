// ETF.java
package sistemacotacoes.modelo;

import sistemacotacoes.enums.TipoAtivo;

/**
 * Representa um Exchange Traded Fund (Fundo de Índice).
 * Exemplos: IVV (S&P 500), QQQ (Nasdaq 100)
 * 
 * Demonstra: HERANÇA + POLIMORFISMO
 * O cálculo de risco é 0.5× menor devido à diversificação intrínseca.
 */
public class ETF extends Ativo {

    // Multiplicador de risco para ETFs (menor por ser diversificado)
    private static final double MULTIPLICADOR_RISCO = 0.5;

    //--------------------------------------------------
    // Construtor
    //--------------------------------------------------
    public ETF(
        String pTicker, 
        String pNome, 
        double pPreco, 
        double pVariacao, 
        long pVolume
    ) {
        super(pTicker, pNome, pPreco, pVariacao, pVolume);
    }//construtor ETF

    //--------------------------------------------------
    // Implementação Polimórfica
    //--------------------------------------------------
    
    /**
     * POLIMORFISMO: Risco de ETF = variação × 0.5
     * ETFs são cestos diversificados, logo têm menos risco.
     */
    @Override
    public double calcularRisco() {
        return Math.abs(this.mVariacao) * MULTIPLICADOR_RISCO;
    }//calcularRisco

    /**
     * POLIMORFISMO: Recomendação específica para ETFs.
     */
    @Override
    public String obterRecomendacao() {
        if (this.mVariacao < -3.0) {
            return "💰 REFORÇAR - Oportunidade de DCA";
        }//if
        if (this.mVariacao > 3.0) {
            return "📈 CONTINUAR - Tendência positiva";
        }//if
        return "🏦 MANTER - Estratégia longo prazo";
    }//obterRecomendacao

    /**
     * Retorna o tipo do ativo.
     */
    @Override
    public TipoAtivo obterTipo() {
        return TipoAtivo.ETF;
    }//obterTipo

}//classe ETF
