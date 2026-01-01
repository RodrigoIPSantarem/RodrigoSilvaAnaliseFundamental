package analisefundamental.enums;

public enum Recomendacao {
    COMPRA_FORTE,   // Margem >= 40%
    COMPRAR,        // Margem >= 25%
    VIGIAR,         // Margem < 25% ou Nota entre 60-79
    EVITAR,         // Nota < 60
    REJEITAR        // Kill Switches ativados
}//enum Recomendacao