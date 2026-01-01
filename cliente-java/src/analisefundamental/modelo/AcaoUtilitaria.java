package analisefundamental.modelo;

import analisefundamental.enums.Setor;
import analisefundamental.util.BoolEMensagem;

public class AcaoUtilitaria extends Acao {

    public AcaoUtilitaria(String pTicker, String pNome, double pPreco, double pBeta, DadosFinanceiros pDados) {
        super(pTicker, pNome, pPreco, pBeta, Setor.UTILITARIOS, pDados);
    }//construtor

    @Override
    public double obterMetricaAvaliacao() {
        // Polimorfismo: Foca no Dividendo
        return this.mDadosFinanceiros.obterDividendoPorAcao();
    }//obterMetricaAvaliacao

    @Override
    public double obterPremioRiscoSetor() {
        return 0.04; // US10Y + 4% (Defensivos)
    }//obterPremioRiscoSetor

    @Override
    public BoolEMensagem verificarRiscosSetoriais() {
        // Utilities exigem Yield decente
        if (this.mDadosFinanceiros.obterRendimentoDividendo() < 0.02) {
            return new BoolEMensagem(false, "REJEIÇÃO UTILITIES: Yield demasiado baixo (<2%).");
        }
        return new BoolEMensagem(true, "Utility: Estável.");
    }//verificarRiscosSetoriais
}//classe AcaoUtilitaria