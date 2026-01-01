package analisefundamental.excecao;

import java.util.ArrayList;
import java.util.List;

public class ExcecaoDadosInsuficientes extends Exception {
    private String mTicker;
    private List<String> mCamposEmFalta;

    public ExcecaoDadosInsuficientes(String pTicker, String pMensagem) {
        super(pMensagem);
        this.mTicker = pTicker;
        this.mCamposEmFalta = new ArrayList<>();
    }//construtor

    public ExcecaoDadosInsuficientes(String pTicker, List<String> pCampos) {
        super("Dados insuficientes para análise de " + pTicker);
        this.mTicker = pTicker;
        this.mCamposEmFalta = pCampos;
    }//construtor sobrecarregado

    public String obterTicker() {
        return this.mTicker;
    }//obterTicker

    public List<String> obterCamposEmFalta() {
        return this.mCamposEmFalta;
    }//obterCamposEmFalta
}//classe ExcecaoDadosInsuficientes