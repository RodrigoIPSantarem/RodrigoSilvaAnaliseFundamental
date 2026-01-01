package analisefundamental.modelo;

import analisefundamental.enums.Setor;
import analisefundamental.util.BoolEMensagem;

public class AcaoBanco extends Acao {

    public AcaoBanco(String pTicker, String pNome, double pPreco, double pBeta, DadosFinanceiros pDados) {
        super(pTicker, pNome, pPreco, pBeta, Setor.FINANCEIRO, pDados);
    }//construtor

    @Override
    public double obterMetricaAvaliacao() {
        // CORREÇÃO: O método agora chama-se 'calcularTBVporAcao'
        // TBV = Valor Contabilístico Tangível
        return this.mDadosFinanceiros.calcularTBVporAcao();
    }//obterMetricaAvaliacao

    @Override
    public double obterPremioRiscoSetor() {
        // Financeiro tem risco sistémico moderado/alto (6.5%)
        return 0.065;
    }//obterPremioRiscoSetor

    @Override
    public BoolEMensagem verificarRiscosSetoriais() {
        // 1. Verificar Goodwill (Bancos sólidos têm ativos reais, não "ar")
        if (this.mDadosFinanceiros.calcularRacioGoodwillAtivos() > 0.10) {
            return new BoolEMensagem(false, "REJEIÇÃO BANCO: Goodwill excessivo (>10% dos Ativos).");
        }

        // 2. Verificar ROE (Bancos precisam de ROE > 8-10% para criar valor)
        if (this.mDadosFinanceiros.obterRoe() < 0.08) {
            return new BoolEMensagem(false, "REJEIÇÃO BANCO: ROE muito baixo (<8%).");
        }

        return new BoolEMensagem(true, "Banco: Métricas de solidez aceitáveis.");
    }//verificarRiscosSetoriais

}//classe AcaoBanco