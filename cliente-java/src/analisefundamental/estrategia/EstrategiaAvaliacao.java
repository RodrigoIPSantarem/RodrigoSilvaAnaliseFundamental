package analisefundamental.estrategia;

import analisefundamental.modelo.Acao;

public interface EstrategiaAvaliacao {
    double calcularPrecoJusto(Acao pAcao, double pTaxaLivreRisco);
}//interface EstrategiaAvaliacao