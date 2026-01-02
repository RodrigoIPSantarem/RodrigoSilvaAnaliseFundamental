// BoolEMensagem.java
package sistemacotacoes.util;

/**
 * Classe utilitária para retornos compostos (booleano + mensagem).
 * Baseada no padrão BoolAndMsg usado nas aulas do professor.
 * 
 * Demonstra: ENCAPSULAMENTO + Retornos compostos
 */
public class BoolEMensagem {
    private boolean mBool;
    private String mMensagem;

    //--------------------------------------------------
    // Construtor
    //--------------------------------------------------
    public BoolEMensagem(boolean pBool, String pMensagem) {
        this.mBool = pBool;
        this.mMensagem = pMensagem;
    }//construtor BoolEMensagem

    //--------------------------------------------------
    // Getters
    //--------------------------------------------------
    public boolean getBool() { return this.mBool; }
    public boolean sucesso() { return this.mBool; }
    public boolean falhou() { return !this.mBool; }
    
    public String getMensagem() { return this.mMensagem; }

    //--------------------------------------------------
    // toString
    //--------------------------------------------------
    @Override
    public String toString() {
        String estado = mBool ? "✅ SUCESSO" : "❌ FALHA";
        return String.format("%s: %s", estado, mMensagem);
    }//toString

}//classe BoolEMensagem
