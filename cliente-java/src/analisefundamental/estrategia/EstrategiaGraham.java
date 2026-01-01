package analisefundamental.estrategia;

import analisefundamental.modelo.Acao;

public class EstrategiaGraham implements EstrategiaAvaliacao {

    @Override
    public double calcularPrecoJusto(Acao pAcao, double pTaxaLivreRisco) {
        double eps = pAcao.obterMetricaAvaliacao(); // EPS
        if (eps <= 0) return 0.0;

        // Crescimento (g) limitado a 15% para ser conservador
        double g = Math.min(15, pAcao.obterDadosFinanceiros().obterCrescimentoLucros5A() * 100);
        if (g < 0) g = 0;

        // Yield AAA (Y) - mínimo 3%
        double y = Math.max(3.0, pTaxaLivreRisco * 100);

        // Fórmula Graham: (EPS * (8.5 + 2g) * 4.4) / Y * 0.7
        double valorBruto = (eps * (8.5 + (2 * g)) * 4.4) / y;
        return valorBruto * 0.7; // Margem de Segurança 30% embutida
    }//calcularPrecoJusto
}//classe EstrategiaGraham