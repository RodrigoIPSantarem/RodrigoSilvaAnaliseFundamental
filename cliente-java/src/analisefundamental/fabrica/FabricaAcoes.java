package analisefundamental.fabrica;

import analisefundamental.modelo.*;
import analisefundamental.enums.Setor;
import analisefundamental.estrategia.*;
import java.util.*;

public class FabricaAcoes {

    @SuppressWarnings("unchecked")
    public static Acao criarAcao(Map<String, Object> pJson) {
        // Extração segura
        String ticker = extrairString(pJson, "ticker");
        String nome = extrairString(pJson, "nomeEmpresa");
        String setorStr = extrairString(pJson, "setor");
        double preco = extrairDouble(pJson, "precoAtual");
        double beta = extrairDouble(pJson, "beta");

        Map<String, Object> fin = (Map<String, Object>) pJson.get("dadosFinanceiros");
        if (fin == null) fin = new HashMap<>();

        DadosFinanceiros dados = criarDadosFinanceiros(fin);
        Setor setor = determinarSetor(setorStr);

        // Factory Pattern
        Acao acao;
        switch (setor) {
            case TECNOLOGIA:
                acao = new AcaoTecnologia(ticker, nome, preco, beta, dados);
                acao.definirEstrategia(new EstrategiaDCF());
                break;
            case FINANCEIRO:
                acao = new AcaoBanco(ticker, nome, preco, beta, dados);
                acao.definirEstrategia(new EstrategiaBanco());
                break;
            case UTILITARIOS:
                acao = new AcaoUtilitaria(ticker, nome, preco, beta, dados);
                acao.definirEstrategia(new EstrategiaGordon());
                break;
            default:
                acao = new AcaoGeral(ticker, nome, preco, beta, setor, dados);
                acao.definirEstrategia(new EstrategiaGraham());
                break;
        }

        return acao;
    }

    @SuppressWarnings("unchecked")
    private static DadosFinanceiros criarDadosFinanceiros(Map<String, Object> m) {
        // Extrair valores com fallback
        double eps = extrairDouble(m, "lucrosPorAcaoTTM");
        double crescimento = extrairDouble(m, "crescimentoLucros5A");
        double divida = extrairDouble(m, "dividaEbitda");
        double roe = extrairDouble(m, "roe");
        double roic = extrairDouble(m, "roic");
        double margem = extrairDouble(m, "margemLiquidaAtual");

        // Históricos
        List<Double> histMargem = (List<Double>) m.getOrDefault("historicoMargemLiquida",
                Arrays.asList(margem * 0.95, margem * 0.98, margem));
        List<Double> histLucros = (List<Double>) m.getOrDefault("historicoLucros",
                Arrays.asList(eps * 0.9, eps * 0.95, eps, eps * 1.05, eps * 1.1));

        // Ações
        long acoesAtual = extrairLong(m, "acoesCirculacaoAtual");
        if (acoesAtual <= 0) acoesAtual = 1000000000L;

        List<Long> histAcoes = (List<Long>) m.getOrDefault("historicoAcoesCirculacao",
                Arrays.asList(acoesAtual, (long)(acoesAtual * 1.01), (long)(acoesAtual * 1.02)));

        // Fluxos
        double fco = extrairDouble(m, "fluxoCaixaOperacional");
        double capex = extrairDouble(m, "capex");
        double sbc = extrairDouble(m, "compensacaoBaseadaAcoes");
        double fcl = extrairDouble(m, "fluxoCaixaLivre");  // FCL direto da API

        // Campos opcionais
        double bookValue = extrairDouble(m, "valorContabilisticoPorAcao");
        double intangiveis = extrairDouble(m, "intangiveis");
        double goodwill = extrairDouble(m, "goodwill");
        double ativos = extrairDouble(m, "ativosTotal");
        double dividendo = extrairDouble(m, "dividendoPorAcao");
        double yield = extrairDouble(m, "rendimentoDividendo");
        double payout = extrairDouble(m, "racioDistribuicao");

        // Criar com construtor completo (21 parâmetros)
        DadosFinanceiros dados = new DadosFinanceiros(
                eps, crescimento, divida, roe, roic, margem,
                histMargem, acoesAtual, histAcoes, histLucros,
                fco, capex, sbc,
                bookValue, intangiveis, goodwill, ativos,
                dividendo, yield, payout
        );
        // Se a API enviou FCL direto, usar esse valor (mais preciso que FCO + Capex)
        if (fcl != 0.0) {
            dados.definirFluxoCaixaLivre(fcl);
        }

        return dados;
    }

    private static Setor determinarSetor(String pS) {
        if (pS == null) return Setor.GERAL;
        String s = pS.toLowerCase();
        if (s.contains("tech") || s.contains("software") || s.contains("hardware"))
            return Setor.TECNOLOGIA;
        if (s.contains("finan") || s.contains("bank") || s.contains("insurance"))
            return Setor.FINANCEIRO;
        if (s.contains("util") || s.contains("electric") || s.contains("water"))
            return Setor.UTILITARIOS;
        if (s.contains("consum") || s.contains("beverage") || s.contains("food"))
            return Setor.CONSUMO_BASICO;
        if (s.contains("health") || s.contains("pharma") || s.contains("medical"))
            return Setor.SAUDE;
        if (s.contains("indust") || s.contains("manufact"))
            return Setor.INDUSTRIAL;
        return Setor.GERAL;
    }

    // Métodos auxiliares de extração
    private static String extrairString(Map<String, Object> m, String chave) {
        Object val = m.get(chave);
        return (val != null) ? val.toString() : "";
    }

    private static double extrairDouble(Map<String, Object> m, String chave) {
        Object val = m.get(chave);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (Exception e) {}
        }
        return 0.0;
    }

    private static long extrairLong(Map<String, Object> m, String chave) {
        Object val = m.get(chave);
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) {
            try { return Long.parseLong((String) val); } catch (Exception e) {}
        }
        return 0L;
    }
}