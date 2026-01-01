package analisefundamental.estrategia;

import analisefundamental.modelo.Acao;

public class EstrategiaDCF implements EstrategiaAvaliacao {

    @Override
    public double calcularPrecoJusto(Acao pAcao, double pTaxaLivreRisco) {
        // Protocolo: WACC = US10Y + Prémio de Risco Setorial
        double wacc = pTaxaLivreRisco + pAcao.obterPremioRiscoSetor();

        // REGRA: Piso absoluto de 7% (Protocolo v3.1)
        if (wacc < 0.07) {
            wacc = 0.07;
        }

        // Obter FCF Ajustado (para Tech) ou FCF normal
        double fcfBase = pAcao.obterMetricaAvaliacao();
        if (fcfBase <= 0) {
            return 0.0;
        }

        // Crescimento: limitado a 12% para ser conservador
        double crescimento = pAcao.obterDadosFinanceiros().obterCrescimentoLucros5A();
        crescimento = Math.min(crescimento, 0.12);

        // 1. Projeção de 5 anos
        double somaFcfDescontados = 0.0;
        double fcfProjetado = fcfBase;

        for (int ano = 1; ano <= 5; ano++) {
            fcfProjetado = fcfProjetado * (1 + crescimento);
            double fcfDescontado = fcfProjetado / Math.pow(1 + wacc, ano);
            somaFcfDescontados += fcfDescontado;
        }

        // 2. Valor Terminal com g máximo de 2.5% (REGRA DO PROTOCOLO)
        double gTerminal = Math.min(crescimento, 0.025); // MÁXIMO 2.5%

        // Garantir que wacc > gTerminal
        if (wacc <= gTerminal) {
            gTerminal = wacc - 0.01; // Ajustar para diferença mínima
        }

        double valorTerminal = fcfProjetado * (1 + gTerminal) / (wacc - gTerminal);
        double valorTerminalDescontado = valorTerminal / Math.pow(1 + wacc, 5);

        // 3. Valor total da empresa
        double valorEmpresa = somaFcfDescontados + valorTerminalDescontado;

        // 4. Aplicar Margem de Segurança de 30% embutida
        return valorEmpresa * 0.7;
    }//calcularPrecoJusto
}//classe EstrategiaDCF