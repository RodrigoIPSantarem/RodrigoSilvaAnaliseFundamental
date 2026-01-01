package analisefundamental.excecao;

import java.util.List;

public class ExcecaoKillSwitchViolado extends Exception {
    private List<String> mViolacoes;

    public ExcecaoKillSwitchViolado(String pMensagem, List<String> pViolacoes) {
        super(pMensagem);
        this.mViolacoes = pViolacoes;
    }//construtor

    public List<String> obterViolacoes() {
        return this.mViolacoes;
    }//obterViolacoes

    @Override
    public String toString() {
        return "Kill Switch Ativado: " + this.getMessage() + " | Detalhes: " + this.mViolacoes;
    }//toString
}//classe ExcecaoKillSwitchViolado