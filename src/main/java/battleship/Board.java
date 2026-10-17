package battleship;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class Board extends Application {

    private static final int BOARD_SIZE = 10;
    private final char[] rows = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'};
    private static int currentTurnShots = 0;

    // Static variables to share the game state with the graphical interface
    public static IFleet currentFleet;
    public static List<IMove> currentMoves;
    public static Game currentGame;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Battleship - Graphical Board Visualization");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Game Board - Battleship");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);

        // Add column headers (1 to 10)
        for (int col = 1; col <= BOARD_SIZE; col++) {
            Label colLabel = new Label(String.valueOf(col));
            colLabel.setMinSize(35, 35);
            colLabel.setAlignment(Pos.CENTER);
            colLabel.setStyle("-fx-font-weight: bold;");
            grid.add(colLabel, col, 0);
        }

        // Add row headers (A to J) and board cells
        for (int i = 0; i < BOARD_SIZE; i++) {
            char rowChar = rows[i];
            Label rowLabel = new Label(String.valueOf(rowChar));
            rowLabel.setMinSize(35, 35);
            rowLabel.setAlignment(Pos.CENTER);
            rowLabel.setStyle("-fx-font-weight: bold;");
            grid.add(rowLabel, 0, i + 1);

            for (int j = 1; j <= BOARD_SIZE; j++) {
                Button cellButton = new Button();
                cellButton.setMinSize(35, 35);
                cellButton.setMaxSize(35, 35);

                final int r = i;
                final int c = j;
                Position pos = new Position(r, c - 1);

                // Update cell color and appearance based on the game engine
                updateCellAppearance(cellButton, pos);

                // Action when clicking on the cell (board interaction)
                cellButton.setOnAction(e -> {
                    // Check if 3 shots have already been made in this graphical round
                    if (currentTurnShots >= Game.NUMBER_SHOTS) {
                        System.out.println("The limit of 3 shots for this burst has already been reached! Submit or reset the round.");
                        return;
                    }

                    System.out.println("Selected shot: " + pos);
                    currentTurnShots++;

                    boolean isHit = (currentFleet != null && currentFleet.shipAt(pos) != null);

                    if (isHit) {
                        currentFleet.shipAt(pos).shoot(pos);
                        cellButton.setStyle("-fx-background-color: #cf222e; -fx-text-fill: white; -fx-font-weight: bold;"); // Red (Hit)
                        cellButton.setText("X");
                    } else {
                        cellButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;"); // Blue (Water)
                        cellButton.setText("o");
                    }
                    cellButton.setDisable(true); // Disable so it cannot be repeated in the same spot

                    if (currentTurnShots == Game.NUMBER_SHOTS) {
                        System.out.println("-> Burst of 3 shots successfully completed!");
                        // You can optionally submit to the game here if you want to link it to the moves list
                        //currentTurnShots = 0; // Reset for the next burst
                    }
                });

                grid.add(cellButton, j, i + 1);
            }
        }

        root.getChildren().addAll(titleLabel, grid);

        Scene scene = new Scene(root, 500, 550);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Define the color and symbol of the cell based on Battleship rules:
     * - Red: Ship hit (successful shot)
     * - Blue: Water hit (missed shot)
     * - Dark gray: Intact ship (visible in your fleet)
     * - Light gray: Clean water / untested
     */
    private void updateCellAppearance(Button button, IPosition pos) {
        boolean hasShip = (currentFleet != null && currentFleet.shipAt(pos) != null);
        boolean isShot = false;
        boolean isHit = false;

        if (currentMoves != null) {
            for (IMove move : currentMoves) {
                for (IPosition shot : move.getShots()) {
                    if (shot.equals(pos)) {
                        isShot = true;
                        if (hasShip) {
                            isHit = true;
                        }
                    }
                }
            }
        }

        if (isHit) {
            button.setStyle("-fx-background-color: #cf222e; -fx-text-fill: white; -fx-font-weight: bold;"); // Red (Hit)
            button.setText("X");
        } else if (isShot) {
            button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;"); // Blue (Water / Missed)[cite: 15]
            button.setText("o");
        } else if (hasShip) {
            button.setStyle("-fx-background-color: #57606a; -fx-text-fill: white;"); // Undamaged ship
            button.setText("#");
        } else {
            button.setStyle("-fx-background-color: #e0e0e0;"); // Clean water
            button.setText("");
        }
    }

    /**
     * Helper method to launch the GUI passing the current game state.
     */
    public static void launchApp(IFleet fleet, List<IMove> moves, Game game) {
        currentFleet = fleet;
        currentMoves = moves;
        currentGame = game;
        launch(new String[0]);
    }

    public static void launchApp(String[] args) {
        launch(args);
    }
}