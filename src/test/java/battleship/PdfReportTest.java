package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PdfReport.
 *
 * Author: Jessica
 * Date: 2026-11-21 17:47
 *
 * Cyclomatic Complexity:
 * - PdfReport()       : 1
 * - export()          : 1
 * - addReportContent(): 5
 * - createBoard()     : 15
 * - addCell()         : 7
 *
 * Note:
 * PdfReport has a private constructor and static methods.
 * Therefore, no PdfReport instance is created.
 * Private methods are tested indirectly through export().
 */
class PdfReportTest {

    @TempDir
    Path tempDir;

    private IGame game;

    @BeforeEach
    void setUp() {
        // Create a default game fixture with an empty fleet and board
        game = createGame(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        // Release the test fixture after each test
        game = null;
    }

    // EXPORT TESTS

    @Test
    @DisplayName("export: Should throw NullPointerException when game is null")
    void export1() {
        Path outputFile = tempDir.resolve("report.pdf");
        assertThrows(NullPointerException.class, () -> PdfReport.export(null, outputFile), "Error: export should throw NullPointerException when game is null");
    }

    @Test
    @DisplayName("export: Should throw NullPointerException when output file is null")
    void export2() {
        assertThrows(NullPointerException.class, () -> PdfReport.export(game, null), "Error: export should throw NullPointerException when output file is null");
    }

    @Test
    @DisplayName("export: Should successfully generate a PDF report")
    void export3() throws Exception {
        Path outputFile = tempDir.resolve("report.pdf");
        PdfReport.export(game, outputFile);
        assertAll(
                "PDF generation results",
                () -> assertTrue(
                        Files.exists(outputFile),
                        "Error: the PDF file should have been created"
                ),
                () -> assertTrue(
                        Files.size(outputFile) > 0,
                        "Error: the generate PDF should not be empty"
                ),
                () -> assertTrue(
                        new String(Files.readAllBytes(outputFile), 0, 4).startsWith("%PDF"),
                        "Error: the generated file should hava a valid PDF signature"
                )
        );
    }

    @Test
    @DisplayName("export: Should create missing parent directories when exporting PDF")
    void export4() throws Exception {
        Path outputFile = tempDir
                .resolve("nested")
                .resolve("reports")
                .resolve("report.pdf");
        PdfReport.export(game, outputFile);
        assertAll(
                "Directory creation results",
                () -> assertTrue(
                        Files.exists(outputFile.getParent()),
                        "Error: missing parent directories should have been created"
                ),
                () -> assertTrue(
                        Files.exists(outputFile),
                        "Error: the PDF should have been created inside the new directories"
                ),
                () -> assertTrue(
                        Files.size(outputFile) > 0,
                        "Error: the generated PDF should contain data"
                )
        );
    }

    // PDF CONTENT TESTS

    @Test
    @DisplayName("export: Should include report headings and fleet statistics")
    void export5() throws Exception {
        Path outputFile = tempDir.resolve("statistics.pdf");
        PdfReport.export(game, outputFile);
        String content = extractPdfText(outputFile);
        assertAll(
                "PDF report content",
                () -> assertTrue(
                        content.contains("Relatório do Jogo - Battleship"),
                        "Error: the PDF should contain the report title"
                ),
                () -> assertTrue(
                        content.contains("Estado da Frota"),
                        "Error: the PDF should contain the fleet status heading"
                ),
                () -> assertTrue(
                        content.contains("Navios a flutuar: 0"),
                        "Error: the PDF should report zero floating ships"
                ),
                () -> assertTrue(
                        content.contains("Navios afundados: 0"),
                        "Error: the PDF should report zero sunk ships"
                ),
                () -> assertTrue(
                        content.contains("Tiros certeiros: 0"),
                        "Error: the PDF should report zero hits"
                ),
                () -> assertTrue(
                        content.contains("Tiros invalidos: 0"),
                        "Error: the PDF should report zero invalid shots"
                ),
                () -> assertTrue(
                        content.contains("Tiros repetidos: 0"),
                        "Error: the PDF should report zero repeated shots"
                ),
                () -> assertTrue(
                        content.contains("Rajadas registadas: 0"),
                        "Error: the PDF should report zero registered moves"
                ),
                () -> assertTrue(
                        content.contains("Tabuleiro"),
                        "Error: the PDF should contain the board heading"
                ),
                () -> assertTrue(
                        content.contains("Legenda"),
                        "Error: the PDF should contain the legend"
                )
        );
    }

    @Test
    @DisplayName("export: Should display all board symbols in the generated PDF")
    void export6() throws Exception {
        // Floating ship occupying (0,0)
        IPosition shipPosition = createPosition(0, 0);
        IShip floatingShip = createShip(
                List.of(shipPosition),
                List.of()
        );
        // Sunk ship with adjacent positions:
        // (1,1) is empty and inside the board
        // (0,0) already contains a ship
        // (-1,0) is outside the board
        IShip sunkShip = createShip(
                List.of(createPosition(4, 4)),
                List.of(
                        createPosition(1, 1),
                        createPosition(2, 1),
                        createPosition(0, 0),
                        createPosition(-1, 0)
                )
        );
        // Enemy shots:
        // (-1,2) -> outside the board, ignored
        // (0,0)  -> hit on a ship
        // (1,1)  -> miss on an adjacent position
        // (2,2)  -> miss on empty water
        // (2,2)  -> repeated miss (existing 'o' is preserved)
        List<IPosition> shots = List.of(
                createPosition(-1, 2),
                createPosition(0, 0),
                createPosition(1, 1),
                createPosition(2, 2),
                createPosition(2, 2)
        );
        game = createGame(
                List.of(floatingShip, sunkShip),
                List.of(sunkShip),
                shots
        );
        Path outputFile = tempDir.resolve("board.pdf");
        PdfReport.export(game, outputFile);
        String content = extractPdfText(outputFile);
        assertAll(
                "Board symbols and report content",
                () -> assertTrue(
                        content.contains("#"),
                        "Error: the PDF should contain the ship symbol (#)"
                ),
                () -> assertTrue(
                        content.contains("*"),
                        "Error: the PDF should contain the hit symbol (*)"
                ),
                () -> assertTrue(
                        content.contains("o"),
                        "Error: the PDF should contain the water-shot symbol (o)"
                ),
                () -> assertTrue(
                        content.contains("-"),
                        "Error: the PDF should contain the adjacent-to-sunk-ship symbol (-)"
                ),
                () -> assertTrue(
                        content.contains("."),
                        "Error: the PDF should contain the empty-water symbol (.)"
                )
        );
    }

    @Test
    @DisplayName("addCell: Should cover default background color")
    void addCell7() throws Exception {
        Method addCellMethod = PdfReport.class.getDeclaredMethod(
                "addCell",
                PdfPTable.class,
                String.class,
                boolean.class
        );
        addCellMethod.setAccessible(true);
        PdfPTable table = new PdfPTable(1);
        addCellMethod.invoke(null, table, "X", false);
        PdfPCell cell = table.getRow(0).getCells()[0];
        assertEquals(Color.WHITE, cell.getBackgroundColor());
    }

    // PDF TEXT EXTRACTION

    private String extractPdfText(Path outputFile) throws IOException {
        byte[] pdfBytes = Files.readAllBytes(outputFile);
        try (PdfReader reader = new PdfReader(pdfBytes)) {
            StringBuilder content = new StringBuilder();
            for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                PdfTextExtractor extractor = new PdfTextExtractor(reader);
                content.append(extractor.getTextFromPage(page, false));
            }
            return content.toString();
        }
    }

    // TEST FIXTURE CREATION

    private static IGame createGame(List<IShip> ships, List<IShip> sunkShips, List<IPosition> shots) {
        List<IShip> floatingShips = new ArrayList<>(ships);
        floatingShips.removeIf(ship ->
                sunkShips.stream().anyMatch(sunk -> sunk == ship)
        );
        IFleet fleet = createFleet(ships, floatingShips, sunkShips);
        IMove move = createMove(shots);
        List<IMove> moves = shots.isEmpty() ? new ArrayList<>() : List.of(move);
        return createProxy(IGame.class, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getMyFleet" -> fleet;
                case "getAlienMoves" -> moves;
                case "getHits" -> 0;
                case "getInvalidShots" -> 0;
                case "getRepeatedShots" -> 0;
                case "getSunkShips" -> sunkShips.size();
                case "getRemainingShips" -> floatingShips.size();
                default -> defaultValue(method.getReturnType());
            };
        });
    }

    private static IFleet createFleet(List<IShip> ships, List<IShip> floatingShips, List<IShip> sunkShips) {
        return createProxy(IFleet.class, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getShips" -> ships;
                case "getFloatingShips" -> floatingShips;
                case "getSunkShips" -> sunkShips;
                case "shipAt" -> {
                    IPosition requested = (IPosition) args[0];
                    IShip foundShip = null;
                    for (IShip ship : ships) {
                        for (IPosition position : ship.getPositions()) {
                            if (position.getRow() == requested.getRow() && position.getColumn() == requested.getColumn()) {
                                foundShip = ship;
                                break;
                            }
                        }
                        if (foundShip != null) {
                            break;
                        }
                    }
                    yield foundShip;
                }
                default -> defaultValue(method.getReturnType());
            };
        });
    }

    private static IShip createShip(List<IPosition> positions, List<IPosition> adjacentPositions) {
        return createProxy(IShip.class, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getPositions" -> positions;
                case "getAdjacentPositions" -> adjacentPositions;
                case "getSize" -> positions.size();
                case "getCategory" -> "Test Ship";
                default -> defaultValue(method.getReturnType());
            };
        });
    }

    private static IPosition createPosition(int row, int column) {
        return createProxy(IPosition.class, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getRow" -> row;
                case "getColumn" -> column;
                case "isInside" ->
                        row >= 0 && row < Game.BOARD_SIZE && column >= 0 && column < Game.BOARD_SIZE;
                case "getClassicRow" -> (char) ('A' + row);
                case "getClassicColumn" -> column + 1;
                default -> defaultValue(method.getReturnType());
            };
        });
    }

    private static IMove createMove(List<IPosition> shots) {
        return createProxy(IMove.class, (proxy, method, args) -> {
            return switch (method.getName()) {
                case "getShots" -> shots;
                case "getNumber" -> 1;
                case "getShotResults" -> new ArrayList<>();
                case "toString" -> "Test Move";
                default -> defaultValue(method.getReturnType());
            };
        });
    }

    // GENERIC DYNAMIC PROXY UTILITIES

    @SuppressWarnings("unchecked")
    private static <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                handler
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive() || returnType == void.class) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0f;
        }
        if (returnType == double.class) {
            return 0d;
        }
        return null;
    }

}


