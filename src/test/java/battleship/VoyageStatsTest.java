package battleship;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class VoyageStats.
 * Author: Beatriz
 * Date: 2026-11-20 03:00
 * Cyclomatic Complexity:
 * - constructor: 1
 * - Destination.getDisplayName(): 1
 * - Destination.getMinPoints(): 1
 * - Destination.fromPoints(): 3
 * - start(): 1
 * - finish(): 2
 * - getVolleys(): 1
 * - getAccuracy(): 2
 * - getElapsedMillis(): 1
 * - getFormattedTime(): 1
 * - getAccuracyPoints(): 1
 * - getEfficiencyPoints(): 2
 * - getSpeedPoints(): 3
 * - getPoints(): 1
 * - getDestination(): 1
 * - printSummary(): 1
 */
class VoyageStatsTest{

    private VoyageStats stats;

    @BeforeEach
    void setUp() {
        stats = new VoyageStats();
    }

    @AfterEach
    void tearDown() {
        stats = null;
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    /**
     * Partida falsa: só devolve os valores que a VoyageStats usa no finish().
     * Os restantes métodos do IGame não são usados nestes testes.
     */
    private static class FakeGame implements IGame {
        private final int volleys;
        private final int invalidShots;
        private final int repeatedShots;
        private final int hits;

        FakeGame(int volleys, int invalidShots, int repeatedShots, int hits) {
            this.volleys = volleys;
            this.invalidShots = invalidShots;
            this.repeatedShots = repeatedShots;
            this.hits = hits;
        }

        @Override public List<IMove> getAlienMoves() { return Collections.nCopies(volleys, null); }
        @Override public int getInvalidShots() { return invalidShots; }
        @Override public int getRepeatedShots() { return repeatedShots; }
        @Override public int getHits() { return hits; }

        @Override public String randomEnemyFire() { return null; }
        @Override public String readEnemyFire(Scanner in) { return null; }
        @Override public void fireShots(List<IPosition> shots) { }
        @Override public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) { return null; }
        @Override public IFleet getMyFleet() { return null; }
        @Override public IFleet getAlienFleet() { return null; }
        @Override public List<IMove> getMyMoves() { return null; }
        @Override public int getSunkShips() { return 0; }
        @Override public int getRemainingShips() { return 0; }
        @Override public void printMyBoard(boolean show_shots, boolean show_legend) { }
        @Override public void printAlienBoard(boolean show_shots, boolean show_legend) { }
        @Override public void over() { }
    }

    /**
     * Substitui o cronómetro interno por um que devolve sempre o tempo indicado.
     * Assim é possível testar partidas de vários minutos sem esperar.
     */
    private void setElapsedMillis(long millis) throws Exception {
        StopWatch fixed = new StopWatch() {
            @Override
            public long getTime() {
                return millis;
            }
        };
        Field field = VoyageStats.class.getDeclaredField("stopWatch");
        field.setAccessible(true);
        field.set(stats, fixed);
    }

    // ------------------------------------------------------------------
    // Construtor
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Construtor: uma VoyageStats nova começa sem rajadas, sem pontaria e com tempo 0")
    void constructor() {
        assertAll(
                () -> assertEquals(0, stats.getVolleys(), "Error: expected 0 volleys but got " + stats.getVolleys()),
                () -> assertEquals(0.0, stats.getAccuracy(), "Error: expected accuracy 0.0 but got " + stats.getAccuracy()),
                () -> assertEquals(0L, stats.getElapsedMillis(), "Error: expected 0 ms but got " + stats.getElapsedMillis())
        );
    }

    // ------------------------------------------------------------------
    // Enum Destination
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Destination.getDisplayName: cada destino devolve o nome mostrado ao jogador")
    void getDisplayName() {
        assertAll(
                () -> assertEquals("Ceuta", VoyageStats.Destination.CEUTA.getDisplayName(), "Error: wrong name for CEUTA"),
                () -> assertEquals("Madeira", VoyageStats.Destination.MADEIRA.getDisplayName(), "Error: wrong name for MADEIRA"),
                () -> assertEquals("Cabo Bojador", VoyageStats.Destination.CABO_BOJADOR.getDisplayName(), "Error: wrong name for CABO_BOJADOR"),
                () -> assertEquals("Cabo da Boa Esperança", VoyageStats.Destination.BOA_ESPERANCA.getDisplayName(), "Error: wrong name for BOA_ESPERANCA"),
                () -> assertEquals("Índia", VoyageStats.Destination.INDIA.getDisplayName(), "Error: wrong name for INDIA")
        );
    }

    @Test
    @DisplayName("Destination.getMinPoints: cada destino devolve a pontuação mínima correta")
    void getMinPoints() {
        assertAll(
                () -> assertEquals(0, VoyageStats.Destination.CEUTA.getMinPoints(), "Error: expected 0 for CEUTA"),
                () -> assertEquals(280, VoyageStats.Destination.MADEIRA.getMinPoints(), "Error: expected 280 for MADEIRA"),
                () -> assertEquals(400, VoyageStats.Destination.CABO_BOJADOR.getMinPoints(), "Error: expected 400 for CABO_BOJADOR"),
                () -> assertEquals(500, VoyageStats.Destination.BOA_ESPERANCA.getMinPoints(), "Error: expected 500 for BOA_ESPERANCA"),
                () -> assertEquals(600, VoyageStats.Destination.INDIA.getMinPoints(), "Error: expected 600 for INDIA")
        );
    }

    @Test
    @DisplayName("Destination.fromPoints: pontuação negativa não alcança nenhum destino e fica em Ceuta")
    void fromPoints1() {
        VoyageStats.Destination d = VoyageStats.Destination.fromPoints(-1);
        assertEquals(VoyageStats.Destination.CEUTA, d, "Error: expected CEUTA for -1 points but got " + d);
    }

    @Test
    @DisplayName("Destination.fromPoints: 0 pontos só alcança o primeiro destino (Ceuta)")
    void fromPoints2() {
        VoyageStats.Destination d = VoyageStats.Destination.fromPoints(0);
        assertEquals(VoyageStats.Destination.CEUTA, d, "Error: expected CEUTA for 0 points but got " + d);
    }

    @Test
    @DisplayName("Destination.fromPoints: nos limites de cada destino devolve o destino mais distante alcançado")
    void fromPoints3() {
        assertAll(
                () -> assertEquals(VoyageStats.Destination.CEUTA, VoyageStats.Destination.fromPoints(279), "Error: expected CEUTA for 279 points"),
                () -> assertEquals(VoyageStats.Destination.MADEIRA, VoyageStats.Destination.fromPoints(280), "Error: expected MADEIRA for 280 points"),
                () -> assertEquals(VoyageStats.Destination.CABO_BOJADOR, VoyageStats.Destination.fromPoints(400), "Error: expected CABO_BOJADOR for 400 points"),
                () -> assertEquals(VoyageStats.Destination.BOA_ESPERANCA, VoyageStats.Destination.fromPoints(500), "Error: expected BOA_ESPERANCA for 500 points"),
                () -> assertEquals(VoyageStats.Destination.INDIA, VoyageStats.Destination.fromPoints(1000), "Error: expected INDIA for 1000 points")
        );
    }

    // ------------------------------------------------------------------
    // Cronómetro
    // ------------------------------------------------------------------

    @Test
    @DisplayName("start: depois de começar, o tempo da partida vai avançando")
    void start() throws InterruptedException {
        stats.start();
        Thread.sleep(20);
        long elapsed = stats.getElapsedMillis();
        assertTrue(elapsed >= 20, "Error: expected at least 20 ms after start but got " + elapsed);
    }

    @Test
    @DisplayName("finish: sem start, guarda os números da partida e o tempo fica a 0")
    void finish1() {
        stats.finish(new FakeGame(10, 2, 1, 20));
        assertAll(
                () -> assertEquals(10, stats.getVolleys(), "Error: expected 10 volleys but got " + stats.getVolleys()),
                () -> assertEquals(20.0 / 27, stats.getAccuracy(), 1e-9, "Error: expected accuracy 20/27 but got " + stats.getAccuracy()),
                () -> assertEquals(0L, stats.getElapsedMillis(), "Error: expected 0 ms without start but got " + stats.getElapsedMillis())
        );
    }

    @Test
    @DisplayName("finish: depois de start, para o cronómetro e o tempo deixa de avançar")
    void finish2() throws InterruptedException {
        stats.start();
        Thread.sleep(10);
        stats.finish(new FakeGame(9, 0, 0, 25));
        long timeAtFinish = stats.getElapsedMillis();
        Thread.sleep(20);
        long timeLater = stats.getElapsedMillis();
        assertAll(
                () -> assertEquals(9, stats.getVolleys(), "Error: expected 9 volleys but got " + stats.getVolleys()),
                () -> assertEquals(timeAtFinish, timeLater, "Error: expected the time to stop at " + timeAtFinish + " ms but got " + timeLater)
        );
    }

    @Test
    @DisplayName("finish: lança NullPointerException se a partida for null")
    void finishNullGame() {
        assertThrows(NullPointerException.class, () -> stats.finish(null),
                "Error: expected NullPointerException for a null game");
    }

    // ------------------------------------------------------------------
    // Getters simples
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getVolleys: devolve o número de rajadas da partida")
    void getVolleys() {
        stats.finish(new FakeGame(12, 0, 0, 0));
        assertEquals(12, stats.getVolleys(), "Error: expected 12 volleys but got " + stats.getVolleys());
    }

    @Test
    @DisplayName("getAccuracy: sem tiros válidos, a pontaria é 0 (evita divisão por zero)")
    void getAccuracy1() {
        stats.finish(new FakeGame(1, 2, 1, 0));
        assertEquals(0.0, stats.getAccuracy(), "Error: expected accuracy 0.0 with no valid shots but got " + stats.getAccuracy());
    }

    @Test
    @DisplayName("getAccuracy: com tiros válidos, a pontaria é certeiros / válidos")
    void getAccuracy2() {
        stats.finish(new FakeGame(9, 0, 0, 25));
        assertEquals(25.0 / 27, stats.getAccuracy(), 1e-9, "Error: expected accuracy 25/27 but got " + stats.getAccuracy());
    }

    @Test
    @DisplayName("getElapsedMillis: devolve o tempo marcado pelo cronómetro")
    void getElapsedMillis() throws Exception {
        setElapsedMillis(123_456);
        assertEquals(123_456L, stats.getElapsedMillis(), "Error: expected 123456 ms but got " + stats.getElapsedMillis());
    }

    @Test
    @DisplayName("getFormattedTime: 3 minutos e 42 segundos aparecem como \"3m 42s\"")
    void getFormattedTime() throws Exception {
        setElapsedMillis(3 * 60_000 + 42_000);
        assertEquals("3m 42s", stats.getFormattedTime(), "Error: expected \"3m 42s\" but got \"" + stats.getFormattedTime() + "\"");
    }

    // ------------------------------------------------------------------
    // Pontuação
    // ------------------------------------------------------------------

    @Test
    @DisplayName("getAccuracyPoints: 25 certeiros em 27 tiros válidos dão 370 pontos de pontaria")
    void getAccuracyPoints() {
        stats.finish(new FakeGame(9, 0, 0, 25));
        assertEquals(370, stats.getAccuracyPoints(), "Error: expected 370 accuracy points but got " + stats.getAccuracyPoints());
    }

    @Test
    @DisplayName("getEfficiencyPoints: sem rajadas, a eficiência vale 0")
    void getEfficiencyPoints1() {
        assertEquals(0, stats.getEfficiencyPoints(), "Error: expected 0 efficiency points with no volleys but got " + stats.getEfficiencyPoints());
    }

    @Test
    @DisplayName("getEfficiencyPoints: 9 rajadas dão o máximo (400) e 18 rajadas dão metade (200)")
    void getEfficiencyPoints2() {
        VoyageStats best = new VoyageStats();
        best.finish(new FakeGame(9, 0, 0, 25));
        stats.finish(new FakeGame(18, 0, 0, 25));
        assertAll(
                () -> assertEquals(400, best.getEfficiencyPoints(), "Error: expected 400 points for 9 volleys but got " + best.getEfficiencyPoints()),
                () -> assertEquals(200, stats.getEfficiencyPoints(), "Error: expected 200 points for 18 volleys but got " + stats.getEfficiencyPoints())
        );
    }

    @Test
    @DisplayName("getSpeedPoints: até 2 minutos, a rapidez vale o máximo (200)")
    void getSpeedPoints1() throws Exception {
        setElapsedMillis(2 * 60_000);
        assertEquals(200, stats.getSpeedPoints(), "Error: expected 200 speed points at 2 minutes but got " + stats.getSpeedPoints());
    }

    @Test
    @DisplayName("getSpeedPoints: a partir de 15 minutos, a rapidez vale 0")
    void getSpeedPoints2() throws Exception {
        setElapsedMillis(15 * 60_000);
        assertEquals(0, stats.getSpeedPoints(), "Error: expected 0 speed points at 15 minutes but got " + stats.getSpeedPoints());
    }

    @Test
    @DisplayName("getSpeedPoints: entre 2 e 15 minutos, a rapidez desce linearmente (8,5 min = 100)")
    void getSpeedPoints3() throws Exception {
        setElapsedMillis(510_000);
        assertEquals(100, stats.getSpeedPoints(), "Error: expected 100 speed points at 8.5 minutes but got " + stats.getSpeedPoints());
    }

    @Test
    @DisplayName("getPoints: soma pontaria + eficiência + rapidez (370 + 400 + 200 = 970)")
    void getPoints() throws Exception {
        stats.finish(new FakeGame(9, 0, 0, 25));
        setElapsedMillis(60_000);
        assertEquals(970, stats.getPoints(), "Error: expected 970 points but got " + stats.getPoints());
    }

    @Test
    @DisplayName("getDestination: com 970 pontos, a viagem chega à Índia")
    void getDestination() throws Exception {
        stats.finish(new FakeGame(9, 0, 0, 25));
        setElapsedMillis(60_000);
        assertEquals(VoyageStats.Destination.INDIA, stats.getDestination(), "Error: expected INDIA but got " + stats.getDestination());
    }

    @Test
    @DisplayName("printSummary: mostra no terminal o destino, o tempo, as rajadas e os pontos")
    void printSummary() throws Exception {
        stats.finish(new FakeGame(9, 0, 0, 25));
        setElapsedMillis(60_000);

        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, "UTF-8"));
        try {
            stats.printSummary();
        } finally {
            System.setOut(originalOut);
        }
        String output = buffer.toString("UTF-8");

        assertAll(
                () -> assertTrue(output.contains("ROTA DA ÍNDIA"), "Error: expected the title in the summary but got:\n" + output),
                () -> assertTrue(output.contains("Chegaste a: Índia!"), "Error: expected destination Índia in the summary but got:\n" + output),
                () -> assertTrue(output.contains("Tempo: 1m 00s"), "Error: expected time 1m 00s in the summary but got:\n" + output),
                () -> assertTrue(output.contains("Rajadas: 9"), "Error: expected 9 volleys in the summary but got:\n" + output),
                () -> assertTrue(output.contains("Pontos: 970"), "Error: expected 970 points in the summary but got:\n" + output)
        );
    }
}