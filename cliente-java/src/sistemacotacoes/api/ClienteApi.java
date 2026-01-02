// ClienteApi.java
package sistemacotacoes.api;

import sistemacotacoes.modelo.*;
import sistemacotacoes.enums.TipoAtivo;
import sistemacotacoes.fabrica.FabricaAtivos;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Cliente HTTP para comunicação com a API Python (Flask).
 * 
 * Demonstra: CONSUMO DE API REST + USO DE FACTORY
 */
public class ClienteApi {

    private static final String URL_BASE = "http://localhost:5000/cotacao?ticker=";
    private static final int TIMEOUT_MS = 5000;

    //--------------------------------------------------
    // Buscar Ativo (com tipo explícito)
    //--------------------------------------------------
    public Ativo buscarAtivo(String pTicker, TipoAtivo pTipo) {
        try {
            String json = fazerRequisicao(pTicker);

            if (json == null || json.contains("erro")) {
                System.out.println("❌ Erro ao buscar " + pTicker);
                return null;
            }//if

            // Extrair dados do JSON
            String nome = extrairValor(json, "nome");
            double preco = extrairDouble(json, "preco");
            double variacao = extrairDouble(json, "variacao");
            long volume = extrairLong(json, "volume");

            // Usar Factory para criar o objeto correto
            return FabricaAtivos.criarAtivo(
                pTipo, pTicker, nome, preco, variacao, volume
            );

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            return null;
        }//catch
    }//buscarAtivo

    //--------------------------------------------------
    // Buscar Ativo (com detecção automática)
    //--------------------------------------------------
    public Ativo buscarAtivoAuto(String pTicker) {
        try {
            String json = fazerRequisicao(pTicker);

            if (json == null || json.contains("erro")) {
                System.out.println("❌ Erro ao buscar " + pTicker);
                return null;
            }//if

            // Extrair dados do JSON
            String nome = extrairValor(json, "nome");
            double preco = extrairDouble(json, "preco");
            double variacao = extrairDouble(json, "variacao");
            long volume = extrairLong(json, "volume");

            // Factory com detecção automática
            return FabricaAtivos.criarAtivoAuto(
                pTicker, nome, preco, variacao, volume
            );

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            return null;
        }//catch
    }//buscarAtivoAuto

    //--------------------------------------------------
    // Verificar se API está disponível
    //--------------------------------------------------
    public boolean apiDisponivel() {
        try {
            URL url = new URL("http://localhost:5000/saude");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setRequestMethod("GET");
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }//catch
    }//apiDisponivel

    //--------------------------------------------------
    // Métodos Auxiliares (HTTP e Parse JSON)
    //--------------------------------------------------

    private String fazerRequisicao(String pTicker) throws Exception {
        URL url = new URL(URL_BASE + pTicker.toUpperCase());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) return null;

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        StringBuilder resposta = new StringBuilder();
        String linha;
        while ((linha = reader.readLine()) != null) {
            resposta.append(linha);
        }//while
        reader.close();

        return resposta.toString();
    }//fazerRequisicao

    private String extrairValor(String pJson, String pChave) {
        String busca = "\"" + pChave + "\":";
        int inicio = pJson.indexOf(busca);
        if (inicio == -1) return "N/A";

        inicio += busca.length();
        if (pJson.charAt(inicio) == '"') inicio++;

        int fim = pJson.indexOf(",", inicio);
        if (fim == -1) fim = pJson.indexOf("}", inicio);

        return pJson.substring(inicio, fim).replace("\"", "").trim();
    }//extrairValor

    private double extrairDouble(String pJson, String pChave) {
        try {
            return Double.parseDouble(extrairValor(pJson, pChave));
        } catch (Exception e) { 
            return 0.0; 
        }//catch
    }//extrairDouble

    private long extrairLong(String pJson, String pChave) {
        try {
            String val = extrairValor(pJson, pChave);
            if (val.contains(".")) val = val.substring(0, val.indexOf("."));
            return Long.parseLong(val);
        } catch (Exception e) { 
            return 0L; 
        }//catch
    }//extrairLong

}//classe ClienteApi
