package sistemacotacoes.modelo;

public interface Analisavel {
    double calcularRisco();     // Polimorfismo: Cada um calcula o seu
    String obterRecomendacao(); // Ex: "Compra", "Venda"
}