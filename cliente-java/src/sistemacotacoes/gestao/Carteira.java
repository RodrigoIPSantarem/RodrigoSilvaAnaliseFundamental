package sistemacotacoes.gestao;

import sistemacotacoes.modelo.Ativo;
import java.util.*;

public class Carteira {
    private List<Ativo> ativos; // Composição

    public Carteira() {
        this.ativos = new ArrayList<>();
    }

    public void adicionar(Ativo a) {
        ativos.add(a);
        System.out.println("✅ " + a.getTicker() + " adicionado.");
    }

    public void listar() {
        System.out.println("\n--- A MINHA CARTEIRA ---");
        for (Ativo a : ativos) {
            System.out.printf("%s | Risco: %.1f | Obs: %s\n",
                    a.toString(), a.calcularRisco(), a.obterRecomendacao());
        }
    }

    // --- ORDENAÇÕES ---
    public void ordenarPorPreco() {
        ativos.sort(Comparator.comparingDouble(Ativo::getPreco).reversed());
        System.out.println("\n🔽 Ordenado por Preço (Decrescente)");
    }

    public void ordenarPorVariacao() {
        ativos.sort(Comparator.comparingDouble(Ativo::getVariacao));
        System.out.println("\n🔼 Ordenado por Performance (Pior -> Melhor)");
    }
}