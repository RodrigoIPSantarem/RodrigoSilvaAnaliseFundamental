package analisefundamental.comparador;

import analisefundamental.modelo.Acao;
import java.util.Comparator;

public enum ComparadoresAcao {
    POR_NOTA_FINAL(new ComparadorNotaFinal()),
    POR_MARGEM_SEGURANCA(new ComparadorMargemSeguranca()),
    POR_NOTA_DEPOIS_MARGEM(new ComparadorNotaDepoisMargem()),
    POR_BETA(new ComparadorBeta()),
    POR_SETOR(new ComparadorSetor());

    Comparator<Acao> mComparador;

    // CORREÇÃO: O nome do construtor deve ser IGUAL ao nome do enum
    ComparadoresAcao(Comparator<Acao> pC) {  // ← "ComparadoresAcao", não "ComparadoresAcoes"
        this.mComparador = pC;
    }//construtor

    public Comparator<Acao> obterComparador() {
        return this.mComparador;
    }//obterComparador

    // 1. Comparador por Nota Final (decrescente)
    static class ComparadorNotaFinal implements Comparator<Acao> {
        @Override
        public int compare(Acao a1, Acao a2) {
            return Double.compare(a2.calcularNotaFinal(), a1.calcularNotaFinal());
        }
    }//ComparadorNotaFinal

    // 2. Comparador por Margem de Segurança (decrescente)
    static class ComparadorMargemSeguranca implements Comparator<Acao> {
        @Override
        public int compare(Acao a1, Acao a2) {
            double m1 = a1.calcularMargemSeguranca(a1.calcularPrecoJusto(0.043));
            double m2 = a2.calcularMargemSeguranca(a2.calcularPrecoJusto(0.043));
            return Double.compare(m2, m1);
        }
    }//ComparadorMargemSeguranca

    // 3. Comparador por Nota e depois Margem
    static class ComparadorNotaDepoisMargem implements Comparator<Acao> {
        @Override
        public int compare(Acao a1, Acao a2) {
            int cmpNota = Double.compare(a2.calcularNotaFinal(), a1.calcularNotaFinal());
            if (cmpNota != 0) return cmpNota;

            double m1 = a1.calcularMargemSeguranca(a1.calcularPrecoJusto(0.043));
            double m2 = a2.calcularMargemSeguranca(a2.calcularPrecoJusto(0.043));
            return Double.compare(m2, m1);
        }
    }//ComparadorNotaDepoisMargem

    // 4. Comparador por Beta (crescente)
    static class ComparadorBeta implements Comparator<Acao> {
        @Override
        public int compare(Acao a1, Acao a2) {
            return Double.compare(a1.obterBeta(), a2.obterBeta());
        }
    }//ComparadorBeta

    // 5. Comparador por Setor (alfabético)
    static class ComparadorSetor implements Comparator<Acao> {
        @Override
        public int compare(Acao a1, Acao a2) {
            return a1.obterSetor().name().compareTo(a2.obterSetor().name());
        }
    }//ComparadorSetor
}//enum ComparadoresAcao