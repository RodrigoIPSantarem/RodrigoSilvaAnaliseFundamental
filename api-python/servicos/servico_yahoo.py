# servico_yahoo.py
import yfinance as yf
import pandas as pd
import numpy as np
import requests_cache

session = requests_cache.CachedSession('yfinance.cache')
session.headers['User-agent'] = 'RodrigoSilvaAnalise/1.0'


class ServicoYahoo:
    @staticmethod
    def obter_dados_acao(ticker_simbolo: str) -> dict:
        try:
            acao = yf.Ticker(ticker_simbolo)
            info = acao.info

            # Validação básica de preço
            preco = info.get('currentPrice') or info.get('regularMarketPreviousClose')
            if not preco:
                raise ValueError(f"Preço não encontrado para {ticker_simbolo}")

            # Extração segura
            balanco = acao.balance_sheet
            demos = acao.financials
            fluxo = acao.cashflow

            def safe_get(df, keys, idx=0):
                if df is None or df.empty: return 0.0
                for k in keys:
                    if k in df.index:
                        try:
                            return float(df.loc[k].iloc[idx])
                        except:
                            pass
                return 0.0

            def safe_series(df, keys, n=3):
                if df is None or df.empty: return [0.0] * n
                for k in keys:
                    if k in df.index:
                        return [float(x) for x in df.loc[k].head(n).tolist()]
                return [0.0] * n

            # Dados críticos
            fco = safe_get(fluxo, ['Operating Cash Flow', 'Total Cash From Operating Activities'])
            capex = safe_get(fluxo, ['Capital Expenditure', 'Capital Expenditures'])
            sbc = safe_get(fluxo, ['Stock Based Compensation'])

            return {
                "ticker": ticker_simbolo.upper(),
                "nomeEmpresa": info.get('shortName', 'N/A'),
                "setor": info.get('sector', 'N/A'),
                "precoAtual": float(preco),
                "beta": float(info.get('beta', 1.0) or 1.0),
                "dadosFinanceiros": {
                    "lucrosPorAcaoTTM": float(info.get('trailingEps', 0.0) or 0.0),
                    "lucrosPorAcaoFuturo": float(info.get('forwardEps', 0.0) or 0.0),
                    "crescimentoReceita5A": float(info.get('revenueGrowth', 0.05) or 0.05),
                    "crescimentoLucros5A": float(info.get('earningsGrowth', 0.05) or 0.05),
                    "dividaEbitda": 0.0,  # Simplificação para brevidade, yfinance nem sempre tem
                    "roe": float(info.get('returnOnEquity', 0.0) or 0.0),
                    "roic": 0.0,
                    "margemLiquidaAtual": float(info.get('profitMargins', 0.0) or 0.0),
                    "historicoMargemLiquida": [],
                    "acoesCirculacaoAtual": int(info.get('sharesOutstanding', 0)),
                    "historicoAcoesCirculacao": [],
                    "historicoLucros": [],
                    "fluxoCaixaOperacional": fco,
                    "capex": capex,
                    "fluxoCaixaLivre": float(info.get('freeCashflow', 0.0) or (fco + capex)),
                    "compensacaoBaseadaAcoes": sbc,
                    "valorContabilisticoPorAcao": float(info.get('bookValue', 0.0) or 0.0),
                    "intangiveis": safe_get(balanco, ['Intangible Assets']),
                    "goodwill": safe_get(balanco, ['Goodwill']),
                    "ativosTotal": safe_get(balanco, ['Total Assets']),
                    "dividendoPorAcao": float(info.get('dividendRate', 0.0) or 0.0),
                    "rendimentoDividendo": float(info.get('dividendYield', 0.0) or 0.0),
                    "racioDistribuicao": float(info.get('payoutRatio', 0.0) or 0.0)
                }
            }
        except Exception as e:
            return {"erro": str(e)}