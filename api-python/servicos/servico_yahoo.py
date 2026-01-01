# servico_yahoo.py
import yfinance as yf
import pandas as pd
import numpy as np
from datetime import datetime
import requests_cache

# Otimização: Cache das sessões para evitar spam ao Yahoo (expira em 1 hora)
session = requests_cache.CachedSession('yfinance.cache')
session.headers['User-agent'] = 'RodrigoSilvaAnalise/1.0'

class ServicoYahoo:
    """
    Serviço responsável por comunicar com a API do Yahoo Finance,
    extrair dados financeiros e formatá-los para o cliente Java.
    """

    @staticmethod
    def obter_taxa_livre_risco() -> float:
        """
        Obtém a yield atual do Tesouro dos EUA a 10 anos (^TNX).
        Usado para o cálculo do WACC Dinâmico no Java.
        """
        try:
            ticker = yf.Ticker("^TNX")
            # O Yahoo retorna o valor em percentagem (ex: 4.3), dividimos por 100
            taxa = ticker.history(period="1d")['Close'].iloc[-1]
            return float(taxa) / 100.0
        except Exception as e:
            print(f"Erro ao buscar Yield Tesouro: {e}")
            return 0.045  # Fallback seguro: 4.5%

    @staticmethod
    def obter_dados_acao(ticker_simbolo: str) -> dict:
        """
        Busca todos os dados fundamentais necessários para o Protocolo.

        Argumentos:
            ticker_simbolo: O símbolo da ação (ex: AAPL, KO)

        Retorna:
            Dicionário com a estrutura JSON completa em Português.
        """
        try:
            acao = yf.Ticker(ticker_simbolo)
            info = acao.info

            # Se não tiver preço atual, a ação provavelmente é inválida
            preco_atual = info.get('currentPrice') or info.get('regularMarketPreviousClose')
            if not preco_atual:
                raise ValueError(f"Preço não encontrado para {ticker_simbolo}")

            # Carregar Demonstrações Financeiras
            demonstracao_resultados = acao.financials
            balanco = acao.balance_sheet
            fluxo_caixa = acao.cashflow

            # --- Funções Auxiliares de Extração Segura ---
            def obter_seguro(df, chaves, indice_linha=0, padrao=0.0):
                """Tenta obter valor do DataFrame usando múltiplas chaves possíveis"""
                if df is None or df.empty: return padrao
                for chave in chaves:
                    if chave in df.index:
                        try:
                            val = df.loc[chave].iloc[indice_linha]
                            return 0.0 if pd.isna(val) else float(val)
                        except:
                            continue
                return padrao

            def obter_serie(df, chaves, anos=3):
                """Retorna lista de valores históricos (mais recente primeiro)"""
                if df is None or df.empty: return [0.0] * anos
                for chave in chaves:
                    if chave in df.index:
                        serie = df.loc[chave].head(anos)
                        return [0.0 if pd.isna(x) else float(x) for x in serie.tolist()]
                return [0.0] * anos

            # --- Extração de Dados Críticos para o Protocolo ---

            # 1. Kill Switch: Diluição (Ações em circulação)
            historico_acoes = obter_serie(balanco, ['Ordinary Shares Number', 'Share Issued'], 3)
            acoes_atuais = historico_acoes[0] if historico_acoes and historico_acoes[0] > 0 else info.get(
                'sharesOutstanding', 0)

            # 2. Kill Switch: Margens em Queda (Margem Líquida Histórica)
            historico_lucro_liquido = obter_serie(demonstracao_resultados,
                                                  ['Net Income', 'Net Income Common Stockholders'], 3)
            historico_receita = obter_serie(demonstracao_resultados, ['Total Revenue', 'Operating Revenue'], 3)

            historico_margem = []
            for i in range(len(historico_lucro_liquido)):
                try:
                    rev = historico_receita[i]
                    luc = historico_lucro_liquido[i]
                    if rev != 0:
                        historico_margem.append(luc / rev)
                    else:
                        historico_margem.append(0.0)
                except IndexError:
                    historico_margem.append(0.0)

            # 3. Ajuste Setorial TECNOLOGIA: Stock Based Compensation (SBC)
            sbc = obter_seguro(fluxo_caixa, ['Stock Based Compensation', 'Share Based Compensation'], 0)

            # 4. Ajuste Setorial BANCA: Intangíveis e Goodwill
            goodwill = obter_seguro(balanco, ['Goodwill'], 0)
            intangiveis = obter_seguro(balanco, ['Intangible Assets', 'Other Intangible Assets'], 0)
            capital_proprio = obter_seguro(balanco, ['Stockholders Equity', 'Total Equity Gross Minority Interest'], 0)

            # 5. Dados para Avaliação (Graham, Gordon, DCF)
            eps_ttm = info.get('trailingEps', 0.0)

            # Dívida e EBITDA
            divida_total = obter_seguro(balanco, ['Total Debt'], 0)
            ebitda = obter_seguro(demonstracao_resultados, ['EBITDA', 'Normalized EBITDA'], 0)
            if ebitda == 0: ebitda = info.get('ebitda', 1.0)

            divida_ebitda = 0.0
            if ebitda != 0:
                divida_ebitda = divida_total / ebitda
            elif 'debtToEquity' in info:
                divida_ebitda = (float(info['debtToEquity']) / 100.0) * 2  # Estimativa

            # Fluxos de Caixa
            fco = obter_seguro(fluxo_caixa, ['Operating Cash Flow', 'Total Cash From Operating Activities'], 0)
            capex = obter_seguro(fluxo_caixa, ['Capital Expenditure', 'Capital Expenditures'], 0)

            # Lucro Histórico (5 anos para Kill Switch)
            historico_lucros_5a = obter_serie(demonstracao_resultados, ['Net Income', 'Net Income Common Stockholders'],
                                              5)

            # --- Construção do JSON Final (Mapeamento para Português) ---
            return {
                "ticker": ticker_simbolo.upper(),
                "nomeEmpresa": info.get('shortName', 'Desconhecido'),
                "setor": info.get('sector', 'Desconhecido'),
                "industria": info.get('industry', 'Desconhecido'),
                "precoAtual": float(preco_atual),
                "beta": float(info.get('beta', 1.0) or 1.0),
                "capitalizacaoMercado": int(info.get('marketCap', 0)),

                "dadosFinanceiros": {
                    "lucrosPorAcaoTTM": float(eps_ttm or 0.0),
                    "lucrosPorAcaoFuturo": float(info.get('forwardEps', 0.0) or 0.0),

                    # Crescimentos (se falhar, usa 5% conservador)
                    "crescimentoReceita5A": float(info.get('revenueGrowth', 0.05) or 0.05),
                    "crescimentoLucros5A": float(info.get('earningsGrowth', 0.05) or 0.05),

                    "dividaEbitda": float(divida_ebitda),
                    "dividaCapitalProprio": float(info.get('debtToEquity', 0.0) or 0.0) / 100.0,

                    "roe": float(info.get('returnOnEquity', 0.0) or 0.0),
                    "roic": 0.0,  # Yahoo free tier raramente tem ROIC direto

                    "margemLiquidaAtual": float(info.get('profitMargins', 0.0) or 0.0),
                    "historicoMargemLiquida": historico_margem,

                    "acoesCirculacaoAtual": int(acoes_atuais),
                    "historicoAcoesCirculacao": [int(x) for x in historico_acoes],

                    "historicoLucros": historico_lucros_5a,

                    "fluxoCaixaOperacional": float(fco),
                    "capex": float(capex),  # Yahoo costuma mandar negativo
                    "fluxoCaixaLivre": float(info.get('freeCashflow', 0.0) or (fco + capex)),
                    "compensacaoBaseadaAcoes": float(sbc),  # CRÍTICO para AcaoTecnologia

                    "valorContabilisticoPorAcao": float(info.get('bookValue', 0.0) or 0.0),
                    "capitalProprio": float(capital_proprio),
                    "intangiveis": float(intangiveis),  # CRÍTICO para AcaoBanco
                    "goodwill": float(goodwill),  # CRÍTICO para AcaoBanco
                    "ativosTotal": float(obter_seguro(balanco, ['Total Assets'], 0)),

                    "dividendoPorAcao": float(info.get('dividendRate', 0.0) or 0.0),
                    "rendimentoDividendo": float(info.get('dividendYield', 0.0) or 0.0),
                    "racioDistribuicao": float(info.get('payoutRatio', 0.0) or 0.0),

                    "racioPrecoLucroTTM": float(info.get('trailingPE', 0.0) or 0.0),
                    "racioPrecoLucroMedia5A": 0.0
                },

                "dadosMercado": {
                    # O Java pode pedir isto separado, mas enviamos já
                    "tesouroEUA10Anos": 0.0,
                    "maximo52Semanas": float(info.get('fiftyTwoWeekHigh', 0.0) or 0.0),
                    "minimo52Semanas": float(info.get('fiftyTwoWeekLow', 0.0) or 0.0)
                },

                "metadados": {
                    "fonteDados": "Yahoo Finance (yfinance)",
                    "ultimaAtualizacao": datetime.now().isoformat()
                }
            }

        except Exception as e:
            print(f"Erro a processar {ticker_simbolo}: {str(e)}")
            return {"erro": str(e), "ticker": ticker_simbolo}