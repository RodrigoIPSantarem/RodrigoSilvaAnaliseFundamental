// FabricaAtivos.java
package sistemacotacoes.fabrica;

import sistemacotacoes.modelo.*;
import sistemacotacoes.enums.TipoAtivo;

/**
 * Classe Factory para criação de objetos Ativo.
 * 
 * Demonstra: FACTORY PATTERN
 * - Centraliza a lógica de criação de objetos
 * - Decide qual subclasse instanciar baseado no tipo
 */
public class FabricaAtivos {

    //--------------------------------------------------
    // Factory Method Principal
    //--------------------------------------------------
    public static Ativo criarAtivo(
        TipoAtivo pTipo,
        String pTicker,
        String pNome,
        double pPreco,
        double pVariacao,
        long pVolume
    ) {
        switch (pTipo) {
            case ACAO:
                return new Acao(pTicker, pNome, pPreco, pVariacao, pVolume);
            case CRIPTO:
                return new Cripto(pTicker, pNome, pPreco, pVariacao, pVolume);
            case ETF:
                return new ETF(pTicker, pNome, pPreco, pVariacao, pVolume);
            default:
                throw new IllegalArgumentException(
                    "Tipo de ativo desconhecido: " + pTipo
                );
        }//switch
    }//criarAtivo

    //--------------------------------------------------
    // Detecção Automática de Tipo
    //--------------------------------------------------
    public static TipoAtivo detectarTipo(String pTicker) {
        String ticker = pTicker.toUpperCase();
        
        // Criptomoedas: contêm -USD, -EUR, etc.
        if (ticker.contains("-USD") || ticker.contains("-EUR") || 
            ticker.contains("-BTC") || ticker.contains("-ETH")) {
            return TipoAtivo.CRIPTO;
        }//if
        
        // ETFs conhecidos
        String[] etfsConhecidos = {
            "IVV", "SPY", "QQQ", "VOO", "VTI", "ARKK", 
            "DIA", "IWM", "EEM", "GLD", "SLV", "USO"
        };
        for (String etf : etfsConhecidos) {
            if (ticker.equals(etf)) {
                return TipoAtivo.ETF;
            }//if
        }//for
        
        // Por defeito: Ação
        return TipoAtivo.ACAO;
    }//detectarTipo

    //--------------------------------------------------
    // Factory com Detecção Automática
    //--------------------------------------------------
    public static Ativo criarAtivoAuto(
        String pTicker,
        String pNome,
        double pPreco,
        double pVariacao,
        long pVolume
    ) {
        TipoAtivo tipo = detectarTipo(pTicker);
        return criarAtivo(tipo, pTicker, pNome, pPreco, pVariacao, pVolume);
    }//criarAtivoAuto

}//classe FabricaAtivos
