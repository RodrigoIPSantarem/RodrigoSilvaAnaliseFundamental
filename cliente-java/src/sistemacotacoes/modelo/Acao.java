package sistemacotacoes.modelo;

public class Acao extends Ativo {
    public Acao(String ticker, String nome, double preco, double variacao, long volume) {
        super(ticker, nome, preco, variacao, volume);
    }

    @Override
    public double calcularRisco() {
        // Risco normal: Baseado na volatilidade simples
        return Math.abs(this.variacao);
    }

    @Override
    public String obterRecomendacao() {
        if (this.variacao < -5.0) return "Oportunidade (Desconto)";
        if (this.variacao > 5.0) return "Cuidado (Esticado)";
        return "Manter";
    }
}