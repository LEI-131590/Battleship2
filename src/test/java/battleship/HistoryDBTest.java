package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class HistoryDB.
 * Author: Lara Fernandes
 * Date: 2026-11-20 11:36
 * Cyclomatic Complexity:
 * - HistoryDB(): 1
 * - initializeDatabase(): 2 (caminho de exceção inalcançável com DB_URL fixo válido)
 * - saveMove(): 2
 */
class HistoryDBTest {

    private static final String DB_URL = "jdbc:sqlite:battleship_history.db";
    private HistoryDB historyDB;

    @BeforeEach
    void setUp(){
        historyDB = new HistoryDB();
    }

    @AfterEach
    void tearDown(){
        historyDB = null;
        // Garante que a tabela fica sempre criada no fim para não afetar outros testes
        HistoryDB.initializeDatabase();
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

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
    // Construtor
    // ------------------------------------------------------------------

    @Test
    @DisplayName("Construtor por omissão de HistoryDB deve criar instância não nula")
    void constructor(){
        assertNotNull(historyDB, "Error: expected HistoryDB instance to be non-null");
    }

    // ------------------------------------------------------------------
    // initializeDatabase()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("initializeDatabase deve criar a tabela moves na base de dados")
    void initializeDatabase() throws Exception{
        HistoryDB.initializeDatabase();

        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='moves'")) {
            assertAll(
                    () -> assertTrue(rs.next(), "Error: expected table 'moves' to exist in database"),
                    () -> assertEquals("moves", rs.getString("name"),
                            "Error: expected table name 'moves' but got " + rs.getString("name"))
            );
        }
    }

    // ------------------------------------------------------------------
    // saveMove()
    // ------------------------------------------------------------------

    @Test
    @DisplayName("saveMove: insere corretamente uma jogada na tabela moves")
    void saveMove1() throws Exception {
        HistoryDB.initializeDatabase();

        int moveNumber = 999;
        String coords = "A1 B2 C3";
        String json = "{\"validShots\":3}";

        HistoryDB.saveMove(moveNumber, coords, json);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT move_number, coordinates, json_result FROM moves WHERE move_number = 999 ORDER BY id DESC LIMIT 1")) {
            assertAll(
                    () -> assertTrue(rs.next(), "Error: expected saved move record to exist in database"),
                    () -> assertEquals(moveNumber, rs.getInt("move_number"),
                            "Error: expected move_number " + moveNumber + " but got " + rs.getInt("move_number")),
                    () -> assertEquals(coords, rs.getString("coordinates"),
                            "Error: expected coordinates '" + coords + "' but got '" + rs.getString("coordinates") + "'"),
                    () -> assertEquals(json, rs.getString("json_result"),
                            "Error: expected json_result '" + json + "' but got '" + rs.getString("json_result") + "'")
            );
        }
    }

    @Test
    @DisplayName("saveMove: captura a exceção e avisa no System.err quando a tabela moves não existe")
    void saveMove2() throws Exception {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS moves");
        }

        String err = captureErr(() ->
                assertDoesNotThrow(() -> HistoryDB.saveMove(1, "A1", "{}"),
                        "Error: expected saveMove to catch SQL exception internally")
        );

        assertTrue(err.contains("Error saving move:"),
                "Error: expected 'Error saving move:' in System.err but got: " + err);
    }

}