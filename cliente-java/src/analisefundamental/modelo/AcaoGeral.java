package analisefundamental.modelo;

import analisefundamental.enums.Setor;
import analisefundamental.util.BoolEMensagem;

public class AcaoGeral extends Acao {

    public AcaoGeral(String pTicker, String pNome, double pPreco, double pBeta, Setor pSetor, DadosFinanceiros pDados) {
        super(pTicker, pNome, pPreco, pBeta, pSetor, pDados);
    }//construtor

    @Override
    public double obterMetricaAvaliacao() {
        // Polimorfismo: Graham usa EPS (Lucros)
        return this.mDadosFinanceiros.obterLucrosPorAcaoTTM();
    }//obterMetricaAvaliacao

    @Override
    public double obterPremioRiscoSetor() {
        return 0.055; // US10Y + 5-6%
    }//obterPremioRiscoSetor

    @Override
    public BoolEMensagem verificarRiscosSetoriais() {
        // Setores gerais não devem ter dívida > 3x (mais estrito que o Kill Switch 4x)
        if (this.mDadosFinanceiros.obterDividaEbitda() > 3.0) {
            return new BoolEMensagem(false, "REJEIÇÃO GERAL: Dívida acima de 3x EBITDA.");
        }
        return new BoolEMensagem(true, "Geral: Estrutura de capital adequada.");
    }//verificarRiscosSetoriais
}//classe AcaoGeral