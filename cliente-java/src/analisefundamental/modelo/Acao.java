package analisefundamental.modelo;

import analisefundamental.enums.*;
import analisefundamental.estrategia.EstrategiaAvaliacao;
import analisefundamental.util.BoolEMensagem;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe abstrata que representa uma ação no sistema de análise fundamental.
 * Implementa o Template Method para os Kill Switches universais.
 * Cada subclasse especializada implementa polimorfismo para análise setorial.
 */
public abstract class Acao {

    // ========== MEMBROS DE DADOS (ENCAPSULAMENTO) ==========
    protected String mTicker;
    protected String mNomeEmpresa;
    protected double mPrecoAtual;
    protected double mBeta;
    protected Setor mSetor;
    protected DadosFinanceiros mDadosFinanceiros;

    // Strategy Pattern: Estratégia de avaliação configurável
    protected EstrategiaAvaliacao mEstrategiaAvaliacao;

    // Análise Qualitativa (Protocolo v3.1: 30% da nota final)
    protected List<TipoMoat> mMoatLista = new ArrayList<>();
    protected double mPontuacaoQualitativa = 50.0; // Base 50/100

    // ========== CONSTRUTOR (TEMPLATE METHOD) ==========
    public Acao(String pTicker, String pNome, double pPreco, double pBeta,
                Setor pSetor, DadosFinanceiros pDados) {
        if (pTicker == null || pTicker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker não pode ser vazio");
        }
        if (pDados == null) {
            throw new IllegalArgumentException("DadosFinanceiros são obrigatórios");
        }

        this.mTicker = pTicker.toUpperCase().trim();
        this.mNomeEmpresa = (pNome != null) ? pNome : "Empresa Desconhecida";
        this.mPrecoAtual = Math.max(pPreco, 0.0);
        this.mBeta = Math.max(pBeta, 0.0);
        this.mSetor = (pSetor != null) ? pSetor : Setor.GERAL;
        this.mDadosFinanceiros = pDados;

        // Análise qualitativa inicial
        this.analisarQualitativoInicial();
    }//construtor

    // ========== MÉTODOS ABSTRATOS (POLIMORFISMO OBRIGATÓRIO) ==========

    /**
     * Retorna a métrica principal para avaliação, conforme o setor:
     * - Tecnologia: FCF Ajustado (FCF - SBC)
     * - Banca: Tangible Book Value (TBV)
     * - Utilities: Dividendo por Ação
     * - Geral: EPS (Lucros por Ação)
     */
    public abstract double obterMetricaAvaliacao();

    /**
     * Retorna o prêmio de risco específico do setor para cálculo do WACC:
     * Protocolo v3.1: US10Y + Prémio Setorial
     */
    public abstract double obterPremioRiscoSetor();

    /**
     * Verifica riscos específicos do setor (polimorfismo setorial):
     * - Tech: aceita prejuízo GAAP se FCF positivo
     * - Banca: verifica P/TBV e ROE
     * - Utilities: verifica yield mínimo
     */
    public abstract BoolEMensagem verificarRiscosSetoriais();

    // ========== TEMPLATE METHOD: KILL SWITCHES UNIVERSAL ==========
    /**
     * CORREÇÃO FEITA AQUI: Removi 'final' para permitir override no AcaoBanco.
     * Implementa TODOS os kill switches do protocolo v3.1.
     * Se falhar em QUALQUER um → REJEITAR IMEDIATAMENTE.
     */
    public List<String> verificarKillSwitchesUniversais() {
        List<String> violacoes = new ArrayList<>();
        DadosFinanceiros df = this.mDadosFinanceiros;

        // === KILL SWITCH 1: DILUIÇÃO CRÓNICA ===
        // "Nº ações aumentou >10% em 3 anos" (exceto REITs)
        if (this.mSetor != Setor.IMOBILIARIO) { // IMOBILIARIO = REITs
            double diluicao3Anos = df.calcularDiluicaoAcoes3Anos();
            if (diluicao3Anos > 0.10) { // >10%
                violacoes.add(String.format(
                        "KILL SWITCH 1: Diluição excessiva (+%.1f%% em 3 anos)",
                        diluicao3Anos * 100
                ));
            }
        }

        // === KILL SWITCH 2: DÍVIDA IMPAGÁVEL ===
        // "Dívida/EBITDA > 4×" (Utilities toleram até 4×, outros 3×)
        double limiteDivida = (this.mSetor == Setor.UTILITARIOS) ? 4.0 : 3.0;
        double dividaEbitda = df.obterDividaEbitda();
        if (dividaEbitda > limiteDivida) {
            violacoes.add(String.format(
                    "KILL SWITCH 2: Dívida elevada (%.1fx EBITDA > limite %.1fx)",
                    dividaEbitda, limiteDivida
            ));
        }

        // === KILL SWITCH 3: MARGENS EM QUEDA ===
        // "Margem Líquida caiu 3 anos consecutivos"
        if (df.temMargensEmQueda3Anos()) {
            violacoes.add("KILL SWITCH 3: Margens líquidas em queda há 3 anos consecutivos");
        }

        // === KILL SWITCH 4: HISTÓRICO IRREGULAR ===
        // "Prejuízo em 2 dos últimos 5 anos"
        int anosPrejuizo = df.contarAnosPrejuizoUltimos5();
        if (anosPrejuizo >= 2) {
            violacoes.add(String.format(
                    "KILL SWITCH 4: Histórico irregular (%d anos de prejuízo em 5)",
                    anosPrejuizo
            ));
        }

        // === KILL SWITCH 5: FCO NEGATIVO ===
        // "Fluxo Caixa Operacional negativo 2+ anos"
        if (df.temFCONegativo2Anos()) {
            violacoes.add("KILL SWITCH 5: Fluxo de Caixa Operacional negativo 2+ anos");
        }

        // === KILL SWITCH 6: GOODWILL EXCESSIVO ===
        // "Goodwill > 50% dos ativos totais"
        double racioGoodwill = df.calcularRacioGoodwillAtivos();
        if (racioGoodwill > 0.50) {
            violacoes.add(String.format(
                    "KILL SWITCH 6: Goodwill excessivo (%.1f%% dos ativos > 50%%)",
                    racioGoodwill * 100
            ));
        }

        // === KILL SWITCH 7: BETA MUITO ALTO (Protocolo Personalizado) ===
        // "Beta > 1.5" - REJEITAR para perfil conservador na seleção
        if (this.mBeta > 1.5) {
            violacoes.add(String.format(
                    "KILL SWITCH 7: Volatilidade excessiva (Beta = %.2f > 1.5)",
                    this.mBeta
            ));
        }

        return violacoes;
    }//verificarKillSwitchesUniversais

    // ========== FILTROS DE QUALIDADE (OBRIGATÓRIOS) ==========

    /**
     * FILTRO 1: Lucratividade Obrigatória
     * Protocolo: "PROIBIDO comprar empresas sem Lucro Líquido Positivo (GAAP) TTM"
     */
    public BoolEMensagem verificarLucratividade() {
        double eps = this.mDadosFinanceiros.obterLucrosPorAcaoTTM();

        // Exceção Tech: pode ter prejuízo GAAP se FCF > 0
        if (this.mSetor == Setor.TECNOLOGIA) {
            double fcfAjustado = obterMetricaAvaliacao(); // FCF - SBC
            if (eps < 0 && fcfAjustado > 0) {
                return new BoolEMensagem(true,
                        "Tech: Prejuízo GAAP aceitável porque FCF Ajustado > 0");
            }
        }

        if (eps <= 0) {
            return new BoolEMensagem(false,
                    "FILTRO QUALIDADE 1: Lucro Líquido (GAAP) negativo ou zero");
        }

        return new BoolEMensagem(true, "Lucratividade: OK");
    }//verificarLucratividade

    /**
     * FILTRO 2: Volatilidade Controlada
     * Protocolo: "Beta < 1.5 obrigatório"
     */
    public BoolEMensagem verificarVolatilidade() {
        if (this.mBeta >= 1.5) {
            return new BoolEMensagem(false,
                    String.format("FILTRO QUALIDADE 2: Beta elevado (%.2f > 1.5)", this.mBeta));
        }
        return new BoolEMensagem(true, "Volatilidade: OK (Beta = " + this.mBeta + ")");
    }//verificarVolatilidade

    /**
     * FILTRO 3: Moat Obrigatório
     * Protocolo: "Pelo menos 1 vantagem competitiva identificável"
     */
    public BoolEMensagem verificarMoat() {
        if (this.mMoatLista.isEmpty()) {
            return new BoolEMensagem(false,
                    "FILTRO QUALIDADE 3: Nenhum Moat identificado (empresa commodity)");
        }

        // Pontuação do Moat (Protocolo: Narrow = 10pts, Wide = 20pts)
        int pontosMoat = calcularPontosMoat();
        return new BoolEMensagem(true,
                String.format("Moat: %s (%d pontos)",
                        this.mMoatLista.toString(), pontosMoat));
    }//verificarMoat

    // ========== SISTEMA DE PONTUAÇÃO (Protocolo v3.1) ==========

    /**
     * Calcula a pontuação quantitativa (70% da nota final)
     * Baseada em métricas financeiras objetivas.
     */
    public double calcularPontuacaoQuantitativa() {
        DadosFinanceiros df = this.mDadosFinanceiros;
        double pontos = 0.0;

        // 1. ROE (>15% = +20pts, >10% = +10pts)
        double roe = df.obterRoe();
        if (roe > 0.15) pontos += 20;
        else if (roe > 0.10) pontos += 10;
        else if (roe > 0.05) pontos += 5;

        // 2. ROIC (>15% = +20pts, >10% = +10pts)
        double roic = df.obterRoic();
        if (roic > 0.15) pontos += 20;
        else if (roic > 0.10) pontos += 10;
        else if (roic > 0.05) pontos += 5;

        // 3. Crescimento Lucros 5A (>10% = +15pts, >5% = +10pts)
        double crescimentoLucros = df.obterCrescimentoLucros5A();
        if (crescimentoLucros > 0.10) pontos += 15;
        else if (crescimentoLucros > 0.05) pontos += 10;
        else if (crescimentoLucros > 0.0) pontos += 5;

        // 4. Margem Líquida (>20% = +15pts, >10% = +10pts)
        double margem = df.obterMargemLiquidaAtual();
        if (margem > 0.20) pontos += 15;
        else if (margem > 0.10) pontos += 10;
        else if (margem > 0.05) pontos += 5;

        // 5. Dívida Controlada (Dívida/EBITDA < 2x = +10pts)
        double dividaEbitda = df.obterDividaEbitda();
        if (dividaEbitda < 2.0) pontos += 10;
        else if (dividaEbitda < 3.0) pontos += 5;

        // 6. Beta Baixo (Beta < 1.0 = +10pts, < 1.3 = +5pts)
        if (this.mBeta < 1.0) pontos += 10;
        else if (this.mBeta < 1.3) pontos += 5;

        // Normalizar para 0-100
        return Math.min(pontos, 100.0);
    }//calcularPontuacaoQuantitativa

    /**
     * Calcula a pontuação qualitativa (30% da nota final)
     * Baseada em Moat e análise subjetiva.
     */
    public double calcularPontuacaoQualitativa() {
        double pontos = this.mPontuacaoQualitativa; // Base 50

        // Adicionar pontos do Moat
        pontos += calcularPontosMoat();

        // Ajustar por qualidade da governança (simplificado)
        double payout = this.mDadosFinanceiros.obterRacioDistribuicao();
        if (payout > 0 && payout < 0.6) { // Payout sustentável
            pontos += 10;
        }

        return Math.min(pontos, 100.0);
    }//calcularPontuacaoQualitativa

    /**
     * NOTA FINAL = 70% Quantitativa + 30% Qualitativa
     * Protocolo v3.1, página 10.
     */
    public double calcularNotaFinal() {
        double quantitativa = this.calcularPontuacaoQuantitativa();
        double qualitativa = this.calcularPontuacaoQualitativa();

        return (0.70 * quantitativa) + (0.30 * qualitativa);
    }//calcularNotaFinal

    // ========== AVALIAÇÃO DE PREÇO ==========

    /**
     * Calcula o Preço Justo usando a estratégia configurada.
     */
    public double calcularPrecoJusto(double pTaxaLivreRisco) {
        if (this.mEstrategiaAvaliacao == null) {
            throw new IllegalStateException("Estratégia de avaliação não definida");
        }
        return this.mEstrategiaAvaliacao.calcularPrecoJusto(this, pTaxaLivreRisco);
    }//calcularPrecoJusto

    /**
     * Calcula a Margem de Segurança.
     * Protocolo: só comprar se Preço Atual ≤ 75% do Preço Justo (margem ≥ 25%)
     */
    public double calcularMargemSeguranca(double pPrecoJusto) {
        if (pPrecoJusto <= 0) return 0.0;
        if (this.mPrecoAtual <= 0) return 0.0;

        // Margem = (Preço Justo - Preço Atual) / Preço Justo
        double margem = ((pPrecoJusto - this.mPrecoAtual) / pPrecoJusto) * 100.0;

        // Se preço atual for maior que preço justo, margem negativa
        return margem;
    }//calcularMargemSeguranca

    // ========== RECOMENDAÇÃO E LIMITES ==========

    /**
     * Determina a recomendação com base na Nota e Margem.
     * Protocolo v3.1, página 10.
     */
    public Recomendacao obterRecomendacao(double pMargemSeguranca, double pNotaFinal) {
        // Primeiro: verificar se tem kill switches
        if (!this.verificarKillSwitchesUniversais().isEmpty()) {
            return Recomendacao.REJEITAR;
        }

        // Segundo: verificar filtros de qualidade
        if (!this.verificarLucratividade().obterSucesso() ||
                !this.verificarVolatilidade().obterSucesso() ||
                !this.verificarMoat().obterSucesso()) {
            return Recomendacao.EVITAR;
        }

        // Terceiro: aplicar tabela de decisão
        if (pNotaFinal < 60) {
            return Recomendacao.EVITAR;
        }

        if (pNotaFinal >= 85) {
            if (pMargemSeguranca >= 40) return Recomendacao.COMPRA_FORTE;
            if (pMargemSeguranca >= 25) return Recomendacao.COMPRAR;
            return Recomendacao.VIGIAR;
        }

        if (pNotaFinal >= 80) {
            if (pMargemSeguranca >= 25) return Recomendacao.COMPRAR;
            return Recomendacao.VIGIAR;
        }

        // Nota 60-79
        return Recomendacao.VIGIAR;
    }//obterRecomendacao

    /**
     * Calcula o limite máximo de posição para esta ação.
     * Protocolo v3.1, página 3.
     */
    public double calcularLimitePosicao() {
        double nota = this.calcularNotaFinal();

        // Tabela de limites por Beta
        if (this.mBeta < 1.0 && nota >= 85 && this.mMoatLista.size() >= 2) {
            return 15.0; // Defensivo com Wide Moat e alta nota
        } else if (this.mBeta >= 1.3 && this.mBeta < 1.5) {
            return 5.0;  // Volátil
        } else if (this.mBeta >= 1.5) {
            return 3.0;  // Muito volátil
        } else {
            return 10.0; // Normal
        }
    }//calcularLimitePosicao

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    private void analisarQualitativoInicial() {
        // Análise simplificada baseada no setor
        switch (this.mSetor) {
            case TECNOLOGIA:
                this.mMoatLista.add(TipoMoat.CUSTOS_MUDANCA);
                this.mMoatLista.add(TipoMoat.EFEITOS_REDE);
                this.mPontuacaoQualitativa = 60.0;
                break;
            case FINANCEIRO:
                this.mMoatLista.add(TipoMoat.ESCALA_EFICIENTE);
                this.mPontuacaoQualitativa = 55.0;
                break;
            case UTILITARIOS:
                this.mMoatLista.add(TipoMoat.ESCALA_EFICIENTE);
                this.mPontuacaoQualitativa = 50.0;
                break;
            case CONSUMO_BASICO:
                this.mMoatLista.add(TipoMoat.ATIVOS_INTANGIVEIS);
                this.mPontuacaoQualitativa = 65.0;
                break;
            default:
                this.mPontuacaoQualitativa = 50.0;
        }
    }//analisarQualitativoInicial

    private int calcularPontosMoat() {
        if (this.mMoatLista.isEmpty()) return 0;
        if (this.mMoatLista.size() >= 2) return 20; // Wide Moat
        return 10; // Narrow Moat
    }//calcularPontosMoat

    // ========== GETTERS E SETTERS ==========

    public void definirEstrategia(EstrategiaAvaliacao pEstrategia) {
        this.mEstrategiaAvaliacao = pEstrategia;
    }//definirEstrategia

    public String obterTicker() { return this.mTicker; }
    public String obterNome() { return this.mNomeEmpresa; }
    public double obterPrecoAtual() { return this.mPrecoAtual; }
    public double obterBeta() { return this.mBeta; }
    public Setor obterSetor() { return this.mSetor; }
    public DadosFinanceiros obterDadosFinanceiros() { return this.mDadosFinanceiros; }
    public List<TipoMoat> obterMoatLista() { return new ArrayList<>(this.mMoatLista); }

    public void adicionarMoat(TipoMoat pMoat) {
        if (pMoat != null && !this.mMoatLista.contains(pMoat)) {
            this.mMoatLista.add(pMoat);
        }
    }//adicionarMoat

    // ========== REPRESENTAÇÃO TEXTUAL ==========

    @Override
    public String toString() {
        return String.format("[%s] %s | Preço: $%.2f | Beta: %.2f | Setor: %s",
                this.mTicker, this.mNomeEmpresa, this.mPrecoAtual,
                this.mBeta, this.mSetor);
    }//toString

    /**
     * Gera um resumo completo da análise.
     */
    public String gerarResumoAnalise(double pTaxaLivreRisco) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(70)).append("\n");
        sb.append("ANÁLISE FUNDAMENTAL: ").append(this.mTicker).append("\n");
        sb.append("=".repeat(70)).append("\n");

        // 1. Dados básicos
        sb.append(String.format("Empresa: %s\n", this.mNomeEmpresa));
        sb.append(String.format("Setor: %s | Beta: %.2f\n", this.mSetor, this.mBeta));
        sb.append(String.format("Preço Atual: $%.2f\n", this.mPrecoAtual));

        // 2. Kill Switches
        List<String> kills = this.verificarKillSwitchesUniversais();
        if (!kills.isEmpty()) {
            sb.append("\n🚫 KILL SWITCHES (REJEIÇÃO IMEDIATA):\n");
            for (String kill : kills) {
                sb.append("  • ").append(kill).append("\n");
            }
            return sb.toString();
        }

        // 3. Filtros de Qualidade
        sb.append("\n✅ FILTROS DE QUALIDADE:\n");
        sb.append("  • ").append(this.verificarLucratividade().obterMensagem()).append("\n");
        sb.append("  • ").append(this.verificarVolatilidade().obterMensagem()).append("\n");
        sb.append("  • ").append(this.verificarMoat().obterMensagem()).append("\n");

        // 4. Riscos Setoriais
        var riscoSetorial = this.verificarRiscosSetoriais();
        sb.append("  • Riscos Setoriais: ").append(riscoSetorial.obterMensagem()).append("\n");

        // 5. Cálculos
        double precoJusto = this.calcularPrecoJusto(pTaxaLivreRisco);
        double margem = this.calcularMargemSeguranca(precoJusto);
        double nota = this.calcularNotaFinal();
        var recomendacao = this.obterRecomendacao(margem, nota);
        double limite = this.calcularLimitePosicao();

        sb.append("\n📊 RESULTADOS:\n");
        sb.append(String.format("  • Preço Justo: $%.2f\n", precoJusto));
        sb.append(String.format("  • Margem Segurança: %.1f%%\n", margem));
        sb.append(String.format("  • Nota Final: %.1f/100\n", nota));
        sb.append(String.format("  • Recomendação: %s\n", recomendacao));
        sb.append(String.format("  • Limite Posição: %.1f%% do portefólio\n", limite));

        // 6. Conclusão
        sb.append("\n🎯 CONCLUSÃO: ");
        switch (recomendacao) {
            case COMPRA_FORTE:
                sb.append("COMPRA FORTE RECOMENDADA! (Margem ≥ 40%, Nota ≥ 85)");
                break;
            case COMPRAR:
                sb.append("COMPRAR (Margem ≥ 25%, Nota ≥ 80)");
                break;
            case VIGIAR:
                sb.append("ACOMPANHAR (aguardar melhor preço ou melhoria)");
                break;
            case EVITAR:
                sb.append("EVITAR (não atende critérios mínimos)");
                break;
            case REJEITAR:
                sb.append("REJEITAR (kill switches ativados)");
                break;
        }

        sb.append("\n").append("=".repeat(70));
        return sb.toString();
    }//gerarResumoAnalise

}//classe Acao