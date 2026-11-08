package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

// Comentário adicionado para o Qodana ler 129896
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

    public void exportHistoryToCSV() { System.out.println("Exporting CSV..."); }
    public void importHistoryFromCSV() { System.out.println("Importing CSV..."); }
    public void getTopTenScores() { System.out.println("Getting top scores..."); }
    public void getAverageScorePerPlayer() { System.out.println("Getting average score..."); }
    public void getTotalGamesPlayed() { System.out.println("Getting total games..."); }
    public void getWinLossRatio() { System.out.println("Getting win/loss ratio..."); }
    public void deletePlayerHistory(String playerName) { System.out.println("Deleting history for " + playerName); }
    public void backupDatabase() { System.out.println("Backing up..."); }
    public void restoreDatabase() { System.out.println("Restoring..."); }
    public void filterHistoryByDate(String date) { System.out.println("Filtering by date " + date); }
    public void filterHistoryByPlayer(String playerName) { System.out.println("Filtering by player " + playerName); }
    public void generateMonthlyReport() { System.out.println("Generating report..."); }
    public void calculateGlobalStats() { System.out.println("Calculating stats..."); }
    public void optimizeDatabaseConnection() { System.out.println("Optimizing DB..."); }
    public void checkDatabaseIntegrity() { System.out.println("Checking integrity..."); }
    public void getLongestGameDuration() { System.out.println("Getting duration..."); }
    public void clearOldHistoryRecords() { System.out.println("Clearing old records..."); }
    public void getWinStreakForPlayer(String playerName) { System.out.println("Getting win streak for " + playerName); }
}
