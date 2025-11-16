import java.util.Set;
public class Main {
    public static void main(String[] args) {
        AnaliseForense af = new AnaliseForense();

        try {
            Set<String> invalidas = af.encontrarSessoesInvalidas("arquivo_logs.csv");

            System.out.println("\n=== SESSÕES INVÁLIDAS ===");
            if (invalidas.isEmpty()) {
                System.out.println("Nenhuma sessão inválida encontrada.");
            } else {
                invalidas.forEach(s -> System.out.println("- " + s));
            }

        } catch (Exception e) {
            System.out.println("Erro ao processar arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
