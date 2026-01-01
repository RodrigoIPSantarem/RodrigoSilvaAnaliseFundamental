package analisefundamental.modelo;

import analisefundamental.enums.Setor;
import analisefundamental.util.BoolEMensagem;

public class AcaoTecnologia extends Acao {

    public AcaoTecnologia(String pTicker, String pNome, double pPreco, double pBeta, DadosFinanceiros pDados) {
        super(pTicker, pNome, pPreco, pBeta, Setor.TECNOLOGIA, pDados);
    }//construtor

    @Override
    public double obterMetricaAvaliacao() {
        // Polimorfismo: Tech usa FCF Ajustado (FCO - CapEx - SBC)
        return this.mDadosFinanceiros.obterFCFAjustado();
    }//obterMetricaAvaliacao

    @Override
    public double obterPremioRiscoSetor() {
        // Protocolo: US10Y + 6-8%
        return 0.07;
    }//obterPremioRiscoSetor

    @Override
    public BoolEMensagem verificarRiscosSetoriais() {
        // Tech aceita prejuízo contabilístico se tiver FCF positivo
        if (this.mDadosFinanceiros.obterLucrosPorAcaoTTM() < 0) {
            if (this.mDadosFinanceiros.obterFluxoCaixaLivre() > 0) {
                return new BoolEMensagem(true, "Aviso Tech: Prejuízo GAAP mas FCF positivo (Aceitável).");
            } else {
                return new BoolEMensagem(false, "REJEIÇÃO TECH: Queima de caixa (FCF negativo).");
            }
        }
        return new BoolEMensagem(true, "Tech: Riscos controlados.");
    }//verificarRiscosSetoriais
}//classe AcaoTecnologia