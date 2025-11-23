package br.edu.icev.aed.forense;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


 interface IAnaliseForenseAvancada {

     Set<String> encontrarSessoesInvalidas(String caminhoArquivo) throws IOException;

     List<String> reconstruirLinhaDoTempo(String caminhoArquivo, String sessionID) throws IOException;

     List<Alerta> priorizarAlertas(String caminhoArquivo, int n) throws IOException;

     Map<Long, Long> encontrarPicosDeTransferencia(String caminhoArquivo) throws IOException;

     Optional<List<String>> rastrearContaminacao(String caminhoArquivo, String recursoInicial, String recursoAlvo) throws IOException;
}
