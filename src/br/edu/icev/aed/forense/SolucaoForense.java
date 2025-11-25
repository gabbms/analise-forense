package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SolucaoForense implements AnaliseForenseAvancada {

    // Construtor público sem parâmetros, CRÍTICO para o validador
    public SolucaoForense() {
        // Inicializações, se necessário
    }

    // Método auxiliar para parsear uma linha do CSV e extrair os campos necessários
    private String[] parseLine(String line) {
        // Usar limite negativo para garantir que campos vazios no final sejam incluídos
        String[] parts = line.split(",", -1);
        if (parts.length < 7) {
            throw new IllegalArgumentException("Linha de log malformada: " + line);
        }
        return parts;
    }

    // DESAFIO 1: Encontrar Sessões Inválidas (O(N) + Custo de I/O)
    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        Map<String, Stack<String>> userSessionStacks = new HashMap<>();
        Set<String> invalidSessions = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula o cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = parseLine(line);
                String userId = parts[1].trim();
                String sessionId = parts[2].trim();
                String actionType = parts[3].trim();

                Stack<String> sessionStack = userSessionStacks.computeIfAbsent(userId, k -> new Stack<>());

                if ("LOGIN".equals(actionType)) {
                    if (!sessionStack.isEmpty()) {
                        // LOGIN aninhado: a sessão anterior é inválida
                        invalidSessions.add(sessionStack.peek());
                    }
                    sessionStack.push(sessionId);

                } else if ("LOGOUT".equals(actionType)) {
                    if (sessionStack.isEmpty() || !sessionStack.peek().equals(sessionId)) {
                        // LOGOUT sem LOGIN correspondente ou fora de ordem
                        invalidSessions.add(sessionId);
                    } else {
                        sessionStack.pop();
                    }
                }
            }
        }

        // Sessões remanescentes na pilha são inválidas
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

    // DESAFIO 3: Priorizar Alertas (O(N log N) + Custo de I/O)
    @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {
        if (n <= 0) {
            return Collections.emptyList();
        }

        // Comparator para ordenar por severityLevel em ordem decrescente
        Comparator<Alerta> severityComparator = (a1, a2) -> Integer.compare(a2.getSeverityLevel(), a1.getSeverityLevel());
        PriorityQueue<Alerta> priorityQueue = new PriorityQueue<>(severityComparator);

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula o cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = parseLine(line);

                try {
                    long timestamp = Long.parseLong(parts[0].trim());
                    String userId = parts[1].trim();
                    String sessionId = parts[2].trim();
                    String actionType = parts[3].trim();
                    String targetResource = parts[4].trim();
                    long bytesTransferred = Long.parseLong(parts[5].trim());
                    int severityLevel = Integer.parseInt(parts[6].trim());

                    // CORREÇÃO: Construtor de 7 argumentos, com cast para int no 6º argumento
                    priorityQueue.add(new Alerta(
                            timestamp,
                            userId,
                            sessionId,
                            actionType,
                            targetResource,
                            (int) bytesTransferred, // Cast para int, pois o construtor exige int
                            severityLevel
                    ));
                } catch (NumberFormatException e) {
                    // Ignora linhas com formato numérico inválido, mantendo a robustez
                    continue;
                }
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
        // Estrutura auxiliar para armazenar os dados de transferência
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
            br.readLine(); // Pula o cabeçalho
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = parseLine(line);

                try {
                    long timestamp = Long.parseLong(parts[0].trim());
                    long bytesTransferred = Long.parseLong(parts[5].trim());

                    if (bytesTransferred > 0) {
                        transferLogs.add(new TransferEntry(timestamp, bytesTransferred));
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        if (transferLogs.isEmpty()) {
            return Collections.emptyMap();
        }

        // A lógica do NGE (Next Greater Element) exige a iteração inversa
        Collections.reverse(transferLogs);

        Stack<TransferEntry> stack = new Stack<>();
        Map<Long, Long> result = new HashMap<>();

        for (TransferEntry currentEntry : transferLogs) {
            // Enquanto a pilha não estiver vazia E o elemento no topo for menor ou igual ao atual, desempilha
            while (!stack.isEmpty() && stack.peek().bytesTransferred <= currentEntry.bytesTransferred) {
                stack.pop();
            }

            if (!stack.isEmpty()) {
                // O topo da pilha é o "próximo elemento maior"
                result.put(currentEntry.timestamp, stack.peek().timestamp);
            }

            // Empilha o elemento atual
            stack.push(currentEntry);
        }

        return result;
    }

    // DESAFIO 5: Rastrear Contaminação (O(V + E) + Custo de I/O)
    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException {
        // 1. Construção do Grafo (Usando Map<String, Set<String>> para evitar duplicatas)
        Map<String, Set<String>> contaminationGraph = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoArquivo))) {
            String line;
            br.readLine(); // Pula o cabeçalho

            String previousResource = null;
            String previousSessionId = null;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = parseLine(line);
                String sessionId = parts[2].trim();
                String targetResource = parts[4].trim();

                if (!targetResource.isEmpty()) {
                    if (sessionId.equals(previousSessionId) && previousResource != null) {
                        if (!targetResource.equals(previousResource)) {
                            // Cria uma aresta direcionada: previousResource -> targetResource
                            contaminationGraph.computeIfAbsent(previousResource, k -> new HashSet<>()).add(targetResource);
                        }
                    }
                    previousResource = targetResource;
                    previousSessionId = sessionId;
                } else {
                    // Se o recurso alvo estiver vazio, reseta o rastreamento de sessão
                    previousResource = null;
                    previousSessionId = null;
                }
            }
        }

        // Garante que o nó inicial esteja no grafo, mesmo que não tenha arestas de saída
        contaminationGraph.putIfAbsent(recursoInicial, new HashSet<>());

        // 2. Execução do BFS
        if (recursoInicial.equals(recursoAlvo)) {
            // Verifica se o recurso inicial existe no grafo (como origem ou destino)
            boolean exists = contaminationGraph.containsKey(recursoInicial) ||
                    contaminationGraph.values().stream().anyMatch(s -> s.contains(recursoInicial));
            if (exists) {
                return Optional.of(Collections.singletonList(recursoInicial));
            }
            return Optional.empty();
        }

        Queue<String> queue = new LinkedList<>();
        Map<String, String> predecessor = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(recursoInicial);
        visited.add(recursoInicial);
        predecessor.put(recursoInicial, null);

        String foundTarget = null;

        while (!queue.isEmpty()) {
            String currentResource = queue.poll();

            if (currentResource.equals(recursoAlvo)) {
                foundTarget = currentResource;
                break;
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

        if (foundTarget != null) {
            return Optional.of(reconstructPath(predecessor, recursoInicial, recursoAlvo));
        }

        return Optional.empty();
    }

    // Método auxiliar para reconstruir o caminho a partir do mapa de predecessores
    private List<String> reconstructPath(Map<String, String> predecessor, String start, String end) {
        LinkedList<String> path = new LinkedList<>();
        String current = end;

        while (current != null) {
            path.addFirst(current);
            if (current.equals(start)) {
                break; // Chegou ao início
            }
            current = predecessor.get(current);
        }

        // Se o caminho não começar com o nó inicial, significa que o nó inicial não estava no grafo
        // ou a busca falhou.
        if (path.isEmpty() || !path.getFirst().equals(start)) {
            return Collections.emptyList(); // Retorna vazio se o caminho não for válido
        }

        return path;
    }
}