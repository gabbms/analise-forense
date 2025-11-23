package br.edu.icev.aed.forense;

import java.util.*;
import java.io.IOException;

/**
 * Interface para análise forense avançada de logs de sistema.
 * Esta interface define os contratos para os 5 desafios do trabalho de AED.
 */
public interface AnaliseForenseAvancada {

    /**
     * Desafio 1: Encontrar Sessões Inválidas
     */
    Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException;

    /**
     * Desafio 2: Reconstruir Linha do Tempo
     */
    List<String> reconstruirLinhaTempo(String caminhoArquivo, String sessionId) throws IOException;

    /**
     * Desafio 3: Priorizar Alertas
     */
    List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException;

    /**
     * Desafio 4: Encontrar Picos de Transferência
     */
    Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivo) throws IOException;

    /**
     * Desafio 5: Rastrear Contaminação
     */
    Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException;
}
