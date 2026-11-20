package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class RankingBoard.
 * Author: Beatriz
 * Date: 2026-11-20 04:00
 * Cyclomatic Complexity:
 * - RankingBoard(): 1
 * - RankingBoard(File): 1
 * - load() (privado, testado através do construtor): 3
 * - save() (privado, testado através do add()): 3
 * - add(): 1
 * - getTop(): 1
 * - printTop(): 3
 */
class RankingBoardTest {

    /** Pasta temporária criada pelo JUnit para cada teste e apagada no fim. */
    @TempDir
    Path tempDir;

    private File rankingFile;
    private RankingBoard board;

    @BeforeEach
    void setUp() {
        rankingFile = tempDir.resolve("ranking.json").toFile();
        board = new RankingBoard(rankingFile);
    }

    @AfterEach
    void tearDown() {
        board = null;
        rankingFile = null;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /**
     * Estatísticas falsas de uma partida, com os valores que o ranking usa.
     * Evita ter de jogar uma partida a sério para obter uma pontuação.
     */
    private static VoyageStats voyage(int points, long millis, int volleys) {
        return new VoyageStats() {
            @Override public int getPoints() { return points; }
            @Override public long getElapsedMillis() { return millis; }
            @Override public int getVolleys() { return volleys; }
        };
    }

    /** Executa uma ação e devolve o que foi escrito no System.out. */
    private static String captureOut(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /** Executa uma ação e devolve o que foi escrito no System.err. */
    private static String captureErr(Runnable action) {
        PrintStream original = System.err;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setErr(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            action.run();
        } finally {
            System.setErr(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    // ------------------------------------------------------------------
    // Construtores
    // ------------------------------------------------------------------

    @Test
    @DisplayName("RankingBoard(): o construtor por omissão usa o ficheiro data/ranking.json e devolve no máximo o top pedido")
    void constructorDefault() {
        RankingBoard defaultBoard = new RankingBoard();
        List<RankingBoard.Entry> top = defaultBoard.getTop(RankingBoard.TOP_SIZE);
        assertAll(
                () -> assertEquals("data/ranking.json", RankingBoard.RANKING_FILE, "Error: expected the default file data/ranking.json but got " + RankingBoard.RANKING_FILE),
                () -> assertNotNull(top, "Error: expected a list (possibly empty) but got null"),
                () -> assertTrue(top.size() <= RankingBoard.TOP_SIZE, "Error: expected at most " + RankingBoard.TOP_SIZE + " entries but got " + top.size())
        );
    }

    @Test
    @DisplayName("RankingBoard(File): o construtor com ficheiro cria um ranking a partir desse ficheiro")
    void constructorWithFile() {
        assertNotNull(board, "Error: expected a RankingBoard instance but got null");
    }

    // ------------------------------------------------------------------
    // load() — chamado pelo construtor
    // ------------------------------------------------------------------

    @Test
    @DisplayName("load: se o ficheiro ainda não existe, o ranking começa vazio")
    void load1() {
        assertAll(
                () -> assertFalse(rankingFile.exists(), "Error: expected the file not to exist yet"),
                () -> assertTrue(board.getTop(RankingBoard.TOP_SIZE).isEmpty(), "Error: expected an empty ranking but got " + board.getTop(RankingBoard.TOP_SIZE))
        );
    }

    @Test
    @DisplayName("load: se o ficheiro existe e é válido, o ranking é lido com as entradas guardadas")
    void load2() {
        board.add("Vasco", voyage(900, 60_000, 9));
        board.add("Bartolomeu", voyage(500, 300_000, 15));

        RankingBoard reloaded = new RankingBoard(rankingFile);
        List<RankingBoard.Entry> top = reloaded.getTop(RankingBoard.TOP_SIZE);

        assertAll(
                () -> assertEquals(2, top.size(), "Error: expected 2 entries after reloading but got " + top.size()),
                () -> assertEquals("Vasco", top.get(0).name(), "Error: expected Vasco in 1st place but got " + top.get(0).name()),
                () -> assertEquals("Bartolomeu", top.get(1).name(), "Error: expected Bartolomeu in 2nd place but got " + top.get(1).name())
        );
    }

    @Test
    @DisplayName("load: se o ficheiro está estragado, avisa no System.err e começa vazio")
    void load3() throws Exception {
        Files.writeString(rankingFile.toPath(), "isto não é JSON");

        RankingBoard[] holder = new RankingBoard[1];
        String err = captureErr(() -> holder[0] = new RankingBoard(rankingFile));

        assertAll(
                () -> assertTrue(err.contains("Não foi possível ler o ranking"), "Error: expected a read error message but got: " + err),
                () -> assertTrue(holder[0].getTop(RankingBoard.TOP_SIZE).isEmpty(), "Error: expected an empty ranking for a corrupted file")
        );
    }

    // ------------------------------------------------------------------
    // save() — chamado pelo add()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("save: ficheiro sem pasta (parent == null) é gravado na pasta atual")
    void save1() {
        File noParent = new File("ranking-test-" + System.nanoTime() + ".json");
        try {
            assertNull(noParent.getParentFile(), "Error: expected a file without parent folder");
            new RankingBoard(noParent).add("Gil", voyage(300, 120_000, 20));
            assertTrue(noParent.exists(), "Error: expected the file " + noParent + " to be created");
        } finally {
            noParent.delete();
        }
    }

    @Test
    @DisplayName("save: se a pasta do ficheiro não existe, é criada antes de gravar")
    void save2() {
        File nested = tempDir.resolve("pasta/nova/ranking.json").toFile();
        new RankingBoard(nested).add("Diogo", voyage(450, 200_000, 12));
        assertAll(
                () -> assertTrue(nested.getParentFile().isDirectory(), "Error: expected the folder " + nested.getParentFile() + " to be created"),
                () -> assertTrue(nested.exists(), "Error: expected the file " + nested + " to be created")
        );
    }

    @Test
    @DisplayName("save: se não for possível gravar, avisa no System.err e não lança exceção")
    void save3() {
        // O "ficheiro" é uma pasta: não dá para ler nem escrever JSON nela
        File folder = tempDir.toFile();
        RankingBoard[] holder = new RankingBoard[1];
        captureErr(() -> holder[0] = new RankingBoard(folder));

        String err = captureErr(() -> holder[0].add("Pedro", voyage(100, 600_000, 30)));

        assertTrue(err.contains("Não foi possível guardar o ranking"), "Error: expected a save error message but got: " + err);
    }

    // ------------------------------------------------------------------
    // add() e getTop()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("add: guarda os dados da partida e devolve a posição, ordenando por pontos e desempatando pelo menor tempo")
    void add() {
        int first = board.add("Vasco", voyage(700, 90_000, 9));
        int better = board.add("Bartolomeu", voyage(800, 300_000, 10));
        int worse = board.add("Gil", voyage(300, 400_000, 20));
        int tieFaster = board.add("Diogo", voyage(700, 60_000, 11));

        RankingBoard.Entry diogo = board.getTop(RankingBoard.TOP_SIZE).get(1);

        assertAll(
                () -> assertEquals(1, first, "Error: expected position 1 for the first entry but got " + first),
                () -> assertEquals(1, better, "Error: expected position 1 for 800 points but got " + better),
                () -> assertEquals(3, worse, "Error: expected position 3 for 300 points but got " + worse),
                () -> assertEquals(2, tieFaster, "Error: expected position 2 for a 700-point tie with less time but got " + tieFaster),
                () -> assertEquals("Diogo", diogo.name(), "Error: expected Diogo but got " + diogo.name()),
                () -> assertEquals(700, diogo.points(), "Error: expected 700 points but got " + diogo.points()),
                () -> assertEquals("Índia", diogo.destination(), "Error: expected destination Índia but got " + diogo.destination()),
                () -> assertEquals(60_000L, diogo.timeMillis(), "Error: expected 60000 ms but got " + diogo.timeMillis()),
                () -> assertEquals("1m 00s", diogo.time(), "Error: expected time 1m 00s but got " + diogo.time()),
                () -> assertEquals(11, diogo.volleys(), "Error: expected 11 volleys but got " + diogo.volleys()),
                () -> assertEquals(LocalDate.now().toString(), diogo.date(), "Error: expected today's date but got " + diogo.date()),
                () -> assertTrue(rankingFile.exists(), "Error: expected the ranking to be saved to " + rankingFile)
        );
    }

    @Test
    @DisplayName("add: lança NullPointerException se as estatísticas da partida forem null")
    void addNullVoyage() {
        assertThrows(NullPointerException.class, () -> board.add("Vasco", null),
                "Error: expected NullPointerException for a null voyage");
    }

    @Test
    @DisplayName("getTop: devolve no máximo n entradas, da melhor para a pior")
    void getTop() {
        board.add("A", voyage(100, 1_000, 9));
        board.add("B", voyage(300, 1_000, 9));
        board.add("C", voyage(200, 1_000, 9));

        List<RankingBoard.Entry> top2 = board.getTop(2);
        List<RankingBoard.Entry> top10 = board.getTop(10);

        assertAll(
                () -> assertEquals(2, top2.size(), "Error: expected 2 entries for getTop(2) but got " + top2.size()),
                () -> assertEquals("B", top2.get(0).name(), "Error: expected B first but got " + top2.get(0).name()),
                () -> assertEquals("C", top2.get(1).name(), "Error: expected C second but got " + top2.get(1).name()),
                () -> assertEquals(3, top10.size(), "Error: expected only the 3 existing entries for getTop(10) but got " + top10.size())
        );
    }

    @Test
    @DisplayName("getTop: lança IllegalArgumentException para um número negativo")
    void getTopNegative() {
        assertThrows(IllegalArgumentException.class, () -> board.getTop(-1),
                "Error: expected IllegalArgumentException for getTop(-1)");
    }

    // ------------------------------------------------------------------
    // printTop()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("printTop: com o ranking vazio, mostra a mensagem para ser o primeiro")
    void printTop1() {
        String out = captureOut(() -> board.printTop());
        assertAll(
                () -> assertTrue(out.contains("GRANDES NAVEGADORES"), "Error: expected the title but got:\n" + out),
                () -> assertTrue(out.contains("Ainda ninguém completou a viagem"), "Error: expected the empty-ranking message but got:\n" + out)
        );
    }

    @Test
    @DisplayName("printTop: com entradas, mostra o cabeçalho da tabela e cada navegador com a sua posição")
    void printTop2() {
        board.add("Vasco", voyage(900, 60_000, 9));
        board.add("Gil", voyage(300, 400_000, 20));

        String out = captureOut(() -> board.printTop());

        assertAll(
                () -> assertTrue(out.contains("Nome"), "Error: expected the table header but got:\n" + out),
                () -> assertTrue(out.matches("(?s).*1\\s+Vasco.*"), "Error: expected Vasco in 1st place but got:\n" + out),
                () -> assertTrue(out.matches("(?s).*2\\s+Gil.*"), "Error: expected Gil in 2nd place but got:\n" + out),
                () -> assertFalse(out.contains("Ainda ninguém"), "Error: did not expect the empty-ranking message but got:\n" + out)
        );
    }

    @Test
    @DisplayName("printTop: com mais de 5 entradas, mostra apenas o top 5")
    void printTop3() {
        for (int i = 1; i <= 6; i++) {
            board.add("Nav" + i, voyage(1000 - i * 100, 60_000, 9));
        }

        String out = captureOut(() -> board.printTop());

        assertAll(
                () -> assertTrue(out.contains("Nav5"), "Error: expected Nav5 (5th place) in the top but got:\n" + out),
                () -> assertFalse(out.contains("Nav6"), "Error: did not expect Nav6 (6th place) in the top 5 but got:\n" + out)
        );
    }
}