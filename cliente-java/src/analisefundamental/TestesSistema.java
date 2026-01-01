package analisefundamental;

import analisefundamental.modelo.*;
import analisefundamental.estrategia.*;
import analisefundamental.enums.Setor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestesSistema {

    public static void main(String[] args) {
        System.out.println(">>> A INICIAR TESTES DE VALIDAÇÃO <<<");

        testarCalculoGraham();
        testarKillSwitchDiluicao();
        testarAjusteTech();
    }//main

    private static void testarCalculoGraham() {
        System.out.print("Teste 1: Fórmula de Graham (AcaoGeral)... ");

        // Setup: EPS = 10, g = 5%, RiskFree = 4.3%
        // Graham = (10 * (8.5 + 2*5) * 4.4) / 4.3 * 0.7
        // Graham = (10 * 18.5 * 4.4) / 4.3 * 0.7 = 814 / 4.3 * 0.7 = 189.30 * 0.7 = ~132.51

        DadosFinanceiros dados = criarDadosDummy();
        Acao acao = new AcaoGeral("TEST", "Teste", 100.0, 1.0, Setor.GERAL, dados);
        acao.definirEstrategia(new EstrategiaGraham());

        double pj = acao.calcularPrecoJusto(0.043);

        if (Math.abs(pj - 132.51) < 1.0) {
            System.out.println("[PASSOU] PJ: " + String.format("%.2f", pj));
        } else {
            System.out.println("[FALHOU] Esperado ~132.51, obtido " + pj);
        }
    }//testarCalculoGraham

    private static void testarKillSwitchDiluicao() {
        System.out.print("Teste 2: Kill Switch Diluição... ");

        // Criar histórico com diluição massiva (1000 -> 1500 ações)
        List<Long> acoesDiluidas = Arrays.asList(1500L, 1200L, 1000L);

        // Construtor alinhado com DadosFinanceiros.java (20 parâmetros)
        DadosFinanceiros dados = new DadosFinanceiros(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, // Lucros, Rentabilidade
                1500, acoesDiluidas, null,          // Ações (Diluição Aqui!)
                0.0, 0.0, 0.0,                      // Fluxos
                0.0, 0.0, 0.0, 0.0,                 // Balanço
                0.0, 0.0, 0.0                       // Dividendos
        );

        Acao acao = new AcaoGeral("DIL", "Diluidora", 10, 1, Setor.GERAL, dados);

        if (!acao.verificarKillSwitchesUniversais().isEmpty()) {
            System.out.println("[PASSOU] Detetou diluição.");
        } else {
            System.out.println("[FALHOU] Não detetou diluição.");
        }
    }//testarKillSwitchDiluicao

    private static void testarAjusteTech() {
        System.out.print("Teste 3: Ajuste Tech (FCF - SBC)... ");
        // FCF = 100, SBC = 20 -> FCF Ajustado deve ser 80

        // Construtor alinhado com DadosFinanceiros.java (20 parâmetros)
        DadosFinanceiros dados = new DadosFinanceiros(
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, // Lucros
                0, null, null,                      // Ações
                110.0, -10.0, 20.0,                 // FCO=110, Capex=-10, SBC=20
                0.0, 0.0, 0.0, 0.0,                 // Balanço
                0.0, 0.0, 0.0                       // Dividendos
        );

        AcaoTecnologia tech = new AcaoTecnologia("TECH", "Tech", 10, 1, dados);
        double metrica = tech.obterMetricaAvaliacao(); // Usa calcularFCFAjustado internamente

        if (metrica == 80.0) {
            System.out.println("[PASSOU] FCF Ajustado: " + metrica);
        } else {
            System.out.println("[FALHOU] Esperado 80.0, obtido " + metrica);
        }
    }//testarAjusteTech

    // Helper para criar dados limpos
    private static DadosFinanceiros criarDadosDummy() {
        // Construtor Padrão para testes simples (Graham)
        return new DadosFinanceiros(
                10.0, 0.05, 0.0, 0.0, 0.0, 0.0, null, // EPS=10, g=5%
                1000, Arrays.asList(1000L), null,
                0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0
        );
    }
}//classe TestesSistema