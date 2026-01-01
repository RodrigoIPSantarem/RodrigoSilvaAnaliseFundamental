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
 * VERSÃO FINAL - Funciona com API real.
 */
public class ClienteApi {

    private static final String API_BASE_URL = "http://localhost:5000/api";
    private static final int TIMEOUT_MS = 10000; // 10 segundos

    /**
     * Testa se a API Python está online.
     */
    public static boolean testarConexao() {
        try {
            String resposta = fazerRequisicaoGET(API_BASE_URL + "/estado");
            return resposta != null && resposta.contains("online");
        } catch (Exception e) {
            System.err.println("❌ API Python não está disponível: " + e.getMessage());
            return false;
        }
    }//testarConexao

    /**
     * Obtém a taxa do Tesouro dos EUA a 10 anos.
     */
    public static double obterTaxaTesouro() {
        try {
            String resposta = fazerRequisicaoGET(API_BASE_URL + "/tesouro/10anos");

            if (resposta != null && resposta.contains("taxa_tesouro_10anos")) {
                // Parse simples do JSON
                int start = resposta.indexOf("taxa_tesouro_10anos") + 21;
                int end = resposta.indexOf(",", start);
                if (end == -1) end = resposta.indexOf("}", start);

                String valorStr = resposta.substring(start, end).trim();
                return Double.parseDouble(valorStr);
            }
        } catch (Exception e) {
            System.err.println("⚠️  Erro ao buscar taxa tesouro: " + e.getMessage());
        }

        // Fallback: 4.3%
        return 0.043;
    }//obterTaxaTesouro

    /**
     * Obtém dados de uma ação específica.
     */
    public static Map<String, Object> obterDadosAcao(String pTicker) {
        try {
            String url = API_BASE_URL + "/acao/" + pTicker.toUpperCase();
            String resposta = fazerRequisicaoGET(url);

            if (resposta == null || resposta.contains("\"erro\"")) {
                System.err.println("❌ Erro ao buscar " + pTicker);
                return criarDadosMock(pTicker); // Fallback para mock
            }

            return parseJson(resposta);

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar " + pTicker + ": " + e.getMessage());
            return criarDadosMock(pTicker);
        }
    }//obterDadosAcao

    /**
     * Obtém dados de múltiplas ações de uma vez.
     */
    public static List<Map<String, Object>> obterDadosMultiplasAcoes(String... pTickers) {
        List<Map<String, Object>> resultados = new ArrayList<>();

        try {
            // Construir query string
            StringBuilder query = new StringBuilder();
            for (String ticker : pTickers) {
                if (ticker != null && !ticker.trim().isEmpty()) {
                    if (query.length() > 0) query.append(",");
                    query.append(ticker.trim().toUpperCase());
                }
            }

            String url = API_BASE_URL + "/acoes?tickers=" + query.toString();
            String resposta = fazerRequisicaoGET(url);

            if (resposta != null && resposta.contains("\"acoes\"")) {
                // Parse do array de ações
                int startArray = resposta.indexOf("\"acoes\":[") + 9;
                int endArray = resposta.lastIndexOf("]");

                if (startArray > 9 && endArray > startArray) {
                    String arrayJson = resposta.substring(startArray, endArray);
                    // Parse simplificado (em produção usar Jackson/Gson)
                    resultados = parseJsonArray(arrayJson);
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️  Erro em lote: " + e.getMessage());
        }

        // Se falhou, buscar uma a uma
        if (resultados.isEmpty()) {
            for (String ticker : pTickers) {
                if (ticker != null && !ticker.trim().isEmpty()) {
                    Map<String, Object> dados = obterDadosAcao(ticker);
                    resultados.add(dados);
                }
            }
        }

        return resultados;
    }//obterDadosMultiplasAcoes

    // ========== MÉTODOS PRIVADOS ==========

    private static String fazerRequisicaoGET(String pUrl) throws Exception {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(pUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "RodrigoSilvaAnalise/1.0");

            int status = conn.getResponseCode();

            if (status >= 200 && status < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();
            } else {
                System.err.println("HTTP " + status + " para: " + pUrl);
                return null;
            }

        } finally {
            if (reader != null) reader.close();
            if (conn != null) conn.disconnect();
        }
    }//fazerRequisicaoGET

    private static String fazerRequisicaoPOST(String pUrl, String pJson) throws Exception {
        HttpURLConnection conn = null;
        OutputStream os = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(pUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Enviar JSON
            os = conn.getOutputStream();
            byte[] input = pJson.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);

            // Ler resposta
            int status = conn.getResponseCode();

            if (status >= 200 && status < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                return response.toString();
            } else {
                System.err.println("HTTP " + status + " para POST: " + pUrl);
                return null;
            }

        } finally {
            if (os != null) os.close();
            if (reader != null) reader.close();
            if (conn != null) conn.disconnect();
        }
    }//fazerRequisicaoPOST

    private static Map<String, Object> parseJson(String pJson) {
        Map<String, Object> map = new HashMap<>();

        try {
            // Parse simplificado - foca nos campos essenciais
            map.put("ticker", extrairValor(pJson, "ticker"));
            map.put("nomeEmpresa", extrairValor(pJson, "nomeEmpresa"));
            map.put("setor", extrairValor(pJson, "setor"));
            map.put("precoAtual", extrairDouble(pJson, "precoAtual"));
            map.put("beta", extrairDouble(pJson, "beta"));

            // Extrair dados financeiros
            int startFin = pJson.indexOf("\"dadosFinanceiros\":{");
            int endFin = encontrarFimObjeto(pJson, startFin + 20);

            if (startFin > 0 && endFin > startFin) {
                String finJson = pJson.substring(startFin + 20, endFin);
                Map<String, Object> dadosFin = parseJsonObjeto(finJson);
                map.put("dadosFinanceiros", dadosFin);
            }

        } catch (Exception e) {
            System.err.println("Erro ao parsear JSON: " + e.getMessage());
        }

        return map;
    }//parseJson

    private static List<Map<String, Object>> parseJsonArray(String pArrayJson) {
        List<Map<String, Object>> lista = new ArrayList<>();

        try {
            // Parse simplificado de array
            String[] objetos = pArrayJson.split("\\},\\{");

            for (int i = 0; i < objetos.length; i++) {
                String objJson = objetos[i];
                if (i > 0) objJson = "{" + objJson;
                if (i < objetos.length - 1) objJson = objJson + "}";

                Map<String, Object> obj = parseJson(objJson);
                if (!obj.isEmpty()) {
                    lista.add(obj);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao parsear array JSON: " + e.getMessage());
        }

        return lista;
    }//parseJsonArray

    private static Map<String, Object> parseJsonObjeto(String pJson) {
        Map<String, Object> map = new HashMap<>();

        try {
            // Campos numéricos essenciais
            String[] camposNumericos = {
                    "lucrosPorAcaoTTM", "crescimentoLucros5A", "dividaEbitda",
                    "roe", "roic", "margemLiquidaAtual", "fluxoCaixaOperacional",
                    "capex", "compensacaoBaseadaAcoes", "fluxoCaixaLivre",
                    "valorContabilisticoPorAcao", "intangiveis", "goodwill",
                    "ativosTotal", "dividendoPorAcao", "rendimentoDividendo",
                    "racioDistribuicao"
            };

            for (String campo : camposNumericos) {
                double valor = extrairDouble(pJson, campo);
                map.put(campo, valor);
            }

            // Campos de array
            map.put("historicoMargemLiquida", extrairListaDouble(pJson, "historicoMargemLiquida"));
            map.put("historicoAcoesCirculacao", extrairListaLong(pJson, "historicoAcoesCirculacao"));
            map.put("historicoLucros", extrairListaDouble(pJson, "historicoLucros"));

            // Campo ações circulação atual
            long acoes = extrairLong(pJson, "acoesCirculacaoAtual");
            map.put("acoesCirculacaoAtual", acoes);

        } catch (Exception e) {
            System.err.println("Erro ao parsear objeto financeiro: " + e.getMessage());
        }

        return map;
    }//parseJsonObjeto

    // ========== MÉTODOS AUXILIARES DE PARSE ==========

    private static String extrairValor(String json, String chave) {
        try {
            String search = "\"" + chave + "\":\"";
            int start = json.indexOf(search);
            if (start == -1) {
                search = "\"" + chave + "\":";
                start = json.indexOf(search);
                if (start == -1) return "";
                start += search.length();
            } else {
                start += search.length();
            }

            int end = json.indexOf("\"", start);
            if (end == -1) end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            if (end == -1) return "";

            return json.substring(start, end).replace("\"", "").trim();
        } catch (Exception e) {
            return "";
        }
    }//extrairValor

    private static double extrairDouble(String json, String chave) {
        try {
            String valorStr = extrairValor(json, chave);
            if (valorStr.isEmpty()) return 0.0;
            return Double.parseDouble(valorStr);
        } catch (Exception e) {
            return 0.0;
        }
    }//extrairDouble

    private static long extrairLong(String json, String chave) {
        try {
            String valorStr = extrairValor(json, chave);
            if (valorStr.isEmpty()) return 0L;
            return Long.parseLong(valorStr);
        } catch (Exception e) {
            return 0L;
        }
    }//extrairLong

    private static List<Double> extrairListaDouble(String json, String chave) {
        List<Double> lista = new ArrayList<>();
        try {
            String search = "\"" + chave + "\":[";
            int start = json.indexOf(search);
            if (start == -1) return lista;
            start += search.length();

            int end = json.indexOf("]", start);
            if (end == -1) return lista;

            String arrayStr = json.substring(start, end);
            String[] valores = arrayStr.split(",");

            for (String val : valores) {
                try {
                    lista.add(Double.parseDouble(val.trim()));
                } catch (Exception e) {
                    lista.add(0.0);
                }
            }
        } catch (Exception e) {
            // Retorna lista vazia em caso de erro
        }
        return lista;
    }//extrairListaDouble

    private static List<Long> extrairListaLong(String json, String chave) {
        List<Long> lista = new ArrayList<>();
        try {
            String search = "\"" + chave + "\":[";
            int start = json.indexOf(search);
            if (start == -1) return lista;
            start += search.length();

            int end = json.indexOf("]", start);
            if (end == -1) return lista;

            String arrayStr = json.substring(start, end);
            String[] valores = arrayStr.split(",");

            for (String val : valores) {
                try {
                    lista.add(Long.parseLong(val.trim()));
                } catch (Exception e) {
                    lista.add(0L);
                }
            }
        } catch (Exception e) {
            // Retorna lista vazia em caso de erro
        }
        return lista;
    }//extrairListaLong

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
    }//encontrarFimObjeto

    // ========== MOCK PARA FALLBACK ==========

    private static Map<String, Object> criarDadosMock(String pTicker) {
        // Dados de fallback se API falhar
        Random rand = new Random(pTicker.hashCode());

        Map<String, Object> dados = new HashMap<>();
        dados.put("ticker", pTicker);
        dados.put("nomeEmpresa", "Empresa " + pTicker);
        dados.put("setor", "Technology");
        dados.put("precoAtual", 50.0 + rand.nextDouble() * 200.0);
        dados.put("beta", 0.8 + rand.nextDouble() * 1.2);

        Map<String, Object> fin = new HashMap<>();
        fin.put("lucrosPorAcaoTTM", 1.0 + rand.nextDouble() * 10.0);
        fin.put("crescimentoLucros5A", 0.05 + rand.nextDouble() * 0.15);
        fin.put("dividaEbitda", 0.5 + rand.nextDouble() * 3.0);
        fin.put("roe", 0.1 + rand.nextDouble() * 0.2);
        fin.put("roic", 0.08 + rand.nextDouble() * 0.15);
        fin.put("margemLiquidaAtual", 0.1 + rand.nextDouble() * 0.25);
        fin.put("historicoMargemLiquida", Arrays.asList(0.1, 0.12, 0.15));
        fin.put("acoesCirculacaoAtual", 1000000000L);
        fin.put("historicoAcoesCirculacao", Arrays.asList(1000000000L, 990000000L, 980000000L));
        fin.put("historicoLucros", Arrays.asList(80.0, 85.0, 90.0, 95.0, 100.0));
        fin.put("fluxoCaixaOperacional", 15000.0);
        fin.put("capex", -2000.0);
        fin.put("compensacaoBaseadaAcoes", 500.0);
        fin.put("fluxoCaixaLivre", 13000.0);
        fin.put("valorContabilisticoPorAcao", 20.0);
        fin.put("intangiveis", 1000.0);
        fin.put("goodwill", 2000.0);
        fin.put("ativosTotal", 50000.0);
        fin.put("dividendoPorAcao", 1.0);
        fin.put("rendimentoDividendo", 0.02);
        fin.put("racioDistribuicao", 0.3);

        dados.put("dadosFinanceiros", fin);
        return dados;
    }//criarDadosMock

}//classe ClienteApi