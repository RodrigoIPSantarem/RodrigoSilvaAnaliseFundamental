package analisefundamental.modelo;

import analisefundamental.enums.Setor;
import analisefundamental.util.BoolEMensagem;
import java.util.List;
import java.util.ArrayList;

public class AcaoBanco extends Acao {

    public AcaoBanco(String pTicker, String pNome, double pPreco, double pBeta, DadosFinanceiros pDados) {
        super(pTicker, pNome, pPreco, pBeta, Setor.FINANCEIRO, pDados);
    }

    @Override
    public double obterMetricaAvaliacao() {
        return this.mDadosFinanceiros.calcularTBVporAcao();
    }

    @Override
    public double obterPremioRiscoSetor() {
        return 0.065;
    }

    @Override
    public List<String> verificarKillSwitchesUniversais() {
        // Bancos têm uma estrutura de capital diferente.
        // NÃO DEVEMOS usar o Kill Switch padrão de Dívida/EBITDA.
        List<String> erros = new ArrayList<>();

        // Apenas verificamos margens e prejuízos, ignorando a dívida
        if (this.mDadosFinanceiros.temMargensEmQueda3Anos()) {
            erros.add("KILL SWITCH 3: Margens líquidas em queda há 3 anos consecutivos");
        }

        // Bancos não podem ter prejuízo
        if (this.mDadosFinanceiros.contarAnosPrejuizoUltimos5() > 0) {
            erros.add("KILL SWITCH 4: Histórico de prejuízos nos últimos 5 anos");
        }

        return erros;
    }

    @Override
    public BoolEMensagem verificarRiscosSetoriais() {
        if (this.mDadosFinanceiros.calcularRacioGoodwillAtivos() > 0.10) {
            return new BoolEMensagem(false, "REJEIÇÃO BANCO: Goodwill excessivo (>10% dos Ativos).");
        }
        if (this.mDadosFinanceiros.obterRoe() < 0.08) {
            return new BoolEMensagem(false, "REJEIÇÃO BANCO: ROE muito baixo (<8%).");
        }
        return new BoolEMensagem(true, "Banco: Métricas de solidez aceitáveis.");
    }
}