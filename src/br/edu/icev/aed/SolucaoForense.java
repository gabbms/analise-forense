package br.edu.icev.aed;

import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// Implementação sem o LogCache estático. Cada método realiza a leitura do arquivo.
public class SolucaoForense implements AnaliseForenseAvancada {

    // Método auxiliar para ler o arquivo e retornar a lista completa de LogEntry
    private List<LogEntry> readAllLogs(String caminhoArquivo) throws IOException {
        List<LogEntry> allLogs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula o cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                allLogs.add(LogEntry.parse(line));
            }
        }
        return allLogs;
    }

    // DESAFIO 1: Encontrar Sessões Inválidas (O(N) + Custo de I/O)
    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        List<LogEntry> allLogs = readAllLogs(caminhoArquivo); // Leitura do arquivo

        Map<String, Stack<String>> userSessionStacks = new HashMap<>();
        Set<String> invalidSessions = new HashSet<>();

        for (LogEntry entry : allLogs) {
            String userId = entry.userId;
            String sessionId = entry.sessionId;
            String actionType = entry.actionType;

            Stack<String> sessionStack = userSessionStacks.computeIfAbsent(userId, k -> new Stack<>());

            if ("LOGIN".equals(actionType)) {
                if (!sessionStack.isEmpty()) {
                    invalidSessions.add(sessionStack.peek());
                }
                sessionStack.push(sessionId);

            } else if ("LOGOUT".equals(actionType)) {
                if (sessionStack.isEmpty() || !sessionStack.peek().equals(sessionId)) {
                    invalidSessions.add(sessionId);
                } else {
                    sessionStack.pop();
                }
            }
        }

        for (Stack<String> stack : userSessionStacks.values()) {
            invalidSessions.addAll(stack);
        }

        return invalidSessions;
    }

    // DESAFIO 2: Reconstruir Linha do Tempo (O(N) + Custo de I/O)
    @Override
    public List<String> reconstruirLinhaTempo(String caminhoArquivo, String sessionId) throws IOException {
        List<String> timeline = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula o cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                LogEntry entry = LogEntry.parse(line);
                if (entry.sessionId.equals(sessionId)) {
                    timeline.add(entry.actionType);
                }
            }
        }
        return timeline;
    }

    // DESAFIO 3: Priorizar Alertas (O(N log N) + Custo de I/O)
    @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {
        if (n <= 0) {
            return Collections.emptyList();
        }

        Comparator<Alerta> severityComparator = (a1, a2) -> Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel());
        PriorityQueue<Alerta> priorityQueue = new PriorityQueue<>(severityComparator);

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula o cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                LogEntry entry = LogEntry.parse(line);
                priorityQueue.add(new Alerta(entry.timestamp, entry.sessionId, entry.severityLevel));
            }
        }

        List<Alerta> topAlerts = new ArrayList<>(n);
        for (int i = 0; i < n && !priorityQueue.isEmpty(); i++) {
            topAlerts.add(priorityQueue.poll());
        }

        return topAlerts;
    }

    // DESAFIO 4: Encontrar Picos de Transferência (O(N) + Custo de I/O)
    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {
        List<LogEntry> allLogs = readAllLogs(caminhoArquivo); // Leitura do arquivo

        List<LogEntry> transferLogs = allLogs.stream()
                .filter(log -> log.bytesTransferred > 0)
                .collect(Collectors.toList());

        if (transferLogs.isEmpty()) {
            return Collections.emptyMap();
        }

        Collections.reverse(transferLogs);

        Stack<LogEntry> stack = new Stack<>();
        Map<Long, Long> result = new HashMap<>();

        for (LogEntry currentEntry : transferLogs) {
            while (!stack.isEmpty() && stack.peek().bytesTransferred <= currentEntry.bytesTransferred) {
                stack.pop();
            }

            if (!stack.isEmpty()) {
                result.put(currentEntry.timestamp, stack.peek().timestamp);
            }

            stack.push(currentEntry);
        }

        return result;
    }

    // DESAFIO 5: Rastrear Contaminação (O(V + E) + Custo de I/O)
    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException {
        // 1. Construção do Grafo (Requer leitura completa do arquivo)
        Map<String, Set<String>> contaminationGraph = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula o cabeçalho

            String previousResource = null;
            String previousSessionId = null;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                LogEntry entry = LogEntry.parse(line);

                if (entry.targetResource != null && !entry.targetResource.isEmpty()) {
                    if (entry.sessionId.equals(previousSessionId) && previousResource != null) {
                        if (!entry.targetResource.equals(previousResource)) {
                            contaminationGraph.computeIfAbsent(previousResource, k -> new HashSet<>()).add(entry.targetResource);
                        }
                    }
                    previousResource = entry.targetResource;
                    previousSessionId = entry.sessionId;
                } else {
                    previousResource = null;
                    previousSessionId = null;
                }
            }
        }

        // 2. Execução do BFS
        if (recursoInicial.equals(recursoAlvo)) {
            if (contaminationGraph.containsKey(recursoInicial) || contaminationGraph.values().stream().anyMatch(s -> s.contains(recursoInicial))) {
                List<String> path = new ArrayList<>();
                path.add(recursoInicial);
                return Optional.of(path);
            }
            return Optional.empty();
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> predecessor = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(recursoInicial);
        visited.add(recursoInicial);
        predecessor.put(recursoInicial, null);

        while (!queue.isEmpty()) {
            String currentResource = queue.poll();

            if (currentResource.equals(recursoAlvo)) {
                return Optional.of(reconstructPath(predecessor, recursoInicial, recursoAlvo));
            }

            Set<String> neighbors = contaminationGraph.getOrDefault(currentResource, Collections.emptySet());

            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    predecessor.put(neighbor, currentResource);
                    queue.add(neighbor);
                }
            }
        }

        return Optional.empty();
    }

    private List<String> reconstructPath(Map<String, String> predecessor, String start, String end) {
        LinkedList<String> path = new LinkedList<>();
        String current = end;

        while (current != null) {
            path.addFirst(current);
            current = predecessor.get(current);
            if (current != null && current.equals(start)) {
                if (!path.contains(start)) {
                    path.addFirst(start);
                }
                break;
            }
        }

        return path;
    }
}
