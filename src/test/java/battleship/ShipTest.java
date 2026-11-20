package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;

/**
 * Test class for Ship.
 * Author: ${user.name}
 * Date: 2026-11-20 04:30 (testes originais completados para cobertura máxima)
 * Cyclomatic Complexity for each method (contando cada assert, &&, for, while, if e case):
 * - buildShip: 9
 * - Constructor: 1
 * - getCategory: 1
 * - getPositions: 1
 * - getAdjacentPositions: 5
 * - getPosition: 1
 * - getBearing: 1
 * - getSize: 1
 * - stillFloating: 3
 * - getTopMostPos: 3
 * - getBottomMostPos: 3
 * - getLeftMostPos: 3
 * - getRightMostPos: 3
 * - occupies: 4
 * - tooCloseTo (IShip): 4
 * - tooCloseTo (IPosition): 4
 * - isInsideBoard: 5
 * - shoot: 5
 * - sink: 2
 * - toString: 1
 */
public class ShipTest {

    private Ship ship, shipBorder;

    @BeforeEach
    void setUp() {
        // Since Ship is abstract, instantiate it with a concrete subclass (e.g., Barge)
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
    }

    @AfterEach
    void tearDown() {
        ship = null;
        shipBorder = null;
    }

    // ------------------------------------------------------------------
    // Auxiliares (adicionados)
    // ------------------------------------------------------------------

    /**
     * Navio de teste com posições "fora de ordem": (5,5), (4,4) e (6,6).
     * Os navios reais acrescentam sempre as posições por ordem crescente de linha/coluna,
     * por isso nunca entrariam nos "if" dos métodos getTopMostPos, getLeftMostPos, etc.
     * Como a Ship é abstrata, é criada como subclasse anónima.
     */
    private static Ship unorderedShip() {
        Ship s = new Ship("Teste", Compass.NORTH, new Position(5, 5), 3) { };
        s.getPositions().add(new Position(5, 5));
        s.getPositions().add(new Position(4, 4));
        s.getPositions().add(new Position(6, 6));
        return s;
    }

    // ------------------------------------------------------------------
    // Testes originais (só foi acrescentado o @DisplayName e corrigido o tearDown)
    // ------------------------------------------------------------------

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("Construtor: uma barca é criada com categoria, orientação, tamanho e posições corretos")
    void testConstructor() {
        assertNotNull(ship, "Error: Ship instance should not be null.");
        assertEquals("Barca", ship.getCategory(), "Error: Ship category is incorrect.");
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing is incorrect.");
        assertEquals(1, ship.getSize(), "Error: Ship size is incorrect.");
        assertFalse(ship.getPositions().isEmpty(), "Error: Ship positions should not be empty.");
    }

    /**
     * Test for the getCategory method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getCategory: devolve a categoria do navio")
    void testGetCategory() {
        assertEquals("Barca", ship.getCategory(), "Error: Ship category should be 'Barca'.");
    }

    /**
     * Test for the getSize method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getSize: devolve o número de posições do navio")
    void testGetSize() {
        assertEquals(1, ship.getSize(), "Error: Ship size should be 1.");
    }

    /**
     * Test for the getBearing method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getBearing: devolve a orientação do navio")
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing should be NORTH.");
    }

    /**
     * Test for the getPositions method.
     * Cyclomatic Complexity: 1
     */
    @Test
    @DisplayName("getPositions: devolve as posições ocupadas pelo navio")
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions, "Error: Ship positions should not be null.");
        assertEquals(1, positions.size(), "Error: Ship should have exactly one position.");
        assertEquals(5, positions.get(0).getRow(), "Error: Position's row should be 5.");
        assertEquals(5, positions.get(0).getColumn(), "Error: Position's column should be 5.");
    }

    /**
     * Test for the stillFloating method (all positions intact).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("stillFloating: um navio sem tiros continua a flutuar")
    void testStillFloating1() {
        assertTrue(ship.stillFloating(), "Error: Ship should still be floating.");
    }

    /**
     * Test for the stillFloating method (all positions hit).
     */
    @Test
    @DisplayName("stillFloating: um navio com todas as posições atingidas já não flutua")
    void testStillFloating2() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating(), "Error: Ship should no longer be floating after being hit.");
    }

    /**
     * Test for the shoot method (valid position).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("shoot: um tiro na posição do navio marca-a como atingida")
    void testShoot1() {
        Position target = new Position(5, 5);
        ship.shoot(target);
        assertTrue(ship.getPositions().get(0).isHit(), "Error: Position should be marked as hit.");
    }

    /**
     * Test for the shoot method (invalid position).
     */
    @Test
    @DisplayName("shoot: um tiro noutra posição não atinge o navio")
    void testShoot2() {
        Position target = new Position(0, 0);
        ship.shoot(target); // No exception expected
        assertFalse(ship.getPositions().get(0).isHit(), "Error: Position should not be marked as hit for an invalid target.");
    }

    /**
     * Test for the occupies method (position occupied).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("occupies: o navio ocupa a sua posição")
    void testOccupies1() {
        Position pos = new Position(5, 5);
        assertTrue(ship.occupies(pos), "Error: Ship should occupy position (5, 5).");
    }

    /**
     * Test for the occupies method (position not occupied).
     */
    @Test
    @DisplayName("occupies: o navio não ocupa uma posição diferente")
    void testOccupies2() {
        Position pos = new Position(1, 1);
        assertFalse(ship.occupies(pos), "Error: Ship should not occupy position (1, 1).");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships too close).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("tooCloseTo(IShip): navios encostados estão demasiado perto")
    void testTooCloseToShip1() {
        Ship nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        assertTrue(ship.tooCloseTo(nearbyShip), "Error: Ships should be too close.");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships not close).
     */
    @Test
    @DisplayName("tooCloseTo(IShip): navios afastados não estão demasiado perto")
    void testTooCloseToShip2() {
        Ship farShip = new Barge(Compass.NORTH, new Position(10, 10));
        assertFalse(ship.tooCloseTo(farShip), "Error: Ships should not be too close.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions adjacent).
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("tooCloseTo(IPosition): uma posição adjacente está demasiado perto")
    void testTooCloseToPosition1() {
        Position pos = new Position(5, 6); // Adjacent position
        assertTrue(ship.tooCloseTo(pos), "Error: Ship should be too close to the given position.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions not adjacent).
     */
    @Test
    @DisplayName("tooCloseTo(IPosition): uma posição afastada não está demasiado perto")
    void testTooCloseToPosition2() {
        Position pos = new Position(7, 7); // Non-adjacent position
        assertFalse(ship.tooCloseTo(pos), "Error: Ship should not be too close to the given position.");
    }

    /**
     * Test for the getTopMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getTopMostPos: numa barca, é a linha da sua única posição")
    void testGetTopMostPos() {
        assertEquals(5, ship.getTopMostPos(), "Error: The topmost position should be 5.");
    }

    /**
     * Test for the getBottomMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getBottomMostPos: numa barca, é a linha da sua única posição")
    void testGetBottomMostPos() {
        assertEquals(5, ship.getBottomMostPos(), "Error: The bottommost position should be 5.");
    }

    /**
     * Test for the getLeftMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getLeftMostPos: numa barca, é a coluna da sua única posição")
    void testGetLeftMostPos() {
        assertEquals(5, ship.getLeftMostPos(), "Error: The leftmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getRightMostPos: numa barca, é a coluna da sua única posição")
    void testGetRightMostPos() {
        assertEquals(5, ship.getRightMostPos(), "Error: The rightmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    @DisplayName("getAdjacentPositions: uma barca tem 8 vizinhas no meio do tabuleiro e 3 no canto")
    void testGetAdjacentPositions() {
        assertEquals(8, ship.getAdjacentPositions().size(), "Error: The number of adjacent positions of a barge in the middle of the tray should be 8.");
        assertEquals(3, shipBorder.getAdjacentPositions().size(), "Error: The number of adjacent positions of a barge in the middle of the tray should be 8.");
    }

    // ------------------------------------------------------------------
    // Testes adicionados para maximizar a cobertura (issue #36)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("buildShip: cria o tipo de navio certo para cada nome")
    void testBuildShip1() {
        Position p = new Position(2, 2);
        assertAll(
                () -> assertInstanceOf(Barge.class, Ship.buildShip("barca", Compass.NORTH, p), "Error: expected a Barge for \"barca\""),
                () -> assertInstanceOf(Caravel.class, Ship.buildShip("caravela", Compass.NORTH, p), "Error: expected a Caravel for \"caravela\""),
                () -> assertInstanceOf(Carrack.class, Ship.buildShip("nau", Compass.NORTH, p), "Error: expected a Carrack for \"nau\""),
                () -> assertInstanceOf(Frigate.class, Ship.buildShip("fragata", Compass.NORTH, p), "Error: expected a Frigate for \"fragata\""),
                () -> assertInstanceOf(Galleon.class, Ship.buildShip("galeao", Compass.NORTH, p), "Error: expected a Galleon for \"galeao\"")
        );
    }

    @Test
    @DisplayName("buildShip: um nome desconhecido devolve null")
    void testBuildShip2() {
        Ship s = Ship.buildShip("submarino", Compass.NORTH, new Position(2, 2));
        assertNull(s, "Error: expected null for an unknown ship kind but got " + s);
    }

    @Test
    @DisplayName("buildShip: com algum argumento null, o assert falha (AssertionError)")
    void testBuildShipNull() {
        Position p = new Position(2, 2);
        assertAll(
                () -> assertThrows(AssertionError.class, () -> Ship.buildShip(null, Compass.NORTH, p), "Error: expected AssertionError for a null kind"),
                () -> assertThrows(AssertionError.class, () -> Ship.buildShip("barca", null, p), "Error: expected AssertionError for a null bearing"),
                () -> assertThrows(AssertionError.class, () -> Ship.buildShip("barca", Compass.NORTH, null), "Error: expected AssertionError for a null position")
        );
    }

    @Test
    @DisplayName("Construtor: lança NullPointerException se a categoria, a orientação ou a posição forem null")
    void testConstructorNull() {
        Position p = new Position(2, 2);
        assertAll(
                () -> assertThrows(NullPointerException.class, () -> new Ship(null, Compass.NORTH, p, 1) { }, "Error: expected NullPointerException for a null category"),
                () -> assertThrows(NullPointerException.class, () -> new Ship("Teste", null, p, 1) { }, "Error: expected NullPointerException for a null bearing"),
                () -> assertThrows(NullPointerException.class, () -> new Ship("Teste", Compass.NORTH, null, 1) { }, "Error: expected NullPointerException for a null position")
        );
    }

    @Test
    @DisplayName("getPosition: devolve a posição inicial do navio")
    void testGetPosition() {
        assertEquals(new Position(5, 5), ship.getPosition(), "Error: expected position (5,5) but got " + ship.getPosition());
    }

    @Test
    @DisplayName("getAdjacentPositions: numa caravela, não inclui as posições do próprio navio nem repete vizinhas")
    void testGetAdjacentPositions2() {
        // Caravela vertical em (5,5)-(6,5): a "caixa" à volta tem 4 x 3 = 12 posições, menos as 2 do navio
        Ship caravel = new Caravel(Compass.NORTH, new Position(5, 5));
        List<IPosition> adj = caravel.getAdjacentPositions();
        assertAll(
                () -> assertEquals(10, adj.size(), "Error: expected 10 adjacent positions but got " + adj.size()),
                () -> assertFalse(adj.contains(new Position(5, 5)), "Error: did not expect the ship's own position (5,5)"),
                () -> assertFalse(adj.contains(new Position(6, 5)), "Error: did not expect the ship's own position (6,5)"),
                () -> assertEquals(adj.size(), new HashSet<>(adj).size(), "Error: expected no repeated positions in " + adj)
        );
    }

    @Test
    @DisplayName("stillFloating: um navio com só algumas posições atingidas continua a flutuar")
    void testStillFloating3() {
        Ship caravel = new Caravel(Compass.NORTH, new Position(5, 5));
        caravel.getPositions().get(0).shoot();
        assertTrue(caravel.stillFloating(), "Error: expected a partially hit ship to still be floating");
    }

    @Test
    @DisplayName("getTopMostPos / getBottomMostPos: com posições fora de ordem, encontra a linha mínima e a máxima")
    void testGetTopAndBottomMostPos2() {
        Ship s = unorderedShip();
        assertAll(
                () -> assertEquals(4, s.getTopMostPos(), "Error: expected top row 4 but got " + s.getTopMostPos()),
                () -> assertEquals(6, s.getBottomMostPos(), "Error: expected bottom row 6 but got " + s.getBottomMostPos())
        );
    }

    @Test
    @DisplayName("getLeftMostPos / getRightMostPos: com posições fora de ordem, encontra a coluna mínima e a máxima")
    void testGetLeftAndRightMostPos2() {
        Ship s = unorderedShip();
        assertAll(
                () -> assertEquals(4, s.getLeftMostPos(), "Error: expected left column 4 but got " + s.getLeftMostPos()),
                () -> assertEquals(6, s.getRightMostPos(), "Error: expected right column 6 but got " + s.getRightMostPos())
        );
    }

    @Test
    @DisplayName("occupies: com uma posição null, o assert falha (AssertionError)")
    void testOccupiesNull() {
        assertThrows(AssertionError.class, () -> ship.occupies(null), "Error: expected AssertionError for a null position");
    }

    @Test
    @DisplayName("tooCloseTo(IShip): com um navio null, o assert falha (AssertionError)")
    void testTooCloseToShipNull() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IShip) null), "Error: expected AssertionError for a null ship");
    }

    @Test
    @DisplayName("tooCloseTo(IPosition): com uma posição null, o assert falha (AssertionError)")
    void testTooCloseToPositionNull() {
        assertThrows(AssertionError.class, () -> ship.tooCloseTo((IPosition) null), "Error: expected AssertionError for a null position");
    }

    @Test
    @DisplayName("isInsideBoard: um navio dentro do tabuleiro está dentro")
    void testIsInsideBoard1() {
        assertAll(
                () -> assertTrue(ship.isInsideBoard(), "Error: expected (5,5) to be inside the board"),
                () -> assertTrue(new Barge(Compass.NORTH, new Position(9, 9)).isInsideBoard(), "Error: expected (9,9) to be inside the board")
        );
    }

    @Test
    @DisplayName("isInsideBoard: um navio que sai por qualquer um dos 4 lados está fora")
    void testIsInsideBoard2() {
        assertAll(
                () -> assertFalse(new Barge(Compass.NORTH, new Position(0, -1)).isInsideBoard(), "Error: expected column -1 to be outside (left)"),
                () -> assertFalse(new Barge(Compass.NORTH, new Position(0, 10)).isInsideBoard(), "Error: expected column 10 to be outside (right)"),
                () -> assertFalse(new Barge(Compass.NORTH, new Position(-1, 0)).isInsideBoard(), "Error: expected row -1 to be outside (top)"),
                () -> assertFalse(new Barge(Compass.NORTH, new Position(10, 0)).isInsideBoard(), "Error: expected row 10 to be outside (bottom)")
        );
    }

    @Test
    @DisplayName("shoot: com uma posição null ou fora do tabuleiro, o assert falha (AssertionError)")
    void testShootInvalid() {
        assertAll(
                () -> assertThrows(AssertionError.class, () -> ship.shoot(null), "Error: expected AssertionError for a null position"),
                () -> assertThrows(AssertionError.class, () -> ship.shoot(new Position(10, 10)), "Error: expected AssertionError for a position outside the board")
        );
    }

    @Test
    @DisplayName("sink: afunda o navio, atingindo todas as suas posições")
    void testSink() {
        Ship caravel = new Caravel(Compass.NORTH, new Position(5, 5));
        caravel.sink();
        assertAll(
                () -> assertTrue(caravel.getPositions().get(0).isHit(), "Error: expected position (5,5) to be hit"),
                () -> assertTrue(caravel.getPositions().get(1).isHit(), "Error: expected position (6,5) to be hit"),
                () -> assertFalse(caravel.stillFloating(), "Error: expected the ship to be sunk")
        );
    }

    @Test
    @DisplayName("toString: mostra a categoria, a orientação e a posição do navio")
    void testToString() {
        String expected = "[Barca " + Compass.NORTH + " " + new Position(5, 5) + "]";
        assertEquals(expected, ship.toString(), "Error: expected " + expected + " but got " + ship);
    }
}