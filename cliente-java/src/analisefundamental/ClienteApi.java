package analisefundamental;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Cliente HTTP para comunicar com a API Python.
 * VERSÃO ROBUSTA - Aceita JSON com ou sem espaços.
 */
public class ClienteApi {

    private static final String API_BASE_URL = "http://localhost:5000/api";
    private static final int TIMEOUT_MS = 10000;

    public static double obterTaxaTesouro() {
        try {
            String resposta = fazerRequisicaoGET(API_BASE_URL + "/tesouro/10anos");
            if (resposta != null) {
                return extrairDouble(resposta, "taxa_tesouro_10anos");
            }
        } catch (Exception e) {
            System.err.println("⚠️  Erro ao buscar taxa tesouro: " + e.getMessage());
        }
        return 0.043;
    }

    public static Map<String, Object> obterDadosAcao(String pTicker) {
        try {
            String url = API_BASE_URL + "/acao/" + pTicker.toUpperCase();
            String resposta = fazerRequisicaoGET(url);

            if (resposta == null || resposta.contains("\"erro\"")) {
                System.err.println("❌ Erro ao buscar " + pTicker + " (API retornou erro/null)");
                return criarDadosMock(pTicker);
            }
            return parseJson(resposta);

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar " + pTicker + ": " + e.getMessage());
            return criarDadosMock(pTicker);
        }
    }

    // --- MÉTODOS DE PARSE (Melhorados) ---

    private static Map<String, Object> parseJson(String pJson) {
        Map<String, Object> map = new HashMap<>();

        try {
            map.put("ticker", extrairValor(pJson, "ticker"));
            map.put("nomeEmpresa", extrairValor(pJson, "nomeEmpresa"));
            map.put("setor", extrairValor(pJson, "setor"));
            map.put("precoAtual", extrairDouble(pJson, "precoAtual"));
            map.put("beta", extrairDouble(pJson, "beta"));

            // MELHORIA: Busca robusta do objeto "dadosFinanceiros"
            // 1. Encontra a chave
            int keyIndex = pJson.indexOf("\"dadosFinanceiros\"");
            if (keyIndex != -1) {
                // 2. Encontra a primeira chaveta '{' depois da chave
                int braceIndex = pJson.indexOf("{", keyIndex);
                if (braceIndex != -1) {
                    // 3. Encontra o fim do objeto correspondente
                    int contentStart = braceIndex + 1;
                    int endFin = encontrarFimObjeto(pJson, contentStart);

                    if (endFin != -1) {
                        String finJson = pJson.substring(contentStart, endFin);
                        Map<String, Object> dadosFin = parseJsonObjeto(finJson);
                        map.put("dadosFinanceiros", dadosFin);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao parsear JSON: " + e.getMessage());
        }
        return map;
    }

    private static Map<String, Object> parseJsonObjeto(String pJson) {
        Map<String, Object> map = new HashMap<>();
        String[] campos = {
                "lucrosPorAcaoTTM", "crescimentoLucros5A", "dividaEbitda",
                "roe", "roic", "margemLiquidaAtual", "fluxoCaixaOperacional",
                "capex", "compensacaoBaseadaAcoes", "fluxoCaixaLivre",
                "valorContabilisticoPorAcao", "intangiveis", "goodwill",
                "ativosTotal", "dividendoPorAcao", "rendimentoDividendo", "racioDistribuicao"
        };

        for (String campo : campos) {
            map.put(campo, extrairDouble(pJson, campo));
        }

        map.put("historicoMargemLiquida", extrairListaDouble(pJson, "historicoMargemLiquida"));
        map.put("historicoAcoesCirculacao", extrairListaLong(pJson, "historicoAcoesCirculacao"));
        map.put("historicoLucros", extrairListaDouble(pJson, "historicoLucros"));
        map.put("acoesCirculacaoAtual", extrairLong(pJson, "acoesCirculacaoAtual"));

        return map;
    }

    private static String extrairValor(String json, String chave) {
        try {
            // Tenta "chave":"valor"
            String search = "\"" + chave + "\":\"";
            int start = json.indexOf(search);
            if (start == -1) {
                // Tenta "chave":valor (números, bools ou com espaços)
                search = "\"" + chave + "\":";
                start = json.indexOf(search);
                if (start == -1) return "";
                start += search.length();
            } else {
                start += search.length();
            }

            // Encontra o fim do valor (",", "}" ou "\"")
            // Ignora espaços iniciais
            while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                start++;
            }

            int end = json.indexOf("\"", start);
            int endComma = json.indexOf(",", start);
            int endBrace = json.indexOf("}", start);

            // Se for string, termina na aspa
            // Se for numero, termina na virgula ou chaveta
            int minEnd = json.length();
            if (end != -1 && end < minEnd) minEnd = end; // Aspa fecha string
            if (endComma != -1 && endComma < minEnd) minEnd = endComma;
            if (endBrace != -1 && endBrace < minEnd) minEnd = endBrace;

            if (minEnd == json.length()) return "";

            return json.substring(start, minEnd).replace("\"", "").trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static double extrairDouble(String json, String chave) {
        try {
            String val = extrairValor(json, chave);
            return val.isEmpty() ? 0.0 : Double.parseDouble(val);
        } catch (Exception e) { return 0.0; }
    }

    private static long extrairLong(String json, String chave) {
        try {
            String val = extrairValor(json, chave);
            return val.isEmpty() ? 0L : Long.parseLong(val);
        } catch (Exception e) { return 0L; }
    }

    private static List<Double> extrairListaDouble(String json, String chave) {
        List<Double> lista = new ArrayList<>();
        try {
            int start = json.indexOf("\"" + chave + "\":[");
            if (start == -1) return lista;
            start = json.indexOf("[", start) + 1;
            int end = json.indexOf("]", start);
            if (end == -1) return lista;

            String[] vals = json.substring(start, end).split(",");
            for (String v : vals) {
                if (!v.trim().isEmpty()) lista.add(Double.parseDouble(v.trim()));
            }
        } catch (Exception e) {}
        return lista;
    }

    private static List<Long> extrairListaLong(String json, String chave) {
        List<Long> lista = new ArrayList<>();
        try {
            int start = json.indexOf("\"" + chave + "\":[");
            if (start == -1) return lista;
            start = json.indexOf("[", start) + 1;
            int end = json.indexOf("]", start);
            if (end == -1) return lista;

            String[] vals = json.substring(start, end).split(",");
            for (String v : vals) {
                if (!v.trim().isEmpty()) lista.add(Long.parseLong(v.trim()));
            }
        } catch (Exception e) {}
        return lista;
    }

    private static int encontrarFimObjeto(String json, int start) {
        int profundidade = 1;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') profundidade++;
            else if (c == '}') {
                profundidade--;
                if (profundidade == 0) return i;
            }
        }
        return -1;
    }

    // --- REDE ---
    private static String fazerRequisicaoGET(String pUrl) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(pUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            if (conn.getResponseCode() == 200) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                return response.toString();
            }
            return null;
        } finally {
            if (reader != null) reader.close();
            if (conn != null) conn.disconnect();
        }
    }

    private static Map<String, Object> criarDadosMock(String pTicker) {
        // Mock simples para manter o sistema a rodar em caso de erro 404
        Map<String, Object> mock = new HashMap<>();
        mock.put("ticker", pTicker);
        mock.put("nomeEmpresa", "Mock " + pTicker);
        mock.put("precoAtual", 100.0);
        mock.put("setor", "Geral");
        // Nota: Sem dados financeiros, vai dar erro ou nota baixa, o que é esperado para falhas.
        return mock;
    }
}