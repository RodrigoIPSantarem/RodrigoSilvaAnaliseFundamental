# servico_tesouro.py
import requests
from datetime import datetime


class ServicoTesouro:
    """
    Serviço para obter a taxa do Tesouro dos EUA a 10 anos.
    Usa API pública do FRED (Federal Reserve Economic Data).
    """

    @staticmethod
    def obter_taxa_tesouro_10anos() -> float:
        """
        Retorna a yield atual do US 10Y Treasury.
        Fallback: 4.3% se API falhar.
        """
        try:
            # Método 1: Yahoo Finance (^TNX)
            try:
                import yfinance as yf
                ticker = yf.Ticker("^TNX")
                taxa = ticker.history(period="1d")['Close'].iloc[-1]
                return float(taxa) / 100.0
            except:
                pass

            # Método 2: API pública alternativa
            url = "https://www.treasury.gov/resource-center/data-chart-center/interest-rates/pages/XmlView.aspx?data=yield"
            response = requests.get(url, timeout=5)

            if response.status_code == 200:
                # Parse simples do XML
                import xml.etree.ElementTree as ET
                root = ET.fromstring(response.content)

                # Buscar yield de 10 anos
                for elem in root.findall(".//{*}entry"):
                    if elem.find("{*}NEW_DATE") is not None:
                        bc_10year = elem.find("{*}BC_10YEAR")
                        if bc_10year is not None and bc_10year.text:
                            return float(bc_10year.text) / 100.0

            # Método 3: API alternativa
            url_alt = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v2/accounting/od/avg_interest_rates"
            params = {
                "filter": "record_date:gte:2024-01-01",
                "sort": "-record_date",
                "page[size]": 1
            }
            response = requests.get(url_alt, params=params, timeout=5)

            if response.status_code == 200:
                data = response.json()
                if data.get('data') and len(data['data']) > 0:
                    taxa = data['data'][0].get('avg_interest_rate_amt', 4.3)
                    return float(taxa) / 100.0

        except Exception as e:
            print(f"⚠️  Erro ao buscar taxa tesouro: {e}")

        # Fallback seguro
        return 0.043  # 4.3%

    @staticmethod
    def obter_todas_taxas() -> dict:
        """
        Retorna várias taxas de juros relevantes.
        """
        try:
            import yfinance as yf

            taxas = {
                "data_hora": datetime.now().isoformat(),
                "taxas": {}
            }

            # Taxas do Tesouro dos EUA
            tenores = {
                "1_mes": "^IRX",  # 13-week
                "3_meses": "^IRX",  # 13-week
                "6_meses": "^FVX",  # 5-year (aproximação)
                "1_ano": "^FVX",  # 5-year (aproximação)
                "2_anos": "^FVX",  # 5-year
                "5_anos": "^FVX",  # 5-year
                "10_anos": "^TNX",  # 10-year
                "30_anos": "^TYX"  # 30-year
            }

            for nome, ticker in tenores.items():
                try:
                    t = yf.Ticker(ticker)
                    historico = t.history(period="1d")
                    if not historico.empty:
                        valor = historico['Close'].iloc[-1]
                        taxas["taxas"][nome] = float(valor) / 100.0
                except:
                    taxas["taxas"][nome] = None

            # Adicionar taxa principal de 10 anos se não conseguiu
            if "10_anos" not in taxas["taxas"] or taxas["taxas"]["10_anos"] is None:
                taxas["taxas"]["10_anos"] = ServicoTesouro.obter_taxa_tesouro_10anos()

            return taxas

        except Exception as e:
            print(f"Erro ao buscar todas taxas: {e}")
            return {
                "data_hora": datetime.now().isoformat(),
                "taxas": {"10_anos": 0.043}
            }