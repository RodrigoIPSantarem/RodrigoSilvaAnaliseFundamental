package analisefundamental.modelo;

import java.util.*;

public class DadosFinanceiros {

    // Campos ESSENCIAIS (13 parâmetros no construtor principal)
    private double mLucrosPorAcaoTTM;
    private double mCrescimentoLucros5A;
    private double mDividaEbitda;
    private double mRoe;
    private double mRoic;
    private double mMargemLiquidaAtual;
    private List<Double> mHistoricoMargemLiquida;
    private long mAcoesCirculacaoAtual;
    private List<Long> mHistoricoAcoesCirculacao;
    private List<Double> mHistoricoLucros;
    private double mFluxoCaixaOperacional;
    private double mCapex;
    private double mCompensacaoBaseadaAcoes;
    private double mFluxoCaixaLivre;

    // Campos OPCIONAIS (podem ser configurados depois)
    private double mValorContabilisticoPorAcao;
    private double mIntangiveis;
    private double mGoodwill;
    private double mAtivosTotal;
    private double mDividendoPorAcao;
    private double mRendimentoDividendo;
    private double mRacioDistribuicao;

    // ========== CONSTRUTOR PRINCIPAL (13 parâmetros) ==========
    public DadosFinanceiros(
            double pLucrosPorAcaoTTM,
            double pCrescimentoLucros5A,
            double pDividaEbitda,
            double pRoe,
            double pRoic,
            double pMargemLiquidaAtual,
            List<Double> pHistoricoMargemLiquida,
            long pAcoesCirculacaoAtual,
            List<Long> pHistoricoAcoesCirculacao,
            List<Double> pHistoricoLucros,
            double pFluxoCaixaOperacional,
            double pCapex,
            double pCompensacaoBaseadaAcoes) {

        // Validar e atribuir
        this.mLucrosPorAcaoTTM = pLucrosPorAcaoTTM;
        this.mCrescimentoLucros5A = pCrescimentoLucros5A;
        this.mDividaEbitda = Math.max(pDividaEbitda, 0.0);
        this.mRoe = pRoe;
        this.mRoic = pRoic;
        this.mMargemLiquidaAtual = pMargemLiquidaAtual;

        // Históricos (garantir não nulos)
        this.mHistoricoMargemLiquida = (pHistoricoMargemLiquida != null)
                ? new ArrayList<>(pHistoricoMargemLiquida)
                : Arrays.asList(pMargemLiquidaAtual * 0.95, pMargemLiquidaAtual * 0.98, pMargemLiquidaAtual);

        this.mAcoesCirculacaoAtual = Math.max(pAcoesCirculacaoAtual, 1L);
        this.mHistoricoAcoesCirculacao = (pHistoricoAcoesCirculacao != null)
                ? new ArrayList<>(pHistoricoAcoesCirculacao)
                : Arrays.asList(pAcoesCirculacaoAtual,
                (long)(pAcoesCirculacaoAtual * 1.01),
                (long)(pAcoesCirculacaoAtual * 1.02));

        this.mHistoricoLucros = (pHistoricoLucros != null)
                ? new ArrayList<>(pHistoricoLucros)
                : Arrays.asList(pLucrosPorAcaoTTM * 0.9,
                pLucrosPorAcaoTTM * 0.95,
                pLucrosPorAcaoTTM,
                pLucrosPorAcaoTTM * 1.05,
                pLucrosPorAcaoTTM * 1.1);

        // Fluxos de caixa
        this.mFluxoCaixaOperacional = pFluxoCaixaOperacional;
        this.mCapex = pCapex;
        this.mCompensacaoBaseadaAcoes = Math.max(pCompensacaoBaseadaAcoes, 0.0);
        this.mFluxoCaixaLivre = pFluxoCaixaOperacional + pCapex; // FCF = FCO + Capex

        // Valores padrão para opcionais
        this.mValorContabilisticoPorAcao = 0.0;
        this.mIntangiveis = 0.0;
        this.mGoodwill = 0.0;
        this.mAtivosTotal = 0.0;
        this.mDividendoPorAcao = 0.0;
        this.mRendimentoDividendo = 0.0;
        this.mRacioDistribuicao = 0.0;
    }

    // ========== CONSTRUTOR COMPLETO (21 parâmetros) ==========
    public DadosFinanceiros(
            double pLucrosPorAcaoTTM,
            double pCrescimentoLucros5A,
            double pDividaEbitda,
            double pRoe,
            double pRoic,
            double pMargemLiquidaAtual,
            List<Double> pHistoricoMargemLiquida,
            long pAcoesCirculacaoAtual,
            List<Long> pHistoricoAcoesCirculacao,
            List<Double> pHistoricoLucros,
            double pFluxoCaixaOperacional,
            double pCapex,
            double pCompensacaoBaseadaAcoes,
            double pValorContabilisticoPorAcao,
            double pIntangiveis,
            double pGoodwill,
            double pAtivosTotal,
            double pDividendoPorAcao,
            double pRendimentoDividendo,
            double pRacioDistribuicao) {

        // Chamar construtor principal
        this(pLucrosPorAcaoTTM, pCrescimentoLucros5A, pDividaEbitda, pRoe, pRoic,
                pMargemLiquidaAtual, pHistoricoMargemLiquida, pAcoesCirculacaoAtual,
                pHistoricoAcoesCirculacao, pHistoricoLucros, pFluxoCaixaOperacional,
                pCapex, pCompensacaoBaseadaAcoes);

        // Configurar campos opcionais
        this.mValorContabilisticoPorAcao = Math.max(pValorContabilisticoPorAcao, 0.0);
        this.mIntangiveis = Math.max(pIntangiveis, 0.0);
        this.mGoodwill = Math.max(pGoodwill, 0.0);
        this.mAtivosTotal = Math.max(pAtivosTotal, 0.0);
        this.mDividendoPorAcao = Math.max(pDividendoPorAcao, 0.0);
        this.mRendimentoDividendo = Math.max(pRendimentoDividendo, 0.0);
        this.mRacioDistribuicao = Math.max(pRacioDistribuicao, 0.0);
    }

    // ========== GETTERS ==========
    public double obterLucrosPorAcaoTTM() { return mLucrosPorAcaoTTM; }
    public double obterCrescimentoLucros5A() { return mCrescimentoLucros5A; }
    public double obterDividaEbitda() { return mDividaEbitda; }
    public double obterRoe() { return mRoe; }
    public double obterRoic() { return mRoic; }
    public double obterMargemLiquidaAtual() { return mMargemLiquidaAtual; }
    public List<Double> obterHistoricoMargemLiquida() { return new ArrayList<>(mHistoricoMargemLiquida); }
    public long obterAcoesCirculacaoAtual() { return mAcoesCirculacaoAtual; }
    public List<Long> obterHistoricoAcoesCirculacao() { return new ArrayList<>(mHistoricoAcoesCirculacao); }
    public List<Double> obterHistoricoLucros() { return new ArrayList<>(mHistoricoLucros); }
    public double obterFluxoCaixaOperacional() { return mFluxoCaixaOperacional; }
    public double obterCapex() { return mCapex; }
    public double obterCompensacaoBaseadaAcoes() { return mCompensacaoBaseadaAcoes; }
    public double obterFluxoCaixaLivre() { return mFluxoCaixaLivre; }
    public double obterValorContabilisticoPorAcao() { return mValorContabilisticoPorAcao; }
    public double obterIntangiveis() { return mIntangiveis; }
    public double obterGoodwill() { return mGoodwill; }
    public double obterAtivosTotal() { return mAtivosTotal; }
    public double obterDividendoPorAcao() { return mDividendoPorAcao; }
    public double obterRendimentoDividendo() { return mRendimentoDividendo; }
    public double obterRacioDistribuicao() { return mRacioDistribuicao; }

    // ========== MÉTODOS DE CÁLCULO ==========
    public double calcularDiluicaoAcoes3Anos() {
        if (mHistoricoAcoesCirculacao.size() < 2) return 0.0;
        long atual = mHistoricoAcoesCirculacao.get(0);
        long antigo = mHistoricoAcoesCirculacao.get(Math.min(2, mHistoricoAcoesCirculacao.size() - 1));
        if (antigo == 0) return 0.0;
        return (double)(atual - antigo) / antigo;
    }

    public boolean temMargensEmQueda3Anos() {
        if (mHistoricoMargemLiquida.size() < 3) return false;
        double m1 = mHistoricoMargemLiquida.get(0);
        double m2 = mHistoricoMargemLiquida.get(1);
        double m3 = mHistoricoMargemLiquida.get(2);
        return (m3 > m2) && (m2 > m1);
    }

    public int contarAnosPrejuizoUltimos5() {
        int count = 0;
        for (int i = 0; i < Math.min(5, mHistoricoLucros.size()); i++) {
            if (mHistoricoLucros.get(i) < 0) count++;
        }
        return count;
    }

    public boolean temFCONegativo2Anos() {
        return mFluxoCaixaOperacional < 0;
    }

    public double calcularRacioGoodwillAtivos() {
        if (mAtivosTotal == 0) return 0.0;
        return mGoodwill / mAtivosTotal;
    }

    public double calcularFCFAjustado() {
        return mFluxoCaixaLivre - mCompensacaoBaseadaAcoes;
    }

    public double calcularTBVporAcao() {
        if (mAcoesCirculacaoAtual == 0) return 0.0;
        double tangibleEquity = (mValorContabilisticoPorAcao * mAcoesCirculacaoAtual)
                - mIntangiveis - mGoodwill;
        return tangibleEquity / mAcoesCirculacaoAtual;
    }

    @Override
    public String toString() {
        return String.format("EPS: $%.2f | Cresc: %.1f%% | D/E: %.1fx | ROE: %.1f%%",
                mLucrosPorAcaoTTM, mCrescimentoLucros5A * 100,
                mDividaEbitda, mRoe * 100);
    }
}