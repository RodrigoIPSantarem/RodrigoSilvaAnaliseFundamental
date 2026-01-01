package sistemacotacoes.modelo;

// Implementa Comparable para permitir ordenação natural (por Ticker)
public abstract class Ativo implements Analisavel, Comparable<Ativo> {
    protected String ticker;
    protected String nome;
    protected double preco;
    protected double variacao;
    protected long volume;

    public Ativo(String ticker, String nome, double preco, double variacao, long volume) {
        this.ticker = ticker;
        this.nome = nome;
        this.preco = preco;
        this.variacao = variacao;
        this.volume = volume;
    }

    // Método concreto (igual para todos)
    public String statusTendencia() {
        if (variacao > 0) return "ALTA 🟢";
        if (variacao < 0) return "BAIXA 🔴";
        return "NEUTRO ⚪";
    }

    // Comparable: Ordem alfabética por defeito
    @Override
    public int compareTo(Ativo outro) {
        return this.ticker.compareTo(outro.ticker);
    }

    @Override
    public String toString() {
        return String.format("%-8s | $%-8.2f | %6.2f%% | %s",
                ticker, preco, variacao, statusTendencia());
    }

    // Getters para os Comparators
    public double getPreco() { return preco; }
    public double getVariacao() { return variacao; }
    public long getVolume() { return volume; }
    public String getTicker() { return ticker; }
}