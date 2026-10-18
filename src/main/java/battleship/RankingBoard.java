package battleship;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ranking dos Grandes Navegadores (issue #6).
 *
 * <p>Guarda o resultado de cada partida terminada num ficheiro JSON, para que o
 * ranking se mantenha entre execuções do jogo. As entradas são ordenadas por
 * pontos (do maior para o menor) e, em caso de empate, pelo menor tempo.</p>
 */
public class RankingBoard {

    /** Ficheiro onde o ranking é guardado (na pasta data do projeto). */
    public static final String RANKING_FILE = "data/ranking.json";

    /** Quantos navegadores aparecem no top. */
    public static final int TOP_SIZE = 5;

    /**
     * Uma entrada do ranking: o resultado de uma partida.
     * O Jackson converte automaticamente cada entrada de e para JSON.
     */
    public record Entry(String name, int points, String destination,
                        long timeMillis, String time, int volleys, String date) {
    }

    // Ordem do ranking: mais pontos primeiro; em caso de empate, menos tempo primeiro
    private static final Comparator<Entry> RANKING_ORDER =
            Comparator.comparingInt(Entry::points).reversed()
                    .thenComparingLong(Entry::timeMillis);

    private final File file;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Entry> entries;

    /** Cria o ranking a partir do ficheiro por omissão ({@value #RANKING_FILE}). */
    public RankingBoard() {
        this(new File(RANKING_FILE));
    }

    /**
     * Cria o ranking a partir de um ficheiro à escolha (útil para os testes).
     *
     * @param file o ficheiro JSON do ranking
     */
    public RankingBoard(File file) {
        this.file = file;
        this.entries = load();
    }

    /** Lê o ranking do ficheiro; se ainda não existir ou estiver estragado, começa vazio. */
    private List<Entry> load() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return new ArrayList<>(mapper.readValue(file, new TypeReference<List<Entry>>() { }));
        } catch (IOException e) {
            System.err.println("Não foi possível ler o ranking: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /** Grava o ranking no ficheiro, criando a pasta se for preciso. */
    private void save() {
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, entries);
        } catch (IOException e) {
            System.err.println("Não foi possível guardar o ranking: " + e.getMessage());
        }
    }

    /**
     * Acrescenta o resultado de uma partida ao ranking e grava-o no ficheiro.
     *
     * @param name   o nome do navegador
     * @param voyage as estatísticas da partida que terminou
     * @return a posição (1, 2, 3, ...) em que a partida ficou no ranking
     */
    public int add(String name, VoyageStats voyage) {
        Entry entry = new Entry(name, voyage.getPoints(),
                voyage.getDestination().getDisplayName(),
                voyage.getElapsedMillis(), voyage.getFormattedTime(),
                voyage.getVolleys(), LocalDate.now().toString());
        entries.add(entry);
        entries.sort(RANKING_ORDER);
        save();
        return entries.indexOf(entry) + 1;
    }

    /**
     * Devolve os melhores navegadores.
     *
     * @param n quantos devolver
     * @return até {@code n} entradas, da melhor para a pior
     */
    public List<Entry> getTop(int n) {
        return entries.subList(0, Math.min(n, entries.size()));
    }

    /** Mostra no terminal o top dos Grandes Navegadores. */
    public void printTop() {
        System.out.println();
        System.out.println("+----------------- GRANDES NAVEGADORES -----------------+");
        if (entries.isEmpty()) {
            System.out.println("  Ainda ninguém completou a viagem. Sê o primeiro!");
        } else {
            System.out.printf("  %-2s %-12s %-22s %6s %8s%n", "#", "Nome", "Destino", "Pontos", "Tempo");
            int pos = 1;
            for (Entry e : getTop(TOP_SIZE)) {
                System.out.printf("  %-2d %-12s %-22s %6d %8s%n",
                        pos++, e.name(), e.destination(), e.points(), e.time());
            }
        }
        System.out.println("+-------------------------------------------------------+");
    }
}