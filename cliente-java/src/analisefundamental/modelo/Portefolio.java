package analisefundamental.modelo;

import analisefundamental.comparador.ComparadoresAcao;
import analisefundamental.enums.Recomendacao;
import analisefundamental.enums.Setor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe que representa um portefólio de investimentos.
 * Gerencia múltiplas ações, análise agregada e geração de relatórios.
 */
public class Portefolio {

    // ========== MEMBROS DE DADOS ==========
    private String mNomeInvestidor;
    private List<Acao> mAcoes;
    private double mTaxaLivreRisco;
    private double mCapitalTotal;

    // ========== CONSTRUTORES ==========

    /**
     * Construtor padrão.
     */
    public Portefolio() {
        this("Investidor Anônimo", 0.043, 100000.0); // 4.3% default, €100k
    }//construtor padrão

    /**
     * Construtor completo.
     */
    public Portefolio(String pNomeInvestidor, double pTaxaLivreRisco, double pCapitalTotal) {
        if (pNomeInvestidor == null || pNomeInvestidor.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do investidor não pode ser vazio");
        }
        if (pCapitalTotal <= 0) {
            throw new IllegalArgumentException("Capital total deve ser positivo");
        }

        this.mNomeInvestidor = pNomeInvestidor;
        this.mAcoes = new ArrayList<>();
        this.mTaxaLivreRisco = Math.max(pTaxaLivreRisco, 0.0);
        this.mCapitalTotal = pCapitalTotal;
    }//construtor completo

    // ========== OPERAÇÕES BÁSICAS ==========

    /**
     * Adiciona uma ação ao portefólio.
     */
    public void adicionarAcao(Acao pAcao) {
        if (pAcao == null) {
            throw new IllegalArgumentException("Ação não pode ser nula");
        }

        // Verificar se já existe (por ticker)
        String tickerNovo = pAcao.obterTicker();
        boolean jaExiste = this.mAcoes.stream()
                .anyMatch(a -> a.obterTicker().equalsIgnoreCase(tickerNovo));

        if (jaExiste) {
            throw new IllegalArgumentException(
                    "Ação " + tickerNovo + " já existe no portefólio");
        }

        this.mAcoes.add(pAcao);
    }//adicionarAcao

    /**
     * Remove uma ação do portefólio.
     */
    public boolean removerAcao(String pTicker) {
        if (pTicker == null) return false;

        return this.mAcoes.removeIf(a ->
                a.obterTicker().equalsIgnoreCase(pTicker.trim()));
    }//removerAcao

    /**
     * Encontra uma ação pelo ticker.
     */
    public Acao encontrarAcao(String pTicker) {
        return this.mAcoes.stream()
                .filter(a -> a.obterTicker().equalsIgnoreCase(pTicker.trim()))
                .findFirst()
                .orElse(null);
    }//encontrarAcao

    // ========== ANÁLISE AGREDADA ==========

    /**
     * Retorna lista de ações APROVADAS (Nota ≥ 80 + Margem ≥ 25%).
     */
    public List<Acao> obterAcoesAprovadas() {
        return this.mAcoes.stream()
                .filter(a -> a.verificarKillSwitchesUniversais().isEmpty())
                .filter(a -> {
                    double precoJusto = a.calcularPrecoJusto(this.mTaxaLivreRisco);
                    double margem = a.calcularMargemSeguranca(precoJusto);
                    double nota = a.calcularNotaFinal();
                    return nota >= 80.0 && margem >= 25.0;
                })
                .collect(Collectors.toList());
    }//obterAcoesAprovadas

    /**
     * Retorna lista de ações para VIGIAR (Nota 60-79 ou Margem < 25%).
     */
    public List<Acao> obterAcoesVigiadas() {
        return this.mAcoes.stream()
                .filter(a -> a.verificarKillSwitchesUniversais().isEmpty())
                .filter(a -> {
                    double precoJusto = a.calcularPrecoJusto(this.mTaxaLivreRisco);
                    double margem = a.calcularMargemSeguranca(precoJusto);
                    double nota = a.calcularNotaFinal();
                    return (nota >= 60.0 && nota < 80.0) || margem < 25.0;
                })
                .collect(Collectors.toList());
    }//obterAcoesVigiadas

    /**
     * Retorna lista de ações REJEITADAS (Kill Switches ou Nota < 60).
     */
    public List<Acao> obterAcoesRejeitadas() {
        return this.mAcoes.stream()
                .filter(a -> !a.verificarKillSwitchesUniversais().isEmpty() ||
                        a.calcularNotaFinal() < 60.0)
                .collect(Collectors.toList());
    }//obterAcoesRejeitadas

    /**
     * Retorna mapa com estatísticas do portefólio.
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();

        int total = this.mAcoes.size();
        int aprovadas = this.obterAcoesAprovadas().size();
        int vigiadas = this.obterAcoesVigiadas().size();
        int rejeitadas = this.obterAcoesRejeitadas().size();

        stats.put("totalAcoes", total);
        stats.put("aprovadas", aprovadas);
        stats.put("vigiadas", vigiadas);
        stats.put("rejeitadas", rejeitadas);
        stats.put("percentagemAprovadas", total > 0 ? (aprovadas * 100.0 / total) : 0.0);

        // Distribuição por setor
        Map<Setor, Long> distribuicaoSetor = this.mAcoes.stream()
                .collect(Collectors.groupingBy(Acao::obterSetor, Collectors.counting()));
        stats.put("distribuicaoSetor", distribuicaoSetor);

        // Média de notas
        double mediaNota = this.mAcoes.stream()
                .mapToDouble(Acao::calcularNotaFinal)
                .average()
                .orElse(0.0);
        stats.put("mediaNota", mediaNota);

        return stats;
    }//obterEstatisticas

    // ========== ORDENAÇÃO ==========

    /**
     * Ordena o portefólio por um comparador específico.
     */
    public void ordenarPor(ComparadoresAcao pComparador) {
        if (pComparador == null) return;
        this.mAcoes.sort(pComparador.obterComparador());
    }//ordenarPor

    /**
     * Ordena por Nota Final (decrescente).
     */
    public void ordenarPorNotaFinal() {
        this.ordenarPor(ComparadoresAcao.POR_NOTA_FINAL);
    }//ordenarPorNotaFinal

    /**
     * Ordena por Margem de Segurança (decrescente).
     */
    public void ordenarPorMargemSeguranca() {
        this.ordenarPor(ComparadoresAcao.POR_MARGEM_SEGURANCA);
    }//ordenarPorMargemSeguranca

    /**
     * Ordena por Nota e depois por Margem (decrescente).
     */
    public void ordenarPorNotaDepoisMargem() {
        this.ordenarPor(ComparadoresAcao.POR_NOTA_DEPOIS_MARGEM);
    }//ordenarPorNotaDepoisMargem

    // ========== RELATÓRIOS ==========

    /**
     * Gera relatório completo do portefólio.
     */
    public String gerarRelatorioCompleto() {
        if (this.mAcoes.isEmpty()) {
            return "Portefólio vazio. Adicione ações para análise.";
        }

        StringBuilder sb = new StringBuilder();

        // Cabeçalho
        sb.append("=".repeat(80)).append("\n");
        sb.append("                    RODRIGO SILVA - ANÁLISE FUNDAMENTAL\n");
        sb.append("                           RELATÓRIO DO PORTEFÓLIO\n");
        sb.append("=".repeat(80)).append("\n\n");

        sb.append(String.format("INVESTIDOR: %s\n", this.mNomeInvestidor));
        sb.append(String.format("CAPITAL TOTAL: €%.2f\n", this.mCapitalTotal));
        sb.append(String.format("TAXA LIVRE DE RISCO: %.2f%%\n", this.mTaxaLivreRisco * 100));
        sb.append(String.format("TOTAL DE AÇÕES ANALISADAS: %d\n\n", this.mAcoes.size()));

        // 1. Estatísticas Gerais
        sb.append(gerarSecaoEstatisticas());
        sb.append("\n");

        // 2. Ações Aprovadas (COMPRAR)
        sb.append(gerarSecaoAprovadas());
        sb.append("\n");

        // 3. Lista de Vigilância
        sb.append(gerarSecaoVigiadas());
        sb.append("\n");

        // 4. Ações Rejeitadas
        sb.append(gerarSecaoRejeitadas());

        // 5. Recomendações de Alocação
        sb.append(gerarSecaoAlocacao());

        sb.append("=".repeat(80)).append("\n");
        sb.append("FIM DO RELATÓRIO\n");
        sb.append("=".repeat(80));

        return sb.toString();
    }//gerarRelatorioCompleto

    private String gerarSecaoEstatisticas() {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> stats = this.obterEstatisticas();

        sb.append("📊 ESTATÍSTICAS GERAIS:\n");
        sb.append("-".repeat(40)).append("\n");

        sb.append(String.format("Total de Ações Analisadas: %d\n",
                (int) stats.get("totalAcoes")));
        sb.append(String.format("Aprovadas para Compra: %d (%.1f%%)\n",
                (int) stats.get("aprovadas"), (double) stats.get("percentagemAprovadas")));
        sb.append(String.format("Em Vigilância: %d\n", (int) stats.get("vigiadas")));
        sb.append(String.format("Rejeitadas: %d\n", (int) stats.get("rejeitadas")));
        sb.append(String.format("Média de Notas: %.1f/100\n", (double) stats.get("mediaNota")));

        // Distribuição por setor
        @SuppressWarnings("unchecked")
        Map<Setor, Long> distribuicao = (Map<Setor, Long>) stats.get("distribuicaoSetor");
        if (!distribuicao.isEmpty()) {
            sb.append("\nDistribuição por Setor:\n");
            distribuicao.forEach((setor, count) -> {
                sb.append(String.format("  • %-20s: %d ação(ões)\n",
                        setor.name(), count));
            });
        }

        return sb.toString();
    }//gerarSecaoEstatisticas

    private String gerarSecaoAprovadas() {
        StringBuilder sb = new StringBuilder();
        List<Acao> aprovadas = this.obterAprovasOrdenadas();

        if (aprovadas.isEmpty()) {
            sb.append("✅ AÇÕES APROVADAS PARA COMPRA:\n");
            sb.append("-".repeat(40)).append("\n");
            sb.append("Nenhuma ação atende todos os critérios de compra.\n");
            return sb.toString();
        }

        sb.append("✅ AÇÕES APROVADAS PARA COMPRA (Nota ≥ 80 + Margem ≥ 25%):\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append(String.format("%-4s | %-6s | %-20s | %-10s | %-8s | %-8s | %-6s | %-6s | %-12s\n",
                "#", "Ticker", "Empresa", "Setor", "Preço", "P.Justo", "Margem", "Nota", "Recom."));
        sb.append("-".repeat(80)).append("\n");

        int i = 1;
        for (Acao a : aprovadas) {
            double pj = a.calcularPrecoJusto(this.mTaxaLivreRisco);
            double margem = a.calcularMargemSeguranca(pj);
            double nota = a.calcularNotaFinal();
            Recomendacao rec = a.obterRecomendacao(margem, nota);
            double limite = a.calcularLimitePosicao();

            // Truncar nomes longos
            String nomeCurto = a.obterNome();
            if (nomeCurto.length() > 20) {
                nomeCurto = nomeCurto.substring(0, 17) + "...";
            }

            String setorCurto = a.obterSetor().name();
            if (setorCurto.length() > 10) {
                setorCurto = setorCurto.substring(0, 7) + "...";
            }

            sb.append(String.format("%-4d | %-6s | %-20s | %-10s | $%-7.2f | $%-7.2f | %-6.1f%% | %-6.1f | %-12s\n",
                    i++, a.obterTicker(), nomeCurto, setorCurto,
                    a.obterPrecoAtual(), pj, margem, nota, rec));
        }

        return sb.toString();
    }//gerarSecaoAprovadas

    private String gerarSecaoVigiadas() {
        StringBuilder sb = new StringBuilder();
        List<Acao> vigiadas = this.obterAcoesVigiadas();

        if (vigiadas.isEmpty()) {
            return "";
        }

        sb.append("👀 LISTA DE VIGILÂNCIA (Nota 60-79 ou Margem < 25%):\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append(String.format("%-4s | %-6s | %-20s | %-10s | %-8s | %-8s | %-6s | %-6s | %-12s\n",
                "#", "Ticker", "Empresa", "Setor", "Preço", "P.Justo", "Margem", "Nota", "Estado"));
        sb.append("-".repeat(80)).append("\n");

        int i = 1;
        for (Acao a : vigiadas) {
            double pj = a.calcularPrecoJusto(this.mTaxaLivreRisco);
            double margem = a.calcularMargemSeguranca(pj);
            double nota = a.calcularNotaFinal();
            String estado = margem < 25.0 ? "CARO" : "MELHORAR";

            // Truncar nomes longos
            String nomeCurto = a.obterNome();
            if (nomeCurto.length() > 20) {
                nomeCurto = nomeCurto.substring(0, 17) + "...";
            }

            String setorCurto = a.obterSetor().name();
            if (setorCurto.length() > 10) {
                setorCurto = setorCurto.substring(0, 7) + "...";
            }

            sb.append(String.format("%-4d | %-6s | %-20s | %-10s | $%-7.2f | $%-7.2f | %-6.1f%% | %-6.1f | %-12s\n",
                    i++, a.obterTicker(), nomeCurto, setorCurto,
                    a.obterPrecoAtual(), pj, margem, nota, estado));
        }

        return sb.toString();
    }//gerarSecaoVigiadas

    private String gerarSecaoRejeitadas() {
        StringBuilder sb = new StringBuilder();
        List<Acao> rejeitadas = this.obterAcoesRejeitadas();

        if (rejeitadas.isEmpty()) {
            return "";
        }

        sb.append("❌ AÇÕES REJEITADAS (Kill Switches ou Nota < 60):\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append(String.format("%-4s | %-6s | %-60s\n",
                "#", "Ticker", "Motivo Principal"));
        sb.append("-".repeat(80)).append("\n");

        int i = 1;
        for (Acao a : rejeitadas) {
            List<String> kills = a.verificarKillSwitchesUniversais();
            String motivo;

            if (!kills.isEmpty()) {
                motivo = kills.get(0);
            } else {
                double nota = a.calcularNotaFinal();
                motivo = String.format("Nota insuficiente (%.1f/100 < 60)", nota);
            }

            // Truncar motivo longo
            if (motivo.length() > 60) {
                motivo = motivo.substring(0, 57) + "...";
            }

            sb.append(String.format("%-4d | %-6s | %-60s\n",
                    i++, a.obterTicker(), motivo));
        }

        return sb.toString();
    }//gerarSecaoRejeitadas

    private String gerarSecaoAlocacao() {
        StringBuilder sb = new StringBuilder();
        List<Acao> aprovadas = this.obterAprovadasOrdenadas();

        if (aprovadas.isEmpty()) {
            return "\n💡 RECOMENDAÇÃO: Nenhuma ação aprovada no momento. " +
                    "Aguardar melhores oportunidades.\n";
        }

        sb.append("\n💰 RECOMENDAÇÕES DE ALOAÇÃO (% do Capital Total):\n");
        sb.append("=".repeat(80)).append("\n");
        sb.append(String.format("%-4s | %-6s | %-20s | %-12s | %-10s | %-12s | %-12s\n",
                "#", "Ticker", "Empresa", "Limite Pos.", "Valor Invest.", "% Capital", "Nota"));
        sb.append("-".repeat(80)).append("\n");

        double capitalAlocado = 0.0;
        int i = 1;

        for (Acao a : aprovadas) {
            double limitePercent = a.calcularLimitePosicao();
            double valorInvestir = (limitePercent / 100.0) * this.mCapitalTotal;
            double percentCapital = (valorInvestir / this.mCapitalTotal) * 100.0;
            double nota = a.calcularNotaFinal();

            capitalAlocado += valorInvestir;

            // Truncar nome
            String nomeCurto = a.obterNome();
            if (nomeCurto.length() > 20) {
                nomeCurto = nomeCurto.substring(0, 17) + "...";
            }

            sb.append(String.format("%-4d | %-6s | %-20s | %-11.1f%% | €%-11.0f | %-10.1f%% | %-11.1f\n",
                    i++, a.obterTicker(), nomeCurto,
                    limitePercent, valorInvestir, percentCapital, nota));
        }

        sb.append("-".repeat(80)).append("\n");
        sb.append(String.format("TOTAL ALOAÇÃO RECOMENDADA: €%.0f (%.1f%% do capital)\n",
                capitalAlocado, (capitalAlocado / this.mCapitalTotal) * 100));
        sb.append(String.format("CAPITAL NÃO ALOAADO: €%.0f (%.1f%% do capital)\n",
                this.mCapitalTotal - capitalAlocado,
                ((this.mCapitalTotal - capitalAlocado) / this.mCapitalTotal) * 100));

        return sb.toString();
    }//gerarSecaoAlocacao

    private List<Acao> obterAprovadasOrdenadas() {
        List<Acao> aprovadas = this.obterAcoesAprovadas();
        // Ordenar por nota depois margem
        aprovadas.sort((a1, a2) -> {
            int cmpNota = Double.compare(
                    a2.calcularNotaFinal(), a1.calcularNotaFinal());
            if (cmpNota != 0) return cmpNota;

            double pj1 = a1.calcularPrecoJusto(this.mTaxaLivreRisco);
            double pj2 = a2.calcularPrecoJusto(this.mTaxaLivreRisco);
            double margem1 = a1.calcularMargemSeguranca(pj1);
            double margem2 = a2.calcularMargemSeguranca(pj2);
            return Double.compare(margem2, margem1);
        });
        return aprovadas;
    }//obterAprovadasOrdenadas

    // ========== GETTERS E SETTERS ==========

    public String obterNomeInvestidor() { return this.mNomeInvestidor; }
    public void definirNomeInvestidor(String pNome) {
        if (pNome != null && !pNome.trim().isEmpty()) {
            this.mNomeInvestidor = pNome;
        }
    }//definirNomeInvestidor

    public double obterTaxaLivreRisco() { return this.mTaxaLivreRisco; }
    public void definirTaxaLivreRisco(double pTaxa) {
        this.mTaxaLivreRisco = Math.max(pTaxa, 0.0);
    }//definirTaxaLivreRisco

    public double obterCapitalTotal() { return this.mCapitalTotal; }
    public void definirCapitalTotal(double pCapital) {
        if (pCapital > 0) {
            this.mCapitalTotal = pCapital;
        }
    }//definirCapitalTotal

    public List<Acao> obterTodasAcoes() {
        return new ArrayList<>(this.mAcoes);
    }//obterTodasAcoes

    public int obterNumeroAcoes() {
        return this.mAcoes.size();
    }//obterNumeroAcoes

    // ========== REPRESENTAÇÃO TEXTUAL ==========

    @Override
    public String toString() {
        return String.format("Portefólio de %s: %d ação(ões), Capital: €%.0f",
                this.mNomeInvestidor, this.mAcoes.size(), this.mCapitalTotal);
    }//toString

}//classe Portefolio