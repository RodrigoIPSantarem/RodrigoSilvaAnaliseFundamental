package sistemacotacoes;

import sistemacotacoes.api.ClienteApi;
import sistemacotacoes.gestao.Carteira;
import java.util.Scanner;

public class Principal {
    public static void main(String[] args) {
        Carteira carteira = new Carteira();
        ClienteApi api = new ClienteApi();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== SISTEMA DE COTAÇÕES ===");
            System.out.println("1. Adicionar Ação (ex: AAPL)");
            System.out.println("2. Adicionar Cripto (ex: BTC-USD)");
            System.out.println("3. Adicionar ETF (ex: IVV)");
            System.out.println("4. Ver Carteira");
            System.out.println("5. Ordenar por Preço");
            System.out.println("6. Sair");
            System.out.print("Escolha: ");

            String op = scanner.nextLine();

            if (op.equals("6")) break;

            if (op.equals("4")) { carteira.listar(); continue; }
            if (op.equals("5")) { carteira.ordenarPorPreco(); carteira.listar(); continue; }

            System.out.print("Digite o Ticker: ");
            String ticker = scanner.nextLine();

            // Aqui decide-se qual classe instanciar (Polimorfismo na criação)
            if (op.equals("1")) carteira.adicionar(api.buscarAtivo(ticker, "ACAO"));
            if (op.equals("2")) carteira.adicionar(api.buscarAtivo(ticker, "CRIPTO"));
            if (op.equals("3")) carteira.adicionar(api.buscarAtivo(ticker, "ETF"));
        }
    }
}