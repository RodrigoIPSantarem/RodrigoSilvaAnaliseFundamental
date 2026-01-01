# app.py - VERSÃO FINAL
from flask import Flask, request, jsonify
from servicos.servico_yahoo import ServicoYahoo
from servicos.servico_tesouro import ServicoTesouro
from flask_cors import CORS
import traceback

app = Flask(__name__)
CORS(app)  # Permitir requests do Java

# Configurações
app.config['JSONIFY_PRETTYPRINT_REGULAR'] = True
app.config['JSON_SORT_KEYS'] = False


@app.route('/api/estado', methods=['GET'])
def estado():
    """Endpoint de verificação de saúde"""
    return jsonify({
        "estado": "online",
        "servico": "Rodrigo Silva Analise Fundamental API",
        "versao": "3.1",
        "endpoints": {
            "/api/acao/<ticker>": "Dados de uma ação",
            "/api/acoes": "Múltiplas ações (tickers=AAPL,MSFT,...)",
            "/api/tesouro/10anos": "Taxa US 10Y",
            "/api/tesouro/todas": "Todas as taxas",
            "/api/batch": "Processamento em lote"
        }
    })


@app.route('/api/tesouro/10anos', methods=['GET'])
def obter_taxa_tesouro():
    """Taxa livre de risco (US 10Y Treasury)"""
    taxa = ServicoTesouro.obter_taxa_tesouro_10anos()
    return jsonify({
        "sucesso": True,
        "taxa_tesouro_10anos": taxa,
        "formatado": f"{taxa * 100:.2f}%",
        "fonte": "Yahoo Finance/FRED"
    })


@app.route('/api/tesouro/todas', methods=['GET'])
def obter_todas_taxas():
    """Todas as taxas de juros"""
    taxas = ServicoTesouro.obter_todas_taxas()
    return jsonify(taxas)


@app.route('/api/acao/<ticker>', methods=['GET'])
def obter_acao_individual(ticker):
    """Dados de uma única ação"""
    try:
        if not ticker or len(ticker.strip()) == 0:
            return jsonify({"erro": "Ticker não fornecido"}), 400

        ticker = ticker.upper().strip()
        print(f"🔍 Buscando dados para: {ticker}")

        dados = ServicoYahoo.obter_dados_acao(ticker)

        if "erro" in dados:
            return jsonify(dados), 404

        # Adicionar taxa do tesouro
        taxa_tesouro = ServicoTesouro.obter_taxa_tesouro_10anos()
        if "dadosMercado" in dados:
            dados["dadosMercado"]["tesouroEUA10Anos"] = taxa_tesouro

        return jsonify(dados)

    except Exception as e:
        print(f"❌ Erro no endpoint /acao/{ticker}: {str(e)}")
        traceback.print_exc()
        return jsonify({
            "erro": str(e),
            "ticker": ticker,
            "sucesso": False
        }), 500


@app.route('/api/acoes', methods=['GET'])
def obter_multiplas_acoes():
    """
    Dados para múltiplas ações.
    Uso: /api/acoes?tickers=AAPL,MSFT,KO
    """
    try:
        tickers_param = request.args.get('tickers', '')
        if not tickers_param:
            return jsonify({"erro": "Parâmetro 'tickers' obrigatório"}), 400

        lista_tickers = [t.strip().upper() for t in tickers_param.split(',') if t.strip()]
        if not lista_tickers:
            return jsonify({"erro": "Nenhum ticker válido fornecido"}), 400

        print(f"🔍 Buscando {len(lista_tickers)} ações: {lista_tickers}")

        # Buscar taxa do tesouro uma vez
        taxa_tesouro = ServicoTesouro.obter_taxa_tesouro_10anos()

        resultados = []
        for t in lista_tickers:
            try:
                dados_acao = ServicoYahoo.obter_dados_acao(t)

                if "erro" not in dados_acao:
                    # Adicionar taxa do tesouro
                    if "dadosMercado" in dados_acao:
                        dados_acao["dadosMercado"]["tesouroEUA10Anos"] = taxa_tesouro
                    resultados.append(dados_acao)
                else:
                    resultados.append({
                        "ticker": t,
                        "erro": dados_acao["erro"],
                        "sucesso": False
                    })

            except Exception as e:
                resultados.append({
                    "ticker": t,
                    "erro": str(e),
                    "sucesso": False
                })

        return jsonify({
            "sucesso": True,
            "total": len(lista_tickers),
            "processadas": len([r for r in resultados if "erro" not in r]),
            "taxa_tesouro_10anos": taxa_tesouro,
            "acoes": resultados
        })

    except Exception as e:
        print(f"❌ Erro no endpoint /acoes: {str(e)}")
        return jsonify({
            "erro": str(e),
            "sucesso": False
        }), 500


@app.route('/api/batch', methods=['POST'])
def processar_lote():
    """
    Processamento em lote via POST.
    Recebe JSON: {"tickers": ["AAPL", "MSFT", "KO"]}
    """
    try:
        dados = request.get_json()
        if not dados or "tickers" not in dados:
            return jsonify({"erro": "JSON com campo 'tickers' obrigatório"}), 400

        tickers = [t.strip().upper() for t in dados["tickers"] if t and str(t).strip()]

        if not tickers:
            return jsonify({"erro": "Nenhum ticker válido"}), 400

        print(f"🔍 Processando lote com {len(tickers)} ações")

        # Taxa do tesouro
        taxa_tesouro = ServicoTesouro.obter_taxa_tesouro_10anos()

        # Processar (limitar a 10 para performance)
        tickers = tickers[:10]
        resultados = []

        for ticker in tickers:
            try:
                dados_acao = ServicoYahoo.obter_dados_acao(ticker)
                if "erro" not in dados_acao:
                    dados_acao["dadosMercado"]["tesouroEUA10Anos"] = taxa_tesouro
                    dados_acao["sucesso"] = True
                resultados.append(dados_acao)
            except Exception as e:
                resultados.append({
                    "ticker": ticker,
                    "erro": str(e),
                    "sucesso": False
                })

        return jsonify({
            "sucesso": True,
            "total": len(tickers),
            "taxa_tesouro_10anos": taxa_tesouro,
            "resultados": resultados
        })

    except Exception as e:
        return jsonify({
            "erro": str(e),
            "sucesso": False
        }), 500


if __name__ == '__main__':
    print("=" * 60)
    print("🚀 API Rodrigo Silva Analise Fundamental iniciada!")
    print("📌 Endpoints disponíveis:")
    print("   http://localhost:5000/api/estado")
    print("   http://localhost:5000/api/acao/AAPL")
    print("   http://localhost:5000/api/tesouro/10anos")
    print("=" * 60)
    app.run(host='0.0.0.0', port=5000, debug=False)