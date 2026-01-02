// Acao.java
package sistemacotacoes.modelo;

import sistemacotacoes.enums.TipoAtivo;

/**
 * Representa uma Ação (Stock) de uma empresa cotada em bolsa.
 * Exemplos: AAPL (Apple), MSFT (Microsoft), PETR4 (Petrobras)
 * 
 * Demonstra: HERANÇA + POLIMORFISMO
 */
public class Acao extends Ativo {

    //--------------------------------------------------
    // Construtor
    //--------------------------------------------------
    public Acao(
        String pTicker, 
        String pNome, 
        double pPreco, 
        double pVariacao, 
        long pVolume
    ) {
        super(pTicker, pNome, pPreco, pVariacao, pVolume);
    }//construtor Acao

    //--------------------------------------------------
    // Implementação Polimórfica
    //--------------------------------------------------
    
    /**
     * POLIMORFISMO: Risco de ação = valor absoluto da variação.
     * Multiplicador: 1.0× (risco base)
     */
    @Override
    public double calcularRisco() {
        return Math.abs(this.mVariacao) * 1.0;
    }//calcularRisco

    /**
     * POLIMORFISMO: Recomendação específica para ações.
     */
    @Override
    public String obterRecomendacao() {
        if (this.mVariacao < -5.0) {
            return "📉 OPORTUNIDADE - Possível desconto";
        }//if
        if (this.mVariacao > 5.0) {
            return "⚠️ CUIDADO - Pode estar esticada";
        }//if
        return "➡️ MANTER - Sem sinal claro";
    }//obterRecomendacao

    /**
     * Retorna o tipo do ativo.
     */
    @Override
    public TipoAtivo obterTipo() {
        return TipoAtivo.ACAO;
    }//obterTipo

}//classe Acao
