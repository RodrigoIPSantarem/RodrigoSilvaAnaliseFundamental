package analisefundamental.estrategia;

import analisefundamental.modelo.Acao;

public class EstrategiaGordon implements EstrategiaAvaliacao {

    @Override
    public double calcularPrecoJusto(Acao pAcao, double pTaxaLivreRisco) {
        // Modelo de Gordon (Dividend Discount Model)
        // P = D₁ / (k - g)

        double dividendoAtual = pAcao.obterMetricaAvaliacao();
        if (dividendoAtual <= 0) {
            return 0.0;
        }

        // k = taxa de desconto (WACC)
        double k = pTaxaLivreRisco + pAcao.obterPremioRiscoSetor();

        // REGRA: Piso absoluto de 7%
        if (k < 0.07) {
            k = 0.07;
        }

        // g = crescimento do dividendo (conservador)
        double crescimentoLucros = pAcao.obterDadosFinanceiros().obterCrescimentoLucros5A();
        double g = Math.min(crescimentoLucros, 0.025); // MÁXIMO 2.5%

        // D₁ = Dividendo do próximo ano
        double d1 = dividendoAtual * (1 + g);

        // Verificar se k > g (requisito do modelo)
        if (k <= g) {
            // Fallback: múltiplo conservador de 15× (yield de 6.7%)
            return dividendoAtual * 15 * 0.7; // Com margem de segurança
        }

        // Aplicar fórmula de Gordon
        double valor = d1 / (k - g);

        // Margem de segurança de 30% embutida
        return valor * 0.7;
    }//calcularPrecoJusto

}//classe EstrategiaGordon