package teste;

import br.edu.icev.aed.forense.AnaliseForenseAvancada;
import br.edu.icev.aed.forense.SolucaoForense;
import br.edu.icev.aed.forense.Alerta;
import java.util.*;

public class TesteSolucao {

    public static void main(String[] args) {
        try {
            AnaliseForenseAvancada analyzer = new SolucaoForense();
            String arquivoLogs = "arquivo_logs.csv";

            // --- Testando Desafio 1: Sessões Inválidas ---
            System.out.println("--- Testando Desafio 1: Sessões Inválidas ---");
            Set<String> invalidas = analyzer.encontrarSessoesInvalidas(arquivoLogs);
            System.out.println("Sessões Inválidas Encontradas: " + invalidas.size());
            System.out.println("Resultados: " + invalidas);

            // --- Testando Desafio 2: Linha do Tempo ---
            System.out.println("\n--- Testando Desafio 2: Linha do Tempo ---");
            String sessionId = "session-a-01"; // Exemplo de ID de sessão
            List<String> timeline = analyzer.reconstruirLinhaTempo(arquivoLogs, sessionId);
            System.out.println("Linha do Tempo para " + sessionId + ": " + timeline);

            // --- Testando Desafio 3: Priorizar Alertas ---
            System.out.println("\n--- Testando Desafio 3: Priorizar Alertas ---");
            int n = 5;
            List<Alerta> topAlerts = analyzer.priorizarAlertas(arquivoLogs, n);
            System.out.println("Top " + n + " Alertas (Severidade):");
            for (Alerta a : topAlerts) {
                System.out.println("  - Sessão: " + a.getSessionId() + ", Nível: " + a.getSeverityLevel());
            }

            // --- Testando Desafio 4: Picos de Transferência ---
            System.out.println("\n--- Testando Desafio 4: Picos de Transferência ---");
            Map<Long, Long> picos = analyzer.encontrarPicosTransferencia(arquivoLogs);
            System.out.println("Picos de Transferência Encontrados: " + picos.size());
            // Imprime apenas os 5 primeiros para não poluir o console
            int count = 0;
            System.out.print("Resultados (Amostra): {");
            for (Map.Entry<Long, Long> entry : picos.entrySet()) {
                if (count >= 5) break;
                System.out.print(entry.getKey() + "=" + entry.getValue() + ", ");
                count++;
            }
            System.out.println("...}");


            // --- Testando Desafio 5: Rastrear Contaminação ---
            System.out.println("\n--- Testando Desafio 5: Rastrear Contaminação ---");

            // RECURSOS REAIS DO SEU LOG (Com base na saída de debug anterior)
            String origem = "/usr/bin/sshd";
            String destino = "/bin/ls";

            Optional<List<String>> caminho = analyzer.rastrearContaminacao(arquivoLogs, origem, destino);

            if (caminho.isPresent()) {
                System.out.println("Caminho Mais Curto Encontrado:");
                System.out.println(caminho.get());
            } else {
                System.out.println("Caminho não encontrado.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}