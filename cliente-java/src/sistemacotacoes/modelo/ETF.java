package sistemacotacoes.modelo;

/**
 * Representa um Exchange Traded Fund (Fundo de Índice).
 * Exemplo: S&P 500 (IVV), Nasdaq (QQQ).
 *
 * POO: Demonstra Polimorfismo ao ter um cálculo de risco muito mais baixo
 * devido à diversificação intrínseca do ativo.
 */
public class ETF extends Ativo {

    public ETF(String ticker, String nome, double preco, double variacao, long volume) {
        super(ticker, nome, preco, variacao, volume);
    }

    @Override
    public double calcularRisco() {
        // POLIMORFISMO:
        // ETFs são cestos de ações diversificados.
        // O risco é considerado metade (0.5) da volatilidade apresentada.
        return Math.abs(this.variacao) * 0.5;
    }

    @Override
    public String obterRecomendacao() {
        // Estratégia típica para ETFs: "Buy and Hold" ou reforçar nas quedas
        if (this.variacao < -1.5) {
            return "Reforçar (Oportunidade)";
        }
        return "Manter para Longo Prazo";
    }
}