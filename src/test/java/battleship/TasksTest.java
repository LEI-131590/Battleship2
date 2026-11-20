package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Tasks.
 * Author: Lara Fernandes
 * Date: 2026-11-20 12:25
 * Cyclomatic Complexity:
 * - Tasks(): 1
 * - menuHelp(): 1
 * - readPosition(): 2
 * - readShip(): 2
 * - readClassicPosition(): 4 (ramo else-if é inalcançável pois o primeiro if já captura [A-Z]\d+)
 * - buildFleet(): 4
 * - menu(): 13 (ramos GUI de 'mapa', 'rajada' com vitória e loop com Thread.sleep de 'simula' omitidos para evitar bloqueio)
 */
class TasksTest {

	private Tasks tasks;
	private InputStream originalIn;
	private PrintStream originalOut;

	/** Input válido com os 11 navios da frota sem colisões nem adjacências. */
	private static final String VALID_FLEET_INPUT =
			"galeao 0 0 n\n" +
					"fragata 0 5 e\n" +
					"nau 2 5 e\n" +
					"nau 4 0 e\n" +
					"caravela 4 5 e\n" +
					"caravela 4 8 e\n" +
					"caravela 6 0 e\n" +
					"barca 6 4 n\n" +
					"barca 6 6 n\n" +
					"barca 6 8 n\n" +
					"barca 8 0 n\n";

	@BeforeEach
	void setUp() {
		tasks = new Tasks();
		originalIn = System.in;
		originalOut = System.out;
	}

	@AfterEach
	void tearDown() throws Exception {
		tasks = null;
		System.setIn(originalIn);
		System.setOut(originalOut);
		Files.deleteIfExists(Paths.get("relatorio-battleship.pdf"));
	}

	// ------------------------------------------------------------------
	// Auxiliares
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

	/** Executa Tasks.menu() com o input simulado e devolve o output da consola. */
	private String runMenuWithInput(String input) {
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
		return captureOut(Tasks::menu);
	}

	// ------------------------------------------------------------------
	// Construtor
	// ------------------------------------------------------------------

	@Test
	@DisplayName("Tasks(): o construtor por omissão cria uma instância não nula")
	void constructor() {
		assertNotNull(tasks, "Error: expected a Tasks instance but got null");
	}

	// ------------------------------------------------------------------
	// menuHelp()
	// ------------------------------------------------------------------

	@Test
	@DisplayName("menuHelp: imprime todos os comandos de ajuda na consola")
	void menuHelp() {
		String out = captureOut(Tasks::menuHelp);
		assertAll(
				() -> assertTrue(out.contains("AJUDA DO MENU"), "Error: expected menu help header but got:\n" + out),
				() -> assertTrue(out.contains("gerafrota"), "Error: expected 'gerafrota' command in help"),
				() -> assertTrue(out.contains("lefrota"), "Error: expected 'lefrota' command in help"),
				() -> assertTrue(out.contains("desisto"), "Error: expected 'desisto' command in help"),
				() -> assertTrue(out.contains("pdf"), "Error: expected 'pdf' command in help")
		);
	}

	// ------------------------------------------------------------------
	// readPosition()
	// ------------------------------------------------------------------

	@Test
	@DisplayName("readPosition: lê corretamente linha e coluna de um Scanner válido")
	void readPosition1() {
		Scanner in = new Scanner("3 7");
		Position pos = Tasks.readPosition(in);
		assertAll(
				() -> assertNotNull(pos, "Error: expected non-null Position"),
				() -> assertEquals(3, pos.getRow(), "Error: expected row 3 but got " + pos.getRow()),
				() -> assertEquals(7, pos.getColumn(), "Error: expected column 7 but got " + pos.getColumn())
		);
	}

	@Test
	@DisplayName("readPosition: lança AssertionError quando o Scanner é null")
	void readPosition2() {
		assertThrows(AssertionError.class, () -> Tasks.readPosition(null),
				"Error: expected AssertionError when Scanner is null");
	}

	// ------------------------------------------------------------------
	// readShip()
	// ------------------------------------------------------------------

	@Test
	@DisplayName("readShip: cria corretamente um navio válido a partir do Scanner")
	void readShip1() {
		Scanner in = new Scanner("barca 2 4 n");
		Ship ship = Tasks.readShip(in);
		assertAll(
				() -> assertNotNull(ship, "Error: expected non-null Ship for valid input"),
				() -> assertEquals("Barca", ship.getCategory(), "Error: expected category 'Barca' but got " + ship.getCategory()),
				() -> assertEquals(Compass.NORTH, ship.getBearing(), "Error: expected NORTH bearing but got " + ship.getBearing())
		);
	}

	@Test
	@DisplayName("readShip: lança AssertionError quando o Scanner é null")
	void readShip2() {
		assertThrows(AssertionError.class, () -> Tasks.readShip(null),
				"Error: expected AssertionError when Scanner is null");
	}

	// ------------------------------------------------------------------
	// readClassicPosition()
	// ------------------------------------------------------------------

	@Test
	@DisplayName("readClassicPosition: lê corretamente uma posição em formato compacto (ex: A3)")
	void readClassicPosition1() {
		Scanner in = new Scanner("a3");
		IPosition pos = Tasks.readClassicPosition(in);
		assertAll(
				() -> assertNotNull(pos, "Error: expected non-null Position for 'a3'"),
				() -> assertEquals(new Position('A', 3), pos, "Error: expected Position('A', 3) but got " + pos)
		);
	}

	@Test
	@DisplayName("readClassicPosition: lê corretamente uma posição com espaço (ex: B 5)")
	void readClassicPosition2() {
		Scanner in = new Scanner("B 5");
		IPosition pos = Tasks.readClassicPosition(in);
		assertAll(
				() -> assertNotNull(pos, "Error: expected non-null Position for 'B 5'"),
				() -> assertEquals(new Position('B', 5), pos, "Error: expected Position('B', 5) but got " + pos)
		);
	}

	@Test
	@DisplayName("readClassicPosition: lança IllegalArgumentException quando o Scanner está vazio")
	void readClassicPosition3() {
		Scanner in = new Scanner("");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(in),
				"Error: expected IllegalArgumentException for empty Scanner");
	}

	@Test
	@DisplayName("readClassicPosition: lança IllegalArgumentException para formato inválido")
	void readClassicPosition4() {
		Scanner in = new Scanner("INVALIDO");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(in),
				"Error: expected IllegalArgumentException for invalid position format");
	}

	// ------------------------------------------------------------------
	// buildFleet()
	// ------------------------------------------------------------------

	@Test
	@DisplayName("buildFleet: constrói uma frota completa com 11 navios válidos")
	void buildFleet1() {
		Scanner in = new Scanner(VALID_FLEET_INPUT);
		Fleet fleet = Tasks.buildFleet(in);
		assertAll(
				() -> assertNotNull(fleet, "Error: expected non-null Fleet"),
				() -> assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
						"Error: expected " + Fleet.FLEET_SIZE + " ships but got " + fleet.getShips().size())
		);
	}

	@Test
	@DisplayName("buildFleet: ignora navios desconhecidos e posições duplicadas até completar a frota")
	void buildFleet2() {
		// Inclui um navio desconhecido (s == null) e uma barca duplicada (addShip == false)
		String inputWithErrors =
				"submarino 0 0 n\n" +
						"barca 6 4 n\n" +
						"barca 6 4 n\n" +
						"galeao 0 0 n\n" +
						"fragata 0 5 e\n" +
						"nau 2 5 e\n" +
						"nau 4 0 e\n" +
						"caravela 4 5 e\n" +
						"caravela 4 8 e\n" +
						"caravela 6 0 e\n" +
						"barca 6 6 n\n" +
						"barca 6 8 n\n" +
						"barca 8 0 n\n";

		Scanner in = new Scanner(inputWithErrors);
		Fleet fleet = Tasks.buildFleet(in);
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size(),
				"Error: expected full fleet despite invalid entries, but got " + fleet.getShips().size());
	}

	@Test
	@DisplayName("buildFleet: lança AssertionError quando o Scanner é null")
	void buildFleet3() {
		assertThrows(AssertionError.class, () -> Tasks.buildFleet(null),
				"Error: expected AssertionError when Scanner is null");
	}

	// ------------------------------------------------------------------
	// menu()
	// ------------------------------------------------------------------

	@Test
	@DisplayName("menu: comando 'desisto' encerra o menu com mensagem de despedida")
	void menu1() {
		String out = runMenuWithInput("desisto\n");
		assertAll(
				() -> assertTrue(out.contains("AJUDA DO MENU"), "Error: expected initial help menu"),
				() -> assertTrue(out.contains("Bons ventos!"), "Error: expected goodbye message 'Bons ventos!'")
		);
	}

	@Test
	@DisplayName("menu: comandos 'ajuda', 'ranking' e comando inválido executam corretamente")
	void menu2() {
		String out = runMenuWithInput("ajuda\nranking\ncomandoinvalido\ndesisto\n");
		assertAll(
				() -> assertTrue(out.contains("GRANDES NAVEGADORES"), "Error: expected ranking output"),
				() -> assertTrue(out.contains("Que comando é esse??? Repete ..."), "Error: expected unknown command message"),
				() -> assertTrue(out.contains("Bons ventos!"), "Error: expected goodbye message")
		);
	}

	@Test
	@DisplayName("menu: comandos que exigem frota não falham quando executados antes de criar frota")
	void menu3() {
		String out = runMenuWithInput("estado\nmapa\nrajada\nsimula\ntiros\npdf\ndesisto\n");
		assertAll(
				() -> assertTrue(out.contains("Crie ou carregue uma frota primeiro."),
						"Error: expected PDF warning when no game exists"),
				() -> assertTrue(out.contains("Bons ventos!"), "Error: expected goodbye message")
		);
	}

	@Test
	@DisplayName("menu: 'gerafrota' seguido de 'estado', 'tiros', 'rajada' e 'pdf' executa sem erros")
	void menu4() {
		String input = "gerafrota\nestado\ntiros\nrajada A1 A2 A3\npdf\ndesisto\n";		String out = runMenuWithInput(input);
		Path pdfPath = Paths.get("relatorio-battleship.pdf");

		assertAll(
				() -> assertTrue(out.contains("Relatorio PDF criado com sucesso"),
						"Error: expected PDF success message but got:\n" + out),
				() -> assertTrue(Files.exists(pdfPath), "Error: expected PDF file to be created"),
				() -> assertTrue(out.contains("Bons ventos!"), "Error: expected goodbye message")
		);
	}

	@Test
	@DisplayName("menu: 'lefrota' carrega frota personalizada a partir do input")
	void menu5() {
		String input = "lefrota\n" + VALID_FLEET_INPUT + "estado\ndesisto\n";
		String out = runMenuWithInput(input);
		assertTrue(out.contains("Bons ventos!"), "Error: expected menu to complete 'lefrota' and exit cleanly");
	}
}