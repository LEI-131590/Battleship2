package battleship;

import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.List;
import java.awt.Color;

public class PdfReport {

    private PdfReport() {
    }

    public static void export(IGame game, Path outputFile) throws IOException, DocumentException {
        Objects.requireNonNull(game, "Game must not be null");
        Objects.requireNonNull(outputFile, "Output file must not be null");
        Path absolutePath = outputFile.toAbsolutePath();
        Files.createDirectories(absolutePath.getParent());
        Document document = new Document(PageSize.A4);
        try (OutputStream outputStream = Files.newOutputStream(absolutePath)) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            try {
                addReportContent(document, game);
            } finally {
                document.close();
            }
        }
    }

    private static void addReportContent(Document document, IGame game) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        document.add(new Paragraph("Relatório do Jogo - Battleship", titleFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Estado da Frota", headingFont));
        document.add(new Paragraph("Navios a flutuar: " + game.getMyFleet().getFloatingShips().size(), normalFont));
        document.add(new Paragraph("Navios afundados: " + game.getMyFleet().getSunkShips().size(), normalFont));
        document.add(new Paragraph("Tiros certeiros: " + game.getHits(), normalFont));
        document.add(new Paragraph("Tiros invalidos: " + game.getInvalidShots(), normalFont));
        document.add(new Paragraph("Tiros repetidos: " + game.getRepeatedShots(), normalFont));
        document.add(new Paragraph("Rajadas registadas: " + game.getAlienMoves().size(), normalFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Tabuleiro", headingFont));
        document.add(new Paragraph(" "));
        char[][] map = createBoard(game);
        PdfPTable table = new PdfPTable(Game.BOARD_SIZE + 1);
        table.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.setWidthPercentage(0);
        table.setTotalWidth(330f);
        table.setLockedWidth(true);
        float[] widths = new float[Game.BOARD_SIZE + 1];
        for (int c = 0; c < widths.length; c++) {
            widths[c] = 1f;
        }
        table.setWidths(widths);
        addCell(table, "", true);
        for (int col = 1; col <= Game.BOARD_SIZE; col++) {
            addCell(table, String.valueOf(col), true);
        }
        for (int row = 0; row < Game.BOARD_SIZE; row++) {
            addCell(table, String.valueOf((char) ('A' + row)), true);
            for (int col = 0; col < Game.BOARD_SIZE; col++) {
                addCell(table, String.valueOf(map[row][col]), false);
            }
        }
        document.add(table);
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Legenda", headingFont));
        document.add(new Paragraph("# = Navio; * = Tiro certeiro; " + "o = Tiro na agua; " + "- = Adjacente a navio afundado; " + ". = Agua", normalFont));
    }

    private static char[][] createBoard(IGame game) {
        char[][] board = new char[Game.BOARD_SIZE][Game.BOARD_SIZE];
        for (int i = 0; i < Game.BOARD_SIZE; i++) {
            for (int j = 0; j < Game.BOARD_SIZE; j++) {
                board[i][j] = '.';
            }
        }
        IFleet fleet = game.getMyFleet();
        for (IShip ship : fleet.getShips()) {
            for (IPosition position : ship.getPositions()) {
                board[position.getRow()][position.getColumn()] = '#';
            }
        }
        for (IShip ship : fleet.getSunkShips()) {
            for (IPosition position : ship.getAdjacentPositions()) {
                int row = position.getRow();
                int col =  position.getColumn();
                if (position.isInside() && board[row][col] == '.') {
                    board[row][col] = '-';
                }
            }
        }
        for (IMove move : game.getAlienMoves()) {
            List<IPosition> shots = move.getShots();
            for (IPosition shot : shots) {
                if (!shot.isInside()) {
                    continue;
                }
                int row = shot.getRow();
                int col = shot.getColumn();
                if (fleet.shipAt(shot) != null) {
                    board[row][col] = '*';
                } else if (board[row][col] == '-' || board[row][col] == '.') {
                    board[row][col] = 'o';
                }
            }
        }
        return board;
    }

    private static void addCell(PdfPTable table, String value, boolean header) {
        Font font = FontFactory.getFont(header ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setFixedHeight(30f);
        cell.setPadding(0);
        cell.setBorderColor(new Color(190, 200, 210));
        cell.setBorderWidth(0.6f);
        if (header) {
            cell.setBackgroundColor(new Color(230, 235, 240));
        } else {
            switch (value) {
                case "#" -> cell.setBackgroundColor(new Color(190, 215, 240));
                case "*" -> cell.setBackgroundColor(new Color(165, 220, 175));
                case "o" -> cell.setBackgroundColor(new Color(250, 200, 195));
                case "-" -> cell.setBackgroundColor(new Color(255, 255, 204));
                case "." -> cell.setBackgroundColor(Color.WHITE);
                default -> cell.setBackgroundColor(Color.WHITE);
            }
        }
        table.addCell(cell);
    }

}
