// TipoAtivo.java
package sistemacotacoes.enums;

/**
 * Enum que representa os tipos de ativos financeiros suportados.
 * 
 * Demonstra: ENUM (como CarState, FuelType das aulas)
 */
public enum TipoAtivo {
    ACAO("Ação", "Participação em empresa cotada", 1.0),
    CRIPTO("Cripto", "Moeda digital descentralizada", 3.0),
    ETF("ETF", "Fundo de índice negociado", 0.5);

    private final String mNome;
    private final String mDescricao;
    private final double mMultiplicadorRisco;

    //--------------------------------------------------
    // Construtor do enum (privado por defeito)
    //--------------------------------------------------
    TipoAtivo(String pNome, String pDescricao, double pMultiplicador) {
        this.mNome = pNome;
        this.mDescricao = pDescricao;
        this.mMultiplicadorRisco = pMultiplicador;
    }//construtor TipoAtivo

    //--------------------------------------------------
    // Getters
    //--------------------------------------------------
    public String getNome() { return mNome; }
    public String getDescricao() { return mDescricao; }
    public double getMultiplicadorRisco() { return mMultiplicadorRisco; }

    @Override
    public String toString() {
        return mNome;
    }//toString

}//enum TipoAtivo
