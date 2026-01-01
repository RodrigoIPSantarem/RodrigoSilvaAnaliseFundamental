package sistemacotacoes.modelo;

public class Cripto extends Ativo {
    public Cripto(String ticker, String nome, double preco, double variacao, long volume) {
        super(ticker, nome, preco, variacao, volume);
    }

    @Override
    public double calcularRisco() {
        // Cripto é mais arriscada: Multiplicador 3x
        return Math.abs(this.variacao) * 3.0;
    }

    @Override
    public String obterRecomendacao() {
        return "Alta Volatilidade - Apenas para especulação";
    }
}