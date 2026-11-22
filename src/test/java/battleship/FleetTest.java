package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for Fleet.
 * Author: Beatriz
 * Date: 2026-11-20 04:30 (testes originais completados para cobertura máxima)
 * Cyclomatic Complexity for each method (contando cada assert, &&, for e if):
 * - Constructor: 1
 * - createRandom: 4
 * - getShips: 1
 * - addShip: 5
 * - getShipsLike: 4
 * - getFloatingShips: 3
 * - getSunkShips: 3
 * - shipAt: 4
 * - collisionRisk (privado): 4
 * - printShips: 3
 * - printStatus: 1
 * - printShipsByCategory: 2
 * - printFloatingShips: 1
 * - printAllShips: 1
 */
public class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@AfterEach
	void tearDown() {
		fleet = null;
	}

	// ------------------------------------------------------------------
	// Auxiliares (adicionados)
	// ------------------------------------------------------------------

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

	/** Afunda um navio, disparando sobre todas as suas posições. */
	private static void sink(IShip ship) {
		for (IPosition p : ship.getPositions())
			p.shoot();
	}

	// ------------------------------------------------------------------
	// Testes originais (só foi acrescentado o @DisplayName)
	// ------------------------------------------------------------------

	/**
	 * Test for the Fleet constructor.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	@DisplayName("Construtor: a frota começa sem navios")
	void testConstructor() {
		assertNotNull(fleet, "Error: Instance of Fleet should not be null.");
		assertTrue(fleet.getShips().isEmpty(), "Error: Fleet should be initialized with empty ships list.");
	}

	@Test
	@DisplayName("createRandom: cria uma frota aleatória com o número certo de navios")
	void testCreateRandom(){
		IFleet r = Fleet.createRandom();
		assertNotNull(r, "Error: createRandom should return a non-null fleet instance.");
		assertEquals(Fleet.FLEET_SIZE, r.getShips().size(), "Error: createRandom should create a fleet with the correct number of ships.");
	}

	/**
	 * Test for the addShip method (all conditions true).
	 * Cyclomatic Complexity: 3
	 */
	@Test
	@DisplayName("addShip: um navio válido é adicionado à frota")
	void testAddShip1() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully.");
		assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.");
	}

	/**
	 * Test for the addShip method (fleet size limit reached).
	 */
	@Test
	@DisplayName("addShip: navio fora do tabuleiro (10,10) não é adicionado (teste original)")
	void testAddShip2() {
		for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
			fleet.addShip(new Barge(Compass.NORTH, new Position(i, 0)));
		}
		IShip anotherShip = new Barge(Compass.NORTH, new Position(10, 10));
		assertFalse(fleet.addShip(anotherShip), "Error: Should not add ship when fleet size limit is reached.");
	}

	/**
	 * Test for the addShip method (ship outside the board).
	 */
	@Test
	@DisplayName("addShip: navio fora do tabuleiro não é adicionado")
	void testAddShip3() {
		IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
		assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
	}

	/**
	 * Test for the addShip method (collision risk).
	 */
	@Test
	@DisplayName("addShip: navio em colisão com outro não é adicionado")
	void testAddShip4() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
		fleet.addShip(ship1);
		assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
	}

	/**
	 * Test for the getShips method.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	@DisplayName("getShips: devolve a lista de navios da frota")
	void testGetShips() {
		assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);
		assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship.");
		assertEquals(ship, fleet.getShips().get(0), "Error: Fleet's first ship should match the added ship.");
	}

	/**
	 * Test for the getShipsLike method (ships of specific category).
	 * Cyclomatic Complexity: 2
	 */
	@Test
	@DisplayName("getShipsLike: devolve só os navios da categoria pedida")
	void testGetShipsLike() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Caravel(Compass.NORTH, new Position(2, 1));
		fleet.addShip(ship1);
		fleet.addShip(ship2);

		List<IShip> barges = fleet.getShipsLike("Barca");
		assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'.");
		assertEquals(ship1, barges.get(0), "Error: The ship of category 'Barca' does not match.");
	}

	/**
	 * Test for the getFloatingShips method.
	 * Cyclomatic Complexity: 2
	 */
	@Test
	@DisplayName("getFloatingShips: devolve só os navios que ainda flutuam")
	void testGetFloatingShips() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(ship1);
		fleet.addShip(ship2);

		List<IShip> floatingShips = fleet.getFloatingShips();
		assertEquals(2, floatingShips.size(), "Error: All ships should be floating initially.");

		ship1.getPositions().get(0).shoot();  // Sink ship1
		floatingShips = fleet.getFloatingShips();
		assertEquals(1, floatingShips.size(), "Error: Only one ship should be floating after sinking one.");
		assertEquals(ship2, floatingShips.get(0), "Error: The floating ship should match the expected result.");
	}

	/**
	 * Test for the shipAt method.
	 * Cyclomatic Complexity: 2
	 */
	@Test
	@DisplayName("shipAt: devolve o navio numa posição ocupada e null numa posição livre")
	void testShipAt() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the correct ship at the position.");
		assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty positions in the fleet.");
	}


	/**
	 * Test for private method collisionRisk.
	 * Cyclomatic Complexity: 2
	 */
	@Test
	@DisplayName("collisionRisk: deteta risco de colisão com navios sobrepostos e não com navios afastados")
	void testColisionRisk() throws Exception {
		var method = Fleet.class.getDeclaredMethod("collisionRisk", IShip.class);
		method.setAccessible(true);

		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
		fleet.addShip(ship1);

		assertTrue((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should be at collision risk.");
		assertFalse((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
				"Error: Ships at non-overlapping positions should not have a collision risk.");
	}

	/**
	 * Test for the printStatus method.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	@DisplayName("printStatus: não lança exceções")
	void testPrintStatus() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);
		assertDoesNotThrow(fleet::printStatus, "Error: printStatus should not throw any exceptions.");
	}

	// ------------------------------------------------------------------
	// Testes adicionados para maximizar a cobertura (issue #36)
	// ------------------------------------------------------------------

	@Test
	@DisplayName("addShip: com a frota cheia, o navio seguinte é rejeitado (limite de tamanho)")
	void testAddShip5() {
		// 13 barcas afastadas umas das outras (linhas e colunas pares), todas dentro do tabuleiro
		int[][] spots = {{0, 0}, {0, 2}, {0, 4}, {0, 6}, {0, 8}, {2, 0}, {2, 2},
				{2, 4}, {2, 6}, {2, 8}, {4, 0}, {4, 2}, {8, 8}};
		int added = 0;
		boolean lastResult = true;
		for (int[] s : spots) {
			lastResult = fleet.addShip(new Barge(Compass.NORTH, new Position(s[0], s[1])));
			if (lastResult)
				added++;
		}
		int finalAdded = added;
		boolean finalLast = lastResult;
		// Nota: a condição é "size <= FLEET_SIZE", por isso a frota aceita FLEET_SIZE + 1 (12) navios
		// antes de recusar. Este teste regista o comportamento atual do código.
		assertAll(
				() -> assertFalse(finalLast, "Error: expected the 13th ship to be rejected but it was added"),
				() -> assertEquals(Fleet.FLEET_SIZE + 1, finalAdded, "Error: expected " + (Fleet.FLEET_SIZE + 1) + " ships added before the limit but got " + finalAdded),
				() -> assertEquals(Fleet.FLEET_SIZE + 1, fleet.getShips().size(), "Error: expected the fleet to keep " + (Fleet.FLEET_SIZE + 1) + " ships but got " + fleet.getShips().size())
		);
	}

	@Test
	@DisplayName("addShip: com um navio null, o assert falha (AssertionError)")
	void testAddShipNull() {
		assertThrows(AssertionError.class, () -> fleet.addShip(null),
				"Error: expected AssertionError for a null ship");
	}

	@Test
	@DisplayName("getShipsLike: com uma categoria null, o assert falha (AssertionError)")
	void testGetShipsLikeNull() {
		assertThrows(AssertionError.class, () -> fleet.getShipsLike(null),
				"Error: expected AssertionError for a null category");
	}

	@Test
	@DisplayName("getSunkShips: numa frota vazia não há navios afundados")
	void testGetSunkShips1() {
		assertTrue(fleet.getSunkShips().isEmpty(), "Error: expected no sunk ships in an empty fleet but got " + fleet.getSunkShips());
	}

	@Test
	@DisplayName("getSunkShips: com todos os navios a flutuar, não há navios afundados")
	void testGetSunkShips2() {
		fleet.addShip(new Barge(Compass.NORTH, new Position(1, 1)));
		fleet.addShip(new Caravel(Compass.NORTH, new Position(4, 4)));
		assertTrue(fleet.getSunkShips().isEmpty(), "Error: expected no sunk ships but got " + fleet.getSunkShips());
	}

	@Test
	@DisplayName("getSunkShips: devolve só os navios que foram afundados")
	void testGetSunkShips3() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(barge);
		fleet.addShip(caravel);
		sink(barge);

		List<IShip> sunk = fleet.getSunkShips();
		assertAll(
				() -> assertEquals(1, sunk.size(), "Error: expected 1 sunk ship but got " + sunk.size()),
				() -> assertEquals(barge, sunk.get(0), "Error: expected the barge to be the sunk ship but got " + sunk.get(0))
		);
	}

	@Test
	@DisplayName("shipAt: com uma posição null, o assert falha (AssertionError)")
	void testShipAtNull() {
		assertThrows(AssertionError.class, () -> fleet.shipAt(null),
				"Error: expected AssertionError for a null position");
	}

	@Test
	@DisplayName("collisionRisk: com um navio null, o assert falha (AssertionError)")
	void testColisionRiskNull() throws Exception {
		Method method = Fleet.class.getDeclaredMethod("collisionRisk", IShip.class);
		method.setAccessible(true);
		// Por reflexão, a exceção vem "embrulhada" numa InvocationTargetException
		InvocationTargetException e = assertThrows(InvocationTargetException.class,
				() -> method.invoke(fleet, (IShip) null),
				"Error: expected the call to fail for a null ship");
		assertInstanceOf(AssertionError.class, e.getCause(),
				"Error: expected an AssertionError but got " + e.getCause());
	}

	@Test
	@DisplayName("printShips: com uma lista vazia, não escreve nada")
	void testPrintShips1() {
		String out = captureOut(() -> fleet.printShips(new ArrayList<>()));
		assertEquals("", out, "Error: expected no output for an empty list but got: " + out);
	}

	@Test
	@DisplayName("printShips: escreve uma linha por cada navio da lista")
	void testPrintShips2() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		String out = captureOut(() -> fleet.printShips(List.of(barge, caravel)));
		assertAll(
				() -> assertTrue(out.contains(barge.toString()), "Error: expected " + barge + " in the output but got: " + out),
				() -> assertTrue(out.contains(caravel.toString()), "Error: expected " + caravel + " in the output but got: " + out),
				() -> assertEquals(2, out.lines().count(), "Error: expected 2 lines but got " + out.lines().count())
		);
	}

	@Test
	@DisplayName("printShips: com uma lista null, o assert falha (AssertionError)")
	void testPrintShipsNull() {
		assertThrows(AssertionError.class, () -> fleet.printShips(null),
				"Error: expected AssertionError for a null list");
	}

	@Test
	@DisplayName("printStatus: mostra quantos navios flutuam e quantos estão afundados")
	void testPrintStatus2() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(barge);
		fleet.addShip(new Caravel(Compass.NORTH, new Position(4, 4)));
		sink(barge);
		String out = captureOut(fleet::printStatus);
		assertTrue(out.contains("Estado da Frota: 1 a flutuar, 1 afundados!"),
				"Error: expected \"1 a flutuar, 1 afundados\" but got: " + out);
	}

	@Test
	@DisplayName("printShipsByCategory: escreve só os navios da categoria pedida")
	void testPrintShipsByCategory() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(barge);
		fleet.addShip(caravel);
		String out = captureOut(() -> fleet.printShipsByCategory("Caravela"));
		assertAll(
				() -> assertTrue(out.contains(caravel.toString()), "Error: expected the caravel in the output but got: " + out),
				() -> assertFalse(out.contains(barge.toString()), "Error: did not expect the barge in the output but got: " + out)
		);
	}

	@Test
	@DisplayName("printShipsByCategory: com uma categoria null, o assert falha (AssertionError)")
	void testPrintShipsByCategoryNull() {
		assertThrows(AssertionError.class, () -> fleet.printShipsByCategory(null),
				"Error: expected AssertionError for a null category");
	}

	@Test
	@DisplayName("printFloatingShips: escreve só os navios que ainda flutuam")
	void testPrintFloatingShips() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(barge);
		fleet.addShip(caravel);
		sink(barge);
		String out = captureOut(fleet::printFloatingShips);
		assertAll(
				() -> assertTrue(out.contains(caravel.toString()), "Error: expected the floating caravel in the output but got: " + out),
				() -> assertFalse(out.contains(barge.toString()), "Error: did not expect the sunk barge in the output but got: " + out)
		);
	}

	@Test
	@DisplayName("printAllShips: escreve todos os navios, a flutuar ou afundados")
	void testPrintAllShips() {
		IShip barge = new Barge(Compass.NORTH, new Position(1, 1));
		IShip caravel = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(barge);
		fleet.addShip(caravel);
		sink(barge);
		String out = captureOut(fleet::printAllShips);
		assertAll(
				() -> assertTrue(out.contains(barge.toString()), "Error: expected the barge in the output but got: " + out),
				() -> assertTrue(out.contains(caravel.toString()), "Error: expected the caravel in the output but got: " + out)
		);
	}
}