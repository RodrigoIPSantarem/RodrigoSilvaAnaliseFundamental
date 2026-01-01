from flask import Flask, jsonify, request
import yfinance as yf

app = Flask(__name__)
app.config['JSONIFY_PRETTYPRINT_REGULAR'] = False


@app.route('/cotacao', methods=['GET'])
def cotacao():
    # Exemplo: /cotacao?ticker=AAPL ou BTC-USD
    ticker = request.args.get('ticker', '').upper()
    if not ticker: return jsonify({"erro": "Falta ticker"}), 400

    try:
        dados = yf.Ticker(ticker).info

        # Preço atual (tenta várias chaves do Yahoo)
        preco = dados.get('currentPrice') or dados.get('regularMarketPrice') or 0.0

        # Variação Percentual (tenta calcular se não vier pronta)
        anterior = dados.get('regularMarketPreviousClose', preco)
        variacao = ((preco - anterior) / anterior * 100) if anterior else 0.0

        return jsonify({
            "ticker": ticker,
            "nome": dados.get('shortName', ticker),
            "preco": float(preco),
            "variacao": float(variacao),
            "volume": int(dados.get('volume', 0))
        })
    except Exception as e:
        return jsonify({"erro": str(e)}), 500


if __name__ == '__main__':
    app.run(port=5000)