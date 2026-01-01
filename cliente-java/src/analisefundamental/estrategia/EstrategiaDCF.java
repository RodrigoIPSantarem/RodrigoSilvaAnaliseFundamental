package analisefundamental.estrategia;

import analisefundamental.modelo.Acao;
import analisefundamental.modelo.DadosFinanceiros;

public class EstrategiaDCF implements EstrategiaAvaliacao {

    @Override
    public double calcularPrecoJusto(Acao pAcao, double pTaxaLivreRisco) {
        DadosFinanceiros dados = pAcao.obterDadosFinanceiros();

        // 1. Obter Free Cash Flow Ajustado por Ação
        double fcfPorAcao = dados.calcularFCFAjustado();

        // Se FCF for negativo, tentamos usar o FCO como fallback para empresas em crescimento agressivo
        if (fcfPorAcao <= 0) {
            fcfPorAcao = dados.obterFluxoCaixaOperacional() / dados.obterAcoesCirculacaoAtual();
            if (fcfPorAcao <= 0) return 0.0;
        }

        // 2. Definir Taxa de Desconto (WACC)
        // Risco Base + Beta * Prémio Mercado (5.5%)
        double custoCapital = pTaxaLivreRisco + (pAcao.obterBeta() * 0.055);
        // Tech "Monstro" (Apple/MSFT) tem risco menor, permitimos 7.5% mínimo
        custoCapital = Math.max(0.075, custoCapital);

        // 3. Taxa de Crescimento (g)
        // Usamos o menor entre o histórico e 15% (já limitado pelo Python, mas garantimos aqui)
        double crescimento = Math.min(0.15, dados.obterCrescimentoLucros5A());
        if (crescimento < 0.03) crescimento = 0.03;

        // 4. Período de Crescimento
        // Empresas "Monstro" (Margem > 20% e ROE > 20%) crescem por 10 anos
        boolean ehMonstro = (dados.obterMargemLiquidaAtual() > 0.20 && dados.obterRoe() > 0.20);
        int anosCrescimento = ehMonstro ? 10 : 5;

        // 5. Taxa Terminal (Perpetuidade)
        double taxaTerminal = 0.03;

        // --- CÁLCULO DCF ---
        double valorPresenteTotal = 0.0;
        double fcfFuturo = fcfPorAcao;

        // Fase de Crescimento
        for (int i = 1; i <= anosCrescimento; i++) {
            fcfFuturo *= (1 + crescimento);
            valorPresenteTotal += fcfFuturo / Math.pow(1 + custoCapital, i);
        }

        // Valor Terminal
        double valorTerminal = (fcfFuturo * (1 + taxaTerminal)) / (custoCapital - taxaTerminal);
        double valorPresenteTerminal = valorTerminal / Math.pow(1 + custoCapital, anosCrescimento);

        valorPresenteTotal += valorPresenteTerminal;

        // 6. Margem de Segurança
        // Se for "Monstro", exigimos apenas 15% de desconto. Outros, 25%.
        double descontoSeguranca = ehMonstro ? 0.85 : 0.75;

        return valorPresenteTotal * descontoSeguranca;
    }
}