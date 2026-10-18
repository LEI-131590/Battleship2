package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class HistoryDB {

    //database file
    private static final String DB_URL = "jdbc:sqlite:battleship_history.db";

    /**
     * Creates the database and table if they do not exist
     */
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS moves (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "move_number INTEGER, " +
                    "coordinates TEXT, " +
                    "json_result TEXT)";

            stmt.execute(sql);
        } catch (Exception e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /**
     * Saves the move coordinates and the result into the table.
     */
    public static void saveMove(int moveNumber, String coordinates, String jsonResult) {
        String sql = "INSERT INTO moves (move_number, coordinates, json_result) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, moveNumber);
            pstmt.setString(2, coordinates);
            pstmt.setString(3, jsonResult);

            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error saving move: " + e.getMessage());
        }
    }
}
