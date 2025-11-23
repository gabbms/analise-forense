package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AnaliseForense implements IAnaliseForenseAvancada {

    private List<Alerta> lerArquivo(String caminhoArquivo) throws IOException {
        List<Alerta> alertas = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Path.of(caminhoArquivo))) {
            String linha;
            boolean primeira = true;
            while ((linha = br.readLine()) != null) {
                if (primeira) {
                    if (linha.toLowerCase().contains("timestamp")) {
                        primeira = false;
                        continue;
                    }
                    primeira = false;
                }
                String[] cols = linha.split(",");
                if (cols.length < 7) continue;

                long timestamp = Long.parseLong(cols[0].trim());
                String userId = cols[1].trim();
                String sessionId = cols[2].trim();
                String actionType = cols[3].trim();
                String targetResource = cols[4].trim();
                int severityLevel = Integer.parseInt(cols[5].trim());
                long bytesTransferred = Long.parseLong(cols[6].trim());

                alertas.add(new Alerta(timestamp, userId, sessionId, actionType, targetResource, severityLevel, bytesTransferred));
            }
        }
        return alertas;
    }

    /* Desafio 1: Encontrar sessões inválidas */
    @Override
    public Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException {
        List<Alerta> alertas = lerArquivo(caminhoArquivo);

        Set<String> sessoesInvalidas = new HashSet<>();


        Set<String> invalidas = new HashSet<>();

        Map<String, Deque<String>> pilhasPorUsuario = new HashMap<>();

        boolean foraDeOrdem = false;


        for (int i = 1; i < alertas.size(); i++) {
            if (alertas.get(i).getTimestamp() < alertas.get(i - 1).getTimestamp()) {
                foraDeOrdem = true;
                break;
            }
        }

        if (foraDeOrdem) {
            alertas.sort(Comparator.comparingLong(Alerta::getTimestamp));
        }

        for (Alerta a: alertas){
            String user = a.getUserId();
            String session = a.getSessionId();
            String action = a.getActionType();

            if (user == null || session == null || action == null) {
                continue;
            }

            user = user.trim();
            session = session.trim();
            action = action.trim();


            if (user.isEmpty() || session.isEmpty() || action.isEmpty()){
                continue;
            }


            Deque<String> pilha = pilhasPorUsuario.get(user);
            if (pilha == null) {
                pilha = new ArrayDeque<>();
                pilhasPorUsuario.put(user, pilha);
            }

            if (action.equalsIgnoreCase("LOGIN")){

                if (!pilha.isEmpty()){
                    invalidas.add(session);
                }
                pilha.push(session);
            }
            else if (action.equalsIgnoreCase("LOGOUT")){
                if (pilha.isEmpty()){
                    invalidas.add(session);
                }
                else if (!pilha.peek().equals(session)) {
                    invalidas.add(session);
                }
                else {
                    pilha.pop();
                }
            }
        }


        for (Deque<String> pilha : pilhasPorUsuario.values()){
            while (!pilha.isEmpty()) invalidas.add(pilha.pop());
        }

        return invalidas;
    }













    @Override
    public List<String> reconstruirLinhaDoTempo(String caminhoArquivo, String sessionID) throws IOException {
        List<Alerta> alertas = lerArquivo(caminhoArquivo);

        boolean foraDeOrdem = false;

        for(int i = 1; i < alertas.size(); i++) {
            if (alertas.get(i).getTimestamp() < alertas.get(i - 1).getTimestamp()) {
                foraDeOrdem = true;
                break;
            }
        }

        if(foraDeOrdem) {
            alertas.sort(Comparator.comparingLong(Alerta::getTimestamp));
        }

        Queue<String> fila = new LinkedList<>();

        for(Alerta i : alertas) {
            if(i.getSessionId().equals(sessionID)) {
                fila.add(i.getActionType());
            }
        }

        List<String> resultado = new ArrayList<>();

        while (!fila.isEmpty()) {
            resultado.add(fila.poll());
        }

        return resultado;
    }

    @Override
    public List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException {
        return List.of();
    }

    @Override
    public Map<Long, Long>encontrarPicosDeTransferencia(String caminhoArquivo) throws IOException {
        return Map.of();
    }

    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException {
        return Optional.empty();
    }
}
