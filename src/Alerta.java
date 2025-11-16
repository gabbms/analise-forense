public class Alerta {
    private final long timestamp;
    private final String acao = "";
    private final int nivelSeveridade = 0;
    private String sessionId;
    private String UserId;
    private String ActionType;


    public Alerta(long timestamp, String userId, String sessionId, String actionType, String targetResource, int severityLevel, long bytesTransferred) {
        this.timestamp = timestamp;

    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAcao() {
        return acao;
    }

    public int getNivelSeveridade() {
        return nivelSeveridade;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUserId() {
        return UserId;
    }

    public String getActionType() {
        return ActionType;
    }
}
