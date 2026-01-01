package analisefundamental.estrategia;

import analisefundamental.modelo.Acao;

/**
 * Estratégia de avaliação específica para bancos.
 * Usa múltiplo P/TBV (Price to Tangible Book Value).
 * Bancos bem geridos tipicamente negociam a 1.0-1.5x TBV.
 */
public class EstrategiaBanco implements EstrategiaAvaliacao {

    @Override
    public double calcularPrecoJusto(Acao pAcao, double pTaxaLivreRisco) {
        // 1. Obter o TBV (Tangible Book Value) por ação
        // No modelo AcaoBanco, o método obterMetricaAvaliacao() já retorna o TBV.
        double tbvPorAcao = pAcao.obterMetricaAvaliacao();

        // Fallback: se o cálculo automático falhar, usar o valor contabilístico bruto
        if (tbvPorAcao <= 0) {
            tbvPorAcao = pAcao.obterDadosFinanceiros().obterValorContabilisticoPorAcao();
            if (tbvPorAcao <= 0) return 0.0; // Se ainda for 0, não é possível avaliar
        }

        // 2. Definir o Múltiplo Justo baseado na qualidade (ROE)
        // Bancos criam valor quando ROE > Custo Capital (~10%)
        double roe = pAcao.obterDadosFinanceiros().obterRoe();
        double multiploJusto;

        if (roe > 0.15) {
            multiploJusto = 1.5; // Banco Excelente (ex: JPM)
        } else if (roe >= 0.10) {
            multiploJusto = 1.2; // Banco Bom
        } else if (roe >= 0.08) {
            multiploJusto = 1.0; // Banco Médio
        } else {
            multiploJusto = 0.8; // Banco Fraco (destrói valor)
        }

        // 3. Calcular Preço Justo Bruto (TBV * Múltiplo)
        double valorBruto = tbvPorAcao * multiploJusto;

        // 4. Aplicar Margem de Segurança (30% de desconto -> multiplicar por 0.7)
        return valorBruto * 0.7;
    }
}