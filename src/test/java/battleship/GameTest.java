package battleship;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Scanner;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet
	}

	@AfterEach
	void tearDown() {
		game = null;
	}

	@Test
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}


	@Test
	@DisplayName("Test getMyFleet returns the player's fleet")
	void getMyFleet() {
		assertNotNull(game.getMyFleet(),
				"Error: player's fleet should not be null.");
	}

	@Test
	@DisplayName("Test getAlienFleet returns the enemy's fleet")
	void getAlienFleet() {
		assertNotNull(game.getAlienFleet(),
				"Error: enemy fleet should not be null.");
	}

	@Test
	@DisplayName("Test getMyMoves returns an empty list initially")
	void getMyMoves() {
		assertNotNull(game.getMyMoves(),
				"Error: player's moves list should not be null.");

		assertTrue(game.getMyMoves().isEmpty(),
				"Error: player's moves list should be empty initially.");
	}


	@Test
	@DisplayName("Test firing at water")
	void fireSingleShot1() {
		Position position = new Position(0, 0);

		game.fireSingleShot(position, false);

		assertEquals(0, game.getHits(),
				"Error: hits counter should remain zero.");

		assertEquals(0, game.getSunkShips(),
				"Error: sunk ships counter should remain zero.");
	}


	@Test
	@DisplayName("Test hitting a ship without sinking it")
	void fireSingleShot2() {
		Ship ship = new Frigate(Compass.NORTH, new Position(5, 5));
		game.getMyFleet().addShip(ship);

		IPosition position = ship.getPositions().get(0);

		game.fireSingleShot(position, false);

		assertEquals(1, game.getHits(),
				"Error: hits counter should increase to one.");

		assertEquals(0, game.getSunkShips(),
				"Error: ship should not be sunk after one hit.");

		assertTrue(ship.stillFloating(),
				"Error: ship should still be floating.");
	}


	@Test
	@DisplayName("Test sinking a ship")
	void fireSingleShot3() {
		Ship ship = new Barge(Compass.NORTH, new Position(1, 1));
		game.getMyFleet().addShip(ship);

		for (IPosition position : ship.getPositions()) {
			game.fireSingleShot(position, false);
		}

		assertFalse(ship.stillFloating(),
				"Error: ship should be sunk.");

		assertEquals(1, game.getSunkShips(),
				"Error: sunk ships counter should be one.");

		assertEquals(0, game.getRemainingShips(),
				"Error: no ships should remain floating.");
	}



	@Test
	@DisplayName("Test incomplete enemy fire position")
	void readEnemyFire1() {
		Scanner scanner = new Scanner("A");

		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(scanner),
				"Error: incomplete position should throw IllegalArgumentException.");
	}


	@Test
	@DisplayName("Test insufficient enemy fire positions")
	void readEnemyFire2() {
		Scanner scanner = new Scanner("A1 B2");

		assertThrows(IllegalArgumentException.class,
				() -> game.readEnemyFire(scanner),
				"Error: fewer than three positions should throw IllegalArgumentException.");
	}


	@Test
	@DisplayName("Test reading enemy fire with separate coordinates")
	void readEnemyFire3() {
		Scanner scanner = new Scanner("A 1 B 2 C 3");

		String result = game.readEnemyFire(scanner);

		assertNotNull(result,
				"Error: JSON result should not be null.");

		assertEquals(1, game.getAlienMoves().size(),
				"Error: one enemy move should be registered.");
	}


	@Test
	@DisplayName("Test reading enemy fire with combined coordinates")
	void readEnemyFire4() {
		Scanner scanner = new Scanner("A1 B2 C3");

		String result = game.readEnemyFire(scanner);

		assertNotNull(result,
				"Error: JSON result should not be null.");

		assertEquals(1, game.getAlienMoves().size(),
				"Error: one enemy move should be registered.");
	}


	@Test
	@DisplayName("Test firing with fewer than three shots")
	void fireShots1() {
		List<IPosition> shots = List.of(
				new Position(1, 1),
				new Position(2, 2)
		);

		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(shots),
				"Error: firing fewer than three shots should throw IllegalArgumentException.");
	}


	@Test
	@DisplayName("Test firing repeated positions in the same move")
	void fireShots2() {
		List<IPosition> shots = List.of(
				new Position(1, 1),
				new Position(1, 1),
				new Position(2, 2)
		);

		game.fireShots(shots);

		assertEquals(1, game.getRepeatedShots(),
				"Error: repeated shots counter should increase to one.");

		assertEquals(1, game.getAlienMoves().size(),
				"Error: one move should be registered.");
	}

	@Test
	@DisplayName("Test random enemy fire")
	void randomEnemyFire() {
		String result = game.randomEnemyFire();

		assertNotNull(result,
				"Error: random enemy fire should return a JSON result.");

		assertEquals(1, game.getAlienMoves().size(),
				"Error: one enemy move should be registered.");

		assertEquals(3, game.getAlienMoves().get(0).getShots().size(),
				"Error: enemy move should contain three shots.");
	}

	@Test
	@DisplayName("Test over method")
	void over() {
		assertDoesNotThrow(() -> game.over(),
				"Error: over method should execute without throwing an exception.");
	}

	@Test
	@DisplayName("Test jsonShots with an empty list")
	void jsonShots1() {
		String json = Game.jsonShots(List.of());

		assertEquals("[]", json.replaceAll("\\s+", ""),
				"Error: expected empty JSON array.");
	}

	@Test
	@DisplayName("Test printMyBoard")
	void printMyBoard() {
		assertDoesNotThrow(() -> game.printMyBoard(false, false));
	}

	@Test
	@DisplayName("Test printAlienBoard")
	void printAlienBoard() {
		assertDoesNotThrow(() -> game.printAlienBoard(false, false));
	}

	@Test
	@DisplayName("Test printBoard with shots and legend")
	void printBoard1() {
		assertDoesNotThrow(() ->
				BoardPrinter.printBoard(game.getMyFleet(), List.of(), true, true));
	}

	@Test
	@DisplayName("Test printBoard without shots or legend")
	void printBoard2() {
		assertDoesNotThrow(() ->
				BoardPrinter.printBoard(game.getMyFleet(), List.of(), false, false));
	}

	@Test
	@DisplayName("Test printBoard with shots but without legend")
	void printBoard3() {
		assertDoesNotThrow(() ->
				BoardPrinter.printBoard(game.getMyFleet(), List.of(), true, false));
	}

	@Test
	@DisplayName("Test printBoard with legend but without shots")
	void printBoard4() {
		assertDoesNotThrow(() ->
				BoardPrinter.printBoard(game.getMyFleet(), List.of(), false, true));
	}

	@Test
	@DisplayName("Test jsonShots with positions")
	void jsonShots2() {
		List<IPosition> shots = List.of(
				new Position(1, 1),
				new Position(2, 2),
				new Position(3, 3)
		);

		String json = Game.jsonShots(shots);

		assertNotNull(json,
				"Error: JSON result should not be null.");

		assertFalse(json.isBlank(),
				"Error: JSON result should not be empty.");
	}

	@Test
	@DisplayName("Test printBoard with a ship")
	void printBoard5() {
		IFleet fleet = new Fleet();
		Ship ship = new Barge(Compass.NORTH, new Position(2, 2));

		fleet.addShip(ship);

		assertDoesNotThrow(() ->
				BoardPrinter.printBoard(fleet, List.of(), false, false));
	}

	@Test
	@DisplayName("Test printBoard with shots and a sunk ship")
	void printBoard6() {
		Ship ship = new Barge(Compass.NORTH, new Position(2, 2));
		game.getMyFleet().addShip(ship);

		IPosition shipPosition = ship.getPositions().get(0);
		IPosition adjacentPosition = ship.getAdjacentPositions().get(0);

		game.fireShots(List.of(
				shipPosition,
				adjacentPosition,
				new Position(9, 9)
		));

		ship.sink();

		assertDoesNotThrow(() ->
				BoardPrinter.printBoard(
						game.getMyFleet(),
						game.getAlienMoves(),
						true,
						true
				));
	}

	@Test
	@DisplayName("Test randomEnemyFire when fewer than three positions are available")
	void randomEnemyFire2() {
		List<IPosition> positions = new ArrayList<>();

		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			for (int col = 0; col < Game.BOARD_SIZE; col++) {
				if (row != 9 || col != 9) {
					positions.add(new Position(row, col));
				}
			}
		}

		for (int i = 0; i < positions.size(); i += 3) {
			game.fireShots(new ArrayList<>(
					positions.subList(i, i + 3)
			));
		}

		String result = game.randomEnemyFire();

		assertNotNull(result,
				"Error: random enemy fire should return a JSON result.");

		assertEquals(34, game.getAlienMoves().size(),
				"Error: 34 enemy moves should be registered.");

		assertEquals(3, game.getAlienMoves().get(33).getShots().size(),
				"Error: the final move should contain three shots.");
	}

	@Test
	@DisplayName("Test randomEnemyFire with two available positions")
	void randomEnemyFire3() {
		List<IPosition> positions = new ArrayList<>();

		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			for (int col = 0; col < Game.BOARD_SIZE; col++) {
				if (!((row == 9 && col == 8) ||
						(row == 9 && col == 9))) {
					positions.add(new Position(row, col));
				}
			}
		}

		// Repetir uma posição para completar os 99 tiros
		positions.add(new Position(0, 0));

		for (int i = 0; i < positions.size(); i += 3) {
			game.fireShots(new ArrayList<>(
					positions.subList(i, i + 3)
			));
		}

		String result = game.randomEnemyFire();

		assertNotNull(result);
		assertEquals(34, game.getAlienMoves().size());
		assertEquals(3, game.getAlienMoves().get(33).getShots().size());
	}
}