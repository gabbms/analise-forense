package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SolucaoForense implements AnaliseForenseAvancada {

    public SolucaoForense() {
    }

    private String[] parseLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Linha de log malformada: " + line);
        }
        return parts;
    }

    // DESAFIO 1: Encontrar Sessões Inválidas
    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        Map<String, Stack<String>> userSessionStacks = new HashMap<>();
        Set<String> invalidSessions = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula o cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseLine(line);
                String userId = parts[1].trim();
                String sessionId = parts[2].trim();
                String actionType = parts[3].trim();

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
        }

        for (Stack<String> stack : userSessionStacks.values()) {
            invalidSessions.addAll(stack);
        }

        return invalidSessions;
    }

    // DESAFIO 2: Reconstruir Linha do Tempo
    @Override
    public List<String> reconstruirLinhaTempo(String caminhoArquivo, String sessionId) throws IOException {
        List<String> timeline = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseLine(line);
                String currentSessionId = parts[2].trim();
                String actionType = parts[3].trim();

                if (currentSessionId.equals(sessionId)) {
                    timeline.add(actionType);
                }
            }
        }
        return timeline;
    }

    // DESAFIO 3: Priorizar Alertas
    @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {
        if (n <= 0) {
            return Collections.emptyList();
        }

        Comparator<Alerta> severityComparator = (a1, a2) -> Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel());
        PriorityQueue<Alerta> priorityQueue = new PriorityQueue<>(severityComparator);

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseLine(line);

                try {
                    long timestamp = Long.parseLong(parts[0].trim());
                    String userId = parts[1].trim();
                    String sessionId = parts[2].trim();
                    String actionType = parts[3].trim();
                    String targetResource = parts[4].trim();
                    int severityLevel = Integer.parseInt(parts[5].trim());

                    String bytesStr = parts[6].trim();
                    int bytesTransferred = bytesStr.isEmpty() ? 0 : Integer.parseInt(bytesStr);

                    priorityQueue.add(new Alerta(
                            timestamp,
                            userId,
                            sessionId,
                            actionType,
                            targetResource,
                            severityLevel,
                            bytesTransferred
                    ));
                } catch (NumberFormatException e) {
                    // Ignora linha com erro de parse
                }
            }
        }

        List<Alerta> topAlerts = new ArrayList<>(n);
        for (int i = 0; i < n && !priorityQueue.isEmpty(); i++) {
            topAlerts.add(priorityQueue.poll());
        }

        return topAlerts;
    }

    // DESAFIO 4: Encontrar Picos de Transferência
    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException {
        class TransferEntry {
            long timestamp;
            long bytesTransferred;

            TransferEntry(long timestamp, long bytesTransferred) {
                this.timestamp = timestamp;
                this.bytesTransferred = bytesTransferred;
            }
        }

        List<TransferEntry> transferLogs = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseLine(line);

                try {
                    long timestamp = Long.parseLong(parts[0].trim());
                    String bytesStr = parts[6].trim();
                    long bytesTransferred = bytesStr.isEmpty() ? 0 : Long.parseLong(bytesStr);

                    if (bytesTransferred > 0) {
                        transferLogs.add(new TransferEntry(timestamp, bytesTransferred));
                    }
                } catch (NumberFormatException e) {
                    // Ignora linha com erro
                }
            }
        }

        if (transferLogs.isEmpty()) {
            return Collections.emptyMap();
        }

        Collections.reverse(transferLogs);

        Stack<TransferEntry> stack = new Stack<>();
        Map<Long, Long> result = new HashMap<>();

        for (TransferEntry currentEntry : transferLogs) {
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

    // DESAFIO 5: Rastrear Contaminação (CORRIGIDO)
    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException {
        Map<String, Set<String>> contaminationGraph = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula cabeçalho

            Map<String, String> sessionLastResource = new HashMap<>();

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = parseLine(line);
                String sessionId = parts[2].trim();
                String targetResource = parts[4].trim();

                if (!targetResource.isEmpty()) {
                    // Adiciona o recurso ao grafo (mesmo que não tenha conexões)
                    contaminationGraph.putIfAbsent(targetResource, new HashSet<>());

                    // Conecta com o recurso anterior da mesma sessão
                    if (sessionLastResource.containsKey(sessionId)) {
                        String previousResource = sessionLastResource.get(sessionId);
                        if (!previousResource.equals(targetResource)) {
                            contaminationGraph.get(previousResource).add(targetResource);
                        }
                    }

                    sessionLastResource.put(sessionId, targetResource);
                }
            }
        }

        // Verifica se o recurso inicial existe no grafo
        if (!contaminationGraph.containsKey(recursoInicial)) {
            return Optional.empty();
        }

        // Se origem e destino são iguais
        if (recursoInicial.equals(recursoAlvo)) {
            return Optional.of(Collections.singletonList(recursoInicial));
        }

        // BFS para encontrar caminho
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
            if (current.equals(start)) {
                break;
            }
            current = predecessor.get(current);
        }

        if (path.isEmpty() || !path.getFirst().equals(start)) {
            return Collections.emptyList();
        }

        return path;
    }
}