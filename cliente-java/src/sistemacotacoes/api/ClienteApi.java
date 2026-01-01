package sistemacotacoes.api;

import sistemacotacoes.modelo.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ClienteApi {

    private static final String URL_BASE = "http://localhost:5000/cotacao?ticker=";

    public Ativo buscarAtivo(String ticker, String tipoAtivo) {
        try {
            // 1. Fazer o pedido à API Python
            String json = fazerRequisicao(ticker);

            if (json == null) {
                System.out.println("❌ Erro: Não foi possível conectar à API.");
                return new Acao(ticker, "Desconhecido", 0, 0, 0); // Retorna vazio para não crashar
            }

            // 2. Extrair dados do JSON (Parse manual simples)
            String nome = extrairValor(json, "nome");
            double preco = extrairDouble(json, "preco");
            double variacao = extrairDouble(json, "variacao");
            long volume = extrairLong(json, "volume");

            // 3. Factory: Criar o objeto certo baseado no tipo pedido no Principal
            switch (tipoAtivo.toUpperCase()) {
                case "CRIPTO":
                    return new Cripto(ticker, nome, preco, variacao, volume);
                case "ETF":
                    return new ETF(ticker, nome, preco, variacao, volume);
                default:
                    return new Acao(ticker, nome, preco, variacao, volume);
            }

        } catch (Exception e) {
            System.out.println("Erro ao buscar ativo: " + e.getMessage());
            return new Acao(ticker, "Erro", 0, 0, 0);
        }
    }

    // --- Métodos Auxiliares (HTTP e Parse) ---

    private String fazerRequisicao(String ticker) throws Exception {
        URL url = new URL(URL_BASE + ticker);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) return null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder resposta = new StringBuilder();
        String linha;
        while ((linha = reader.readLine()) != null) resposta.append(linha);
        reader.close();

        return resposta.toString();
    }

    // Extrai texto do JSON (ex: "nome": "Apple")
    private String extrairValor(String json, String chave) {
        String busca = "\"" + chave + "\":";
        int inicio = json.indexOf(busca);
        if (inicio == -1) return "N/A";

        inicio += busca.length();
        if (json.charAt(inicio) == '"') inicio++; // Pula aspas se for string

        int fim = json.indexOf(",", inicio);
        if (fim == -1) fim = json.indexOf("}", inicio);

        String valor = json.substring(inicio, fim).replace("\"", "").trim();
        return valor;
    }

    // Extrai número double
    private double extrairDouble(String json, String chave) {
        try {
            return Double.parseDouble(extrairValor(json, chave));
        } catch (Exception e) { return 0.0; }
    }

    // Extrai número long (inteiro grande)
    private long extrairLong(String json, String chave) {
        try {
            // Remove casas decimais se houver (ex: volume: 1000.0)
            String val = extrairValor(json, chave);
            if (val.contains(".")) val = val.substring(0, val.indexOf("."));
            return Long.parseLong(val);
        } catch (Exception e) { return 0L; }
    }
}