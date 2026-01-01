package analisefundamental.modelo;

import analisefundamental.enums.Setor;
import analisefundamental.util.BoolEMensagem;

public class AcaoBanco extends Acao {

    public AcaoBanco(String pTicker, String pNome, double pPreco, double pBeta, DadosFinanceiros pDados) {
        super(pTicker, pNome, pPreco, pBeta, Setor.FINANCEIRO, pDados);
    }//construtor

    @Override
    public double obterMetricaAvaliacao() {
        // Polimorfismo: Banca usa Tangible Book Value (TBV)
        return this.mDadosFinanceiros.obterValorContabilisticoTangivelPorAcao();
    }//obterMetricaAvaliacao

    @Override
    public double obterPremioRiscoSetor() {
        return 0.05; // US10Y + 5%
    }//obterPremioRiscoSetor

    @Override
    public BoolEMensagem verificarRiscosSetoriais() {
        double tbv = obterMetricaAvaliacao();
        if (tbv <= 0) {
            return new BoolEMensagem(false, "REJEIÇÃO BANCA: TBV Negativo (Insolvência técnica?).");
        }

        double ptbv = this.mPrecoAtual / tbv;
        if (ptbv > 1.5 && this.mDadosFinanceiros.obterRoe() < 0.15) {
            return new BoolEMensagem(false, "REJEIÇÃO BANCA: P/TBV > 1.5 sem ROE > 15%.");
        }

        return new BoolEMensagem(true, "Banca: Métricas de balanço sólidas.");
    }//verificarRiscosSetoriais
}//classe AcaoBanco