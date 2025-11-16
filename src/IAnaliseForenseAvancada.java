import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


 interface IAnaliseForenseAvancada {

     Set<String> desafio1_encontrarSessoesInvalidas(String caminhoArquivoCsv) throws IOException;

     List<String> desafio2_reconstruirLinhaDoTempo(String caminhoArquivoCsv, String sessionID) throws IOException;

     List<Alerta> desafio3_priorizarAlertas(String caminhoArquivoCsv, int n) throws IOException;

     Map<Long, Long> desafio4_encontrarPicosDeTransferencia(String caminhoArquivoCsv) throws IOException;

     Optional<List<String>> desafio5_rastrearContaminacao(String caminhoArquivoCsv, String recursoInicial, String recursoAlvo) throws IOException;
}
