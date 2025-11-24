package br.edu.icev.aed.forense;

// Classe de apoio para representar uma linha do log.
public class LogEntry {
    public final long timestamp;
    public final String userId;
    public final String sessionId;
    public final String actionType;
    public final String targetResource;
    public final long bytesTransferred;
    public final int severityLevel;

    public LogEntry(long timestamp, String userId, String sessionId, String actionType, String targetResource, long bytesTransferred, int severityLevel) {
        this.timestamp = timestamp;
        this.userId = userId;
        this.sessionId = sessionId;
        this.actionType = actionType;
        this.targetResource = targetResource;
        this.bytesTransferred = bytesTransferred;
        this.severityLevel = severityLevel;
    }

    // Método estático para parsear uma linha do CSV de forma otimizada.
    public static LogEntry parse(String line) {
        String[] parts = line.split(",");
        if (parts.length < 7) {
            throw new IllegalArgumentException("Linha de log malformada: " + line);
        }

        try {
            long timestamp = Long.parseLong(parts[0].trim());
            String userId = parts[1].trim();
            String sessionId = parts[2].trim();
            String actionType = parts[3].trim();
            String targetResource = parts[4].trim();
            long bytesTransferred = Long.parseLong(parts[5].trim());
            int severityLevel = Integer.parseInt(parts[6].trim());

            return new LogEntry(timestamp, userId, sessionId, actionType, targetResource, bytesTransferred, severityLevel);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Erro de formato numérico na linha: " + line, e);
        }
    }
}
