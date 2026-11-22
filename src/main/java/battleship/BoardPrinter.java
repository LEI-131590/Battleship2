package battleship;

import java.util.List;

public class BoardPrinter {
    /**
     * Prints the game board by representing the positions of ships, adjacent tiles,
     * shots, and other game elements onto the console. The method also optionally
     * displays shot positions and a legend explaining the symbols used on the board.
     *
     * @param fleet      the fleet of ships to be displayed on the board. Ships are marked
     *                   and their positions are shown according to their placement.
     * @param moves      the list of moves containing shots. If shot positions are shown,
     *                   they will be rendered based on their outcome (hit, miss, etc.).
     * @param showShots if true, displays the shots taken during the game and marks
     *                   their result (hit or miss) on the board.
     * @param showLegend if true, displays an explanatory legend of the symbols used
     *                   to represent various elements such as ships, misses, hits, etc.
     */
    public static void printBoard(IFleet fleet, List<IMove> moves, boolean showShots, boolean showLegend) {

        assert fleet != null;
        assert moves != null;

        char[][] map = new char[Game.BOARD_SIZE][Game.BOARD_SIZE];

        for (int r = 0; r < Game.BOARD_SIZE; r++)
            for (int c = 0; c < Game.BOARD_SIZE; c++)
                map[r][c] = Game.EMPTY_MARKER;

        printShips(fleet, map);

        if (showShots)
            printBoardShots(moves, map);

        printBoardFrame(map);

        if (showLegend) {
            System.out.println("          LEGENDA");
            System.out.println("'" + Game.SHIP_MARKER + "'->navio, '" + Game.SHIP_ADJACENT_MARKER + "'->adjacente a navio, '" + Game.EMPTY_MARKER + "'->água");
            System.out.println("'" + Game.SHOT_SHIP_MARKER + "'->Tiro certeiro, '" + Game.SHOT_WATER_MARKER + "'->Tiro na água");
        }
        System.out.println();
    }

    static void printShips(IFleet fleet, char[][] map) {
        for (IShip ship : fleet.getShips()) {
            for (IPosition ship_pos : ship.getPositions())
                map[ship_pos.getRow()][ship_pos.getColumn()] = Game.SHIP_MARKER;
            if (!ship.stillFloating())
                for (IPosition adjacent_pos : ship.getAdjacentPositions())
                    map[adjacent_pos.getRow()][adjacent_pos.getColumn()] = Game.SHIP_ADJACENT_MARKER;
        }
    }

    static void printBoardShots(List<IMove> moves, char[][] map) {
        for (IMove move : moves)
            for (IPosition shot : move.getShots()) {
                if (shot.isInside()) {
                    int row = shot.getRow();
                    int col = shot.getColumn();
                    if (map[row][col] == Game.SHIP_MARKER)
                        map[row][col] = Game.SHOT_SHIP_MARKER;
                    if (map[row][col] == Game.EMPTY_MARKER || map[row][col] == Game.SHIP_ADJACENT_MARKER)
                        map[row][col] = Game.SHOT_WATER_MARKER;
                }
            }
    }

    static void printBoardFrame(char[][] map) {
        System.out.println();
        System.out.print("    ");
        for (int col = 0; col < Game.BOARD_SIZE; col++) {
            System.out.print(" " + (col + 1));
        }
        System.out.println();

        System.out.print("   +-");
        for (int col = 0; col < Game.BOARD_SIZE; col++) {
            System.out.print("--");
        }
        System.out.println("+");

        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            Position pos = new Position(row, 0);
            char rowLabel = pos.getClassicRow();
            System.out.print(" " + rowLabel + " |");
            for (int col = 0; col < Game.BOARD_SIZE; col++)
                System.out.print(" " + map[row][col]);
            System.out.println(" |");
        }

        System.out.print("   +");
        for (int col = 0; col < Game.BOARD_SIZE; col++)
            System.out.print("--");
        System.out.println("-+");
    }
}