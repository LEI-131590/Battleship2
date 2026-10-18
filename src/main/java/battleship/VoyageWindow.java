package battleship;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Janela final da Rota da Índia (issue #6).
 *
 * <p>Mostra um mapa colorido e animado (mar com ondas, terra com florestas,
 * desertos e montanhas, nuvens e uma baleia), onde uma caravela navega desde
 * Lisboa até ao destino alcançado na partida. Mostra também o resumo da partida
 * e o top 5 dos Grandes Navegadores.</p>
 */
public class VoyageWindow {

    // ---------------------------------------------------------------- cores
    private static final Color SEA_LIGHT = Color.web("#8fdcf7");
    private static final Color SEA_DEEP = Color.web("#2f8fd0");
    private static final Color LAND = Color.web("#7cc576");
    private static final Color LAND_EDGE = Color.web("#3f8f45");
    private static final Color BEACH = Color.web("#f5e3a3");
    private static final Color DESERT = Color.web("#ecd593");
    private static final Color FOREST = Color.web("#3f9a4a");
    private static final Color NAVY = Color.web("#1d4e89");
    private static final Color TEAL = Color.web("#2a8bc4");
    private static final Color CORAL = Color.web("#ff5a36");
    private static final Color GOLD = Color.web("#ffc93c");
    private static final Color GOLD_EDGE = Color.web("#e08a00");
    private static final Color WOOD = Color.web("#8b5a2b");

    private static final double MAP_WIDTH = 760;
    private static final double MAP_HEIGHT = 410;

    /** Duração da viagem entre dois destinos consecutivos. */
    private static final Duration LEG_DURATION = Duration.millis(1300);

    // ------------------------------------------------- a rota e os destinos
    /** Ponto de partida: Lisboa. */
    private static final Point2D LISBOA = new Point2D(135, 50);

    /**
     * Pontos por onde a caravela passa até chegar a cada destino.
     * O último ponto de cada lista é o próprio destino.
     * A ordem segue a ordem do enum {@link VoyageStats.Destination}.
     */
    private static final double[][][] LEGS = {
            {{160, 70}, {186, 75}},                                                         // Ceuta
            {{150, 86}, {94, 98}},                                                          // Madeira
            {{104, 113}, {120, 125}},                                                       // Cabo Bojador
            {{110, 160}, {140, 207}, {212, 224}, {248, 262}, {268, 330}, {306, 388}},       // Boa Esperança
            {{348, 392}, {394, 340}, {446, 268}, {482, 222}, {540, 192}, {602, 168}}        // Índia
    };

    /** Posição (canto superior esquerdo) da etiqueta com o nome de cada destino. */
    private static final double[][] LABELS = {
            {196, 80}, {22, 78}, {132, 118}, {326, 380}, {618, 196}
    };

    private VoyageWindow() {
        // classe utilitária: só tem métodos estáticos
    }

    // ================================================================ abrir
    /**
     * Abre a janela da Rota da Índia e espera até o jogador a fechar.
     *
     * <p>O JavaFX só pode ser arrancado uma vez por execução do programa.
     * Por isso, se já estiver a correr (por exemplo, por causa da janela do
     * tabuleiro), a janela é aberta no JavaFX que já existe. Se não for possível
     * abrir a janela, o jogo continua normalmente: o resumo já foi mostrado no
     * terminal.</p>
     *
     * @param voyage     as estatísticas da partida
     * @param ranking    o ranking, já com esta partida incluída
     * @param playerName o nome do navegador
     * @param position   a posição em que a partida ficou no ranking
     */
    public static void show(VoyageStats voyage, RankingBoard ranking, String playerName, int position) {
        CountDownLatch opened = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);

        Runnable openWindow = () -> {
            try {
                Stage stage = new Stage();
                stage.setTitle("Rota da Índia");
                stage.setScene(buildScene(voyage, ranking, playerName, position, stage));
                stage.setResizable(false);
                stage.setOnHidden(e -> closed.countDown());
                stage.show();
                stage.toFront();
            } catch (RuntimeException e) {
                System.err.println("Não foi possível mostrar a janela: " + e.getMessage());
                closed.countDown();
            } finally {
                opened.countDown();
            }
        };

        try {
            // Mantém o JavaFX ligado depois de as janelas fecharem, para poder ser reutilizado
            Platform.setImplicitExit(false);
            try {
                Platform.startup(openWindow);         // primeira vez: arranca o JavaFX
            } catch (IllegalStateException alreadyStarted) {
                Platform.runLater(openWindow);        // o JavaFX já estava a correr
            }

            if (!opened.await(5, TimeUnit.SECONDS)) {
                System.out.println("(A janela da Rota da Índia não pôde ser aberta.)");
                return;
            }
            closed.await();
        } catch (IllegalStateException | InterruptedException e) {
            System.out.println("(A janela da Rota da Índia não pôde ser aberta.)");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ============================================================ a janela
    private static Scene buildScene(VoyageStats voyage, RankingBoard ranking,
                                    String playerName, int position, Stage stage) {
        VoyageStats.Destination destination = voyage.getDestination();

        // --- título
        Label title = new Label("Rota da Índia");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        title.setTextFill(NAVY);
        Label subtitle = new Label("Diário de bordo de " + playerName);
        subtitle.setFont(Font.font("Georgia", FontPosture.ITALIC, 15));
        subtitle.setTextFill(TEAL);

        Label result = new Label(arrivalPhrase(destination));
        result.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        result.setTextFill(Color.WHITE);
        result.setStyle("-fx-background-color: " + toHex(CORAL) + "; -fx-background-radius: 20;"
                + "-fx-padding: 4 22 4 22;");
        result.setOpacity(0);

        VBox header = new VBox(4, title, subtitle, result);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 10, 0));

        // --- mapa
        Pane map = new Pane();
        map.setPrefSize(MAP_WIDTH, MAP_HEIGHT);
        map.setMinSize(MAP_WIDTH, MAP_HEIGHT);
        map.setMaxSize(MAP_WIDTH, MAP_HEIGHT);
        Rectangle clip = new Rectangle(MAP_WIDTH, MAP_HEIGHT);
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        map.setClip(clip);

        drawSea(map);
        drawWaves(map);
        drawLand(map);
        drawCompassRose(map, 692, 336);
        drawWhale(map, 60, 330);
        drawClouds(map);   // as nuvens passam por baixo da rota e da caravela

        Path fullRoute = buildRoute(VoyageStats.Destination.values().length - 1);
        fullRoute.setStroke(Color.WHITE);
        fullRoute.setOpacity(0.85);
        fullRoute.setStrokeWidth(2.5);
        fullRoute.getStrokeDashArray().addAll(7.0, 7.0);
        map.getChildren().add(fullRoute);

        double length = approximateLength(destination.ordinal());
        Path glow = buildRoute(destination.ordinal());
        glow.setStroke(Color.WHITE);
        glow.setStrokeWidth(8);
        glow.setOpacity(0.7);
        Path travelled = buildRoute(destination.ordinal());
        travelled.setStroke(CORAL);
        travelled.setStrokeWidth(4);
        for (Path p : List.of(glow, travelled)) {
            p.setStrokeLineCap(StrokeLineCap.ROUND);
            p.getStrokeDashArray().addAll(length, length);
            p.setStrokeDashOffset(length);
        }
        map.getChildren().addAll(glow, travelled);

        drawMarker(map, LISBOA, "Lisboa", 66, 34, true);
        List<Circle> markers = new ArrayList<>();
        for (VoyageStats.Destination d : VoyageStats.Destination.values()) {
            double[] stop = LEGS[d.ordinal()][LEGS[d.ordinal()].length - 1];
            markers.add(drawMarker(map, new Point2D(stop[0], stop[1]), d.getDisplayName(),
                    LABELS[d.ordinal()][0], LABELS[d.ordinal()][1], false));
        }

        Group caravel = buildCaravel();
        caravel.setTranslateX(LISBOA.getX());
        caravel.setTranslateY(LISBOA.getY());
        map.getChildren().add(caravel);

        StackedMap framed = new StackedMap(map);

        // --- estatísticas e ranking
        HBox stats = new HBox(10,
                statCard("Tempo", voyage.getFormattedTime(), TEAL),
                statCard("Rajadas", String.valueOf(voyage.getVolleys()), TEAL),
                statCard("Pontaria", Math.round(voyage.getAccuracy() * 100) + "%", TEAL),
                statCard("Pontos", String.valueOf(voyage.getPoints()), CORAL));
        stats.setAlignment(Pos.CENTER_LEFT);

        Label points = new Label("pontaria " + voyage.getAccuracyPoints()
                + "  +  eficiência " + voyage.getEfficiencyPoints()
                + "  +  rapidez " + voyage.getSpeedPoints());
        points.setFont(Font.font("Georgia", FontPosture.ITALIC, 12));
        points.setTextFill(TEAL);

        Label placeLabel = new Label("Ficaste em " + position + "º lugar no ranking!");
        placeLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        placeLabel.setTextFill(NAVY);

        Button close = new Button("Terminar viagem");
        close.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        close.setTextFill(Color.WHITE);
        close.setStyle("-fx-background-color: " + toHex(CORAL) + "; -fx-background-radius: 20;"
                + "-fx-padding: 8 22 8 22; -fx-cursor: hand;");
        close.setOnAction(e -> stage.close());

        VBox left = new VBox(10, stats, points, placeLabel, close);
        left.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);

        HBox bottom = new HBox(20, left, buildTopTable(ranking, position));
        bottom.setPadding(new Insets(14, 0, 0, 0));

        BorderPane root = new BorderPane(framed.node(), header, null, bottom, null);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #dff3ff, #fff8e7);");

        // --- animação da viagem
        Duration travelTime = LEG_DURATION.multiply(destination.ordinal() + 1);

        PathTransition sail = new PathTransition(travelTime, buildRoute(destination.ordinal()), caravel);
        sail.setInterpolator(Interpolator.EASE_BOTH);

        Timeline drawRoute = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(travelled.strokeDashOffsetProperty(), length),
                        new KeyValue(glow.strokeDashOffsetProperty(), length)),
                new KeyFrame(travelTime,
                        new KeyValue(travelled.strokeDashOffsetProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(glow.strokeDashOffsetProperty(), 0, Interpolator.EASE_BOTH)));

        // os destinos ficam dourados à medida que a caravela lá chega
        Timeline lightUp = new Timeline();
        for (int i = 0; i <= destination.ordinal(); i++) {
            Circle marker = markers.get(i);
            lightUp.getKeyFrames().add(new KeyFrame(LEG_DURATION.multiply(i + 1),
                    new KeyValue(marker.fillProperty(), GOLD),
                    new KeyValue(marker.strokeProperty(), GOLD_EDGE),
                    new KeyValue(marker.radiusProperty(), 8)));
        }

        RotateTransition bob = new RotateTransition(Duration.millis(700), caravel.getChildren().get(0));
        bob.setFromAngle(-6);
        bob.setToAngle(6);
        bob.setAutoReverse(true);
        bob.setCycleCount(Animation.INDEFINITE);
        bob.play();

        FadeTransition showResult = new FadeTransition(Duration.millis(600), result);
        showResult.setToValue(1);
        ScaleTransition popResult = new ScaleTransition(Duration.millis(400), result);
        popResult.setFromX(0.5);
        popResult.setFromY(0.5);
        popResult.setToX(1);
        popResult.setToY(1);
        popResult.setInterpolator(Interpolator.EASE_OUT);

        SequentialTransition voyageAnimation = new SequentialTransition(
                new PauseTransition(Duration.millis(500)),
                new ParallelTransition(sail, drawRoute, lightUp),
                new ParallelTransition(showResult, popResult));

        if (destination == VoyageStats.Destination.INDIA) {
            voyageAnimation.setOnFinished(e -> celebrate(map, markers.get(destination.ordinal())));
        }
        voyageAnimation.play();

        return new Scene(root);
    }

    /** Envolve o mapa numa moldura com cantos arredondados. */
    private record StackedMap(Pane map) {
        Node node() {
            Pane frame = new Pane(map);
            frame.setMaxSize(MAP_WIDTH + 6, MAP_HEIGHT + 6);
            map.setLayoutX(3);
            map.setLayoutY(3);
            frame.setStyle("-fx-background-color: " + toHex(NAVY) + "; -fx-background-radius: 15;");
            return frame;
        }
    }

    /** Frase que aparece quando a caravela chega ao destino. */
    private static String arrivalPhrase(VoyageStats.Destination d) {
        switch (d) {
            case CEUTA:
                return "Chegaste a Ceuta!";
            case MADEIRA:
                return "Chegaste à Madeira!";
            case CABO_BOJADOR:
                return "Dobraste o Cabo Bojador!";
            case BOA_ESPERANCA:
                return "Dobraste o Cabo da Boa Esperança!";
            default:
                return "Chegaste à Índia!";
        }
    }

    // ============================================================ o mar
    /** Pinta o mar com um degradê de azul claro (norte) para azul profundo (sul). */
    private static void drawSea(Pane map) {
        Rectangle sea = new Rectangle(MAP_WIDTH, MAP_HEIGHT);
        sea.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, SEA_LIGHT), new Stop(1, SEA_DEEP)));
        map.getChildren().add(sea);
    }

    /** Pequenas ondas brancas que se mexem devagar. */
    private static void drawWaves(Pane map) {
        double[][] positions = {
                {55, 170}, {30, 250}, {80, 300}, {150, 280}, {190, 340}, {230, 385},
                {420, 385}, {500, 330}, {560, 260}, {520, 230}, {700, 250}, {735, 175},
                {610, 300}, {300, 52}, {470, 45}, {40, 380}, {640, 370}, {110, 210}
        };
        for (int i = 0; i < positions.length; i++) {
            double x = positions[i][0];
            double y = positions[i][1];
            Path wave = new Path(new MoveTo(x, y),
                    new QuadCurveTo(x + 5, y - 5, x + 10, y),
                    new QuadCurveTo(x + 15, y - 5, x + 20, y));
            wave.setStroke(Color.WHITE);
            wave.setStrokeWidth(2);
            wave.setFill(null);
            wave.setStrokeLineCap(StrokeLineCap.ROUND);
            map.getChildren().add(wave);

            TranslateTransition drift = new TranslateTransition(Duration.millis(1800 + i * 90), wave);
            drift.setByX(8);
            drift.setAutoReverse(true);
            drift.setCycleCount(Animation.INDEFINITE);
            FadeTransition shimmer = new FadeTransition(Duration.millis(1800 + i * 90), wave);
            shimmer.setFromValue(0.3);
            shimmer.setToValue(0.9);
            shimmer.setAutoReverse(true);
            shimmer.setCycleCount(Animation.INDEFINITE);
            ParallelTransition move = new ParallelTransition(drift, shimmer);
            move.setDelay(Duration.millis(i * 130));
            move.play();
        }
    }

    /** Nuvens brancas que atravessam o mapa muito devagar. */
    private static void drawClouds(Pane map) {
        double[][] clouds = {{40, 0.25, 45}, {175, 0.65, 60}, {300, 0.05, 75}};   // {y, início, segundos}
        for (double[] c : clouds) {
            Group cloud = new Group(
                    puff(0, 0, 22, 11), puff(16, -7, 16, 12), puff(30, 0, 20, 10), puff(14, 4, 24, 9));
            cloud.setOpacity(0.85);
            cloud.setLayoutY(c[0]);
            map.getChildren().add(cloud);

            TranslateTransition fly = new TranslateTransition(Duration.seconds(c[2]), cloud);
            fly.setFromX(-80);
            fly.setToX(MAP_WIDTH + 40);
            fly.setInterpolator(Interpolator.LINEAR);
            fly.setCycleCount(Animation.INDEFINITE);
            fly.playFrom(Duration.seconds(c[2] * c[1]));
        }
    }

    private static Ellipse puff(double x, double y, double rx, double ry) {
        Ellipse e = new Ellipse(x, y, rx, ry);
        e.setFill(Color.WHITE);
        return e;
    }

    /** Uma baleia simpática que sobe e desce e deita água pelo espiráculo. */
    private static void drawWhale(Pane map, double x, double y) {
        Ellipse body = new Ellipse(0, 0, 20, 10);
        body.setFill(Color.web("#4a6fa5"));
        Ellipse belly = new Ellipse(2, 4, 15, 5);
        belly.setFill(Color.web("#a9c4e8"));
        Polygon tail = new Polygon(-18, 0, -30, -9, -27, 0, -30, 9);
        tail.setFill(Color.web("#4a6fa5"));
        Circle eye = new Circle(11, -2, 1.8, Color.web("#1b2a40"));
        Group spout = new Group(
                spray(4, -10, 0, -20), spray(4, -10, -5, -18), spray(4, -10, 5, -18));
        Group whale = new Group(tail, body, belly, eye, spout);
        whale.setLayoutX(x);
        whale.setLayoutY(y);
        map.getChildren().add(whale);

        TranslateTransition swim = new TranslateTransition(Duration.millis(1600), whale);
        swim.setByY(-4);
        swim.setAutoReverse(true);
        swim.setCycleCount(Animation.INDEFINITE);
        swim.play();

        FadeTransition blow = new FadeTransition(Duration.millis(900), spout);
        blow.setFromValue(0);
        blow.setToValue(1);
        blow.setAutoReverse(true);
        blow.setCycleCount(Animation.INDEFINITE);
        blow.play();
    }

    private static Line spray(double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.WHITE);
        line.setStrokeWidth(2);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        return line;
    }

    // ============================================================ a terra
    /**
     * Desenha a Península Ibérica, a Europa, África, a Arábia, a Índia e a Ásia
     * (versão simplificada), com praias, desertos, montanhas e árvores.
     */
    private static void drawLand(Pane map) {
        double[][] lands = {
                {128, 22, 210, 18, 232, 40, 205, 62, 178, 68, 150, 64, 132, 48},                    // Península Ibérica
                {195, 0, 420, 0, 400, 22, 360, 40, 300, 28, 232, 40, 210, 18},                      // resto da Europa
                {175, 78, 260, 72, 360, 80, 400, 140, 430, 180, 470, 190, 440, 240,
                        415, 300, 375, 350, 335, 380, 310, 382, 292, 340, 278, 275, 262, 232,
                        232, 203, 182, 192, 142, 172, 124, 140, 148, 104},                          // África
                {392, 84, 510, 92, 548, 132, 522, 166, 472, 178, 440, 150, 410, 118},               // Arábia
                {555, 60, 690, 60, 700, 100, 655, 185, 630, 215, 605, 140, 560, 105},               // Índia
                {530, 0, 760, 0, 760, 60, 690, 60, 555, 60, 520, 40}                                // Ásia
        };
        // primeiro as praias (um contorno largo cor de areia), depois a terra por cima
        for (double[] l : lands) {
            Polygon beach = new Polygon(l);
            beach.setFill(BEACH);
            beach.setStroke(BEACH);
            beach.setStrokeWidth(7);
            beach.setStrokeLineJoin(StrokeLineJoin.ROUND);
            map.getChildren().add(beach);
        }
        for (double[] l : lands) {
            Polygon land = new Polygon(l);
            land.setFill(LAND);
            land.setStroke(LAND_EDGE);
            land.setStrokeWidth(1.5);
            land.setStrokeLineJoin(StrokeLineJoin.ROUND);
            map.getChildren().add(land);
        }

        // desertos: o Sara e a Arábia
        map.getChildren().addAll(
                desert(190, 96, 350, 92, 385, 135, 330, 152, 250, 160, 185, 150, 160, 122),
                desert(405, 104, 505, 98, 535, 132, 512, 158, 470, 168, 445, 145));

        // ilhas: Madeira, Canárias, Cabo Verde, Madagáscar e Ceilão
        map.getChildren().addAll(
                island(88, 104, 7, 3.5), island(104, 136, 4, 2), island(112, 140, 3, 2),
                island(84, 186, 3, 2), island(90, 192, 3, 2),
                island(455, 330, 8, 20), island(655, 225, 6, 8));

        // montanhas: Atlas e Himalaias
        map.getChildren().addAll(
                mountain(205, 98, 14), mountain(222, 94, 11),
                mountain(590, 52, 15), mountain(612, 48, 18), mountain(636, 52, 14));

        // florestas em África e na Europa, palmeiras na Índia
        double[][] trees = {{290, 225}, {315, 240}, {345, 228}, {300, 265}, {335, 285},
                {360, 260}, {318, 320}, {250, 210}, {160, 40}, {185, 50}, {330, 15}};
        for (double[] t : trees) {
            map.getChildren().add(tree(t[0], t[1]));
        }
        map.getChildren().addAll(palm(625, 110), palm(650, 95), palm(640, 140));
    }

    private static Polygon desert(double... points) {
        Polygon p = new Polygon(points);
        p.setFill(DESERT);
        return p;
    }

    private static Ellipse island(double x, double y, double rx, double ry) {
        Ellipse e = new Ellipse(x, y, rx, ry);
        e.setFill(LAND);
        e.setStroke(BEACH);
        e.setStrokeWidth(2);
        return e;
    }

    private static Group mountain(double x, double y, double size) {
        Polygon rock = new Polygon(x - size, y + size * 0.6, x, y - size * 0.6, x + size, y + size * 0.6);
        rock.setFill(Color.web("#9a7b5f"));
        Polygon snow = new Polygon(x - size * 0.33, y - size * 0.2, x, y - size * 0.6, x + size * 0.33, y - size * 0.2);
        snow.setFill(Color.WHITE);
        return new Group(rock, snow);
    }

    private static Group tree(double x, double y) {
        Rectangle trunk = new Rectangle(x - 1.5, y, 3, 6);
        trunk.setFill(WOOD);
        Circle top = new Circle(x, y - 2, 6, FOREST);
        return new Group(trunk, top);
    }

    private static Group palm(double x, double y) {
        Path trunk = new Path(new MoveTo(x, y + 10), new QuadCurveTo(x - 3, y + 3, x, y - 4));
        trunk.setStroke(WOOD);
        trunk.setStrokeWidth(2.5);
        Group palm = new Group(trunk);
        for (int i = 0; i < 5; i++) {
            Ellipse leaf = new Ellipse(x, y - 4, 8, 2.5);
            leaf.setFill(FOREST);
            leaf.setRotate(-30 + i * 45);
            leaf.setTranslateX(Math.cos(Math.toRadians(-30 + i * 45 + 180)) * -5);
            palm.getChildren().add(leaf);
        }
        return palm;
    }

    /** Rosa dos ventos azul e dourada, num canto do oceano. */
    private static void drawCompassRose(Pane map, double cx, double cy) {
        Circle back = new Circle(cx, cy, 36, Color.web("#ffffff", 0.75));
        back.setStroke(NAVY);
        back.setStrokeWidth(2);
        Polygon big = new Polygon(
                cx, cy - 32, cx + 6, cy - 6, cx + 32, cy, cx + 6, cy + 6,
                cx, cy + 32, cx - 6, cy + 6, cx - 32, cy, cx - 6, cy - 6);
        big.setFill(NAVY);
        Polygon small = new Polygon(
                cx + 16, cy - 16, cx + 4, cy, cx + 16, cy + 16, cx, cy + 4,
                cx - 16, cy + 16, cx - 4, cy, cx - 16, cy - 16, cx, cy - 4);
        small.setFill(GOLD);
        Circle center = new Circle(cx, cy, 3, Color.WHITE);
        Text north = new Text(cx - 5, cy - 40, "N");
        north.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        north.setFill(Color.WHITE);
        map.getChildren().addAll(back, small, big, center, north);
    }

    /** Desenha um ponto da rota com uma etiqueta branca com o seu nome. */
    private static Circle drawMarker(Pane map, Point2D at, String name, double labelX, double labelY,
                                     boolean start) {
        Circle dot = new Circle(at.getX(), at.getY(), 6, start ? CORAL : Color.WHITE);
        dot.setStroke(start ? Color.WHITE : NAVY);
        dot.setStrokeWidth(2);
        Label label = new Label(name);
        label.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        label.setTextFill(NAVY);
        label.setStyle("-fx-background-color: rgba(255,255,255,0.88); -fx-background-radius: 10;"
                + "-fx-padding: 1 7 1 7;");
        label.setLayoutX(labelX);
        label.setLayoutY(labelY);
        map.getChildren().addAll(dot, label);
        return dot;
    }

    /** Constrói a rota desde Lisboa até ao destino indicado, com curvas suaves. */
    private static Path buildRoute(int destinationIndex) {
        List<Point2D> points = routePoints(destinationIndex);
        Path path = new Path(new MoveTo(points.get(0).getX(), points.get(0).getY()));
        // curvas de Catmull-Rom convertidas em curvas de Bézier, para a rota não ter "esquinas"
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p0 = points.get(Math.max(i - 1, 0));
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);
            Point2D p3 = points.get(Math.min(i + 2, points.size() - 1));
            Point2D c1 = p1.add(p2.subtract(p0).multiply(1.0 / 6));
            Point2D c2 = p2.subtract(p3.subtract(p1).multiply(1.0 / 6));
            path.getElements().add(new CubicCurveTo(c1.getX(), c1.getY(), c2.getX(), c2.getY(),
                    p2.getX(), p2.getY()));
        }
        path.setFill(null);
        return path;
    }

    private static List<Point2D> routePoints(int destinationIndex) {
        List<Point2D> points = new ArrayList<>();
        points.add(LISBOA);
        for (int i = 0; i <= destinationIndex; i++) {
            for (double[] p : LEGS[i]) {
                points.add(new Point2D(p[0], p[1]));
            }
        }
        return points;
    }

    /** Comprimento aproximado da rota (para a animação da linha da rota). */
    private static double approximateLength(int destinationIndex) {
        List<Point2D> points = routePoints(destinationIndex);
        double length = 0;
        for (int i = 1; i < points.size(); i++) {
            length += points.get(i).distance(points.get(i - 1));
        }
        return length * 1.1;
    }

    // ========================================================= a caravela
    /** Desenha uma caravela com a Cruz de Cristo na vela. */
    private static Group buildCaravel() {
        Polygon hull = new Polygon(-17, 0, 17, 0, 12, 8, -12, 8);
        hull.setFill(WOOD);
        hull.setStroke(Color.web("#5a3a1a"));
        Polygon stern = new Polygon(-17, 0, -19, -6, -11, 0);
        stern.setFill(WOOD);
        Line mast = new Line(0, 0, 0, -32);
        mast.setStroke(Color.web("#5a3a1a"));
        mast.setStrokeWidth(2);
        Polygon sail = new Polygon(-10, -26, 10, -26, 12, -8, -12, -8);
        sail.setFill(Color.WHITE);
        sail.setStroke(Color.web("#c9b48a"));
        Rectangle crossV = new Rectangle(-1.5, -22, 3, 11);
        crossV.setFill(Color.web("#d62828"));
        Rectangle crossH = new Rectangle(-5.5, -18, 11, 3);
        crossH.setFill(Color.web("#d62828"));
        Polygon lateen = new Polygon(-13, -3, -13, -19, -5, -4);
        lateen.setFill(Color.WHITE);
        lateen.setStroke(Color.web("#c9b48a"));
        Polygon flag = new Polygon(0, -32, 8, -35, 0, -38);
        flag.setFill(Color.web("#d62828"));
        Line flagPole = new Line(0, -32, 0, -38);
        flagPole.setStroke(Color.web("#5a3a1a"));
        Ellipse foam = new Ellipse(0, 9, 19, 3);
        foam.setFill(Color.web("#ffffff", 0.8));

        Group ship = new Group(foam, lateen, mast, flagPole, flag, sail, crossV, crossH, stern, hull);
        ship.setScaleX(1.35);
        ship.setScaleY(1.35);
        return new Group(ship);   // o grupo de fora move-se; o de dentro baloiça
    }

    /** Celebração quando se chega à Índia: estrelas douradas e vermelhas a cintilar. */
    private static void celebrate(Pane map, Circle india) {
        for (int i = 0; i < 16; i++) {
            double angle = Math.toRadians(i * (360.0 / 16));
            double distance = 28 + (i % 3) * 14;
            Polygon sparkle = star(india.getCenterX() + Math.cos(angle) * distance,
                    india.getCenterY() + Math.sin(angle) * distance, 6 + (i % 3) * 3);
            sparkle.setFill(i % 2 == 0 ? GOLD : CORAL);
            sparkle.setStroke(Color.WHITE);
            sparkle.setOpacity(0);
            map.getChildren().add(sparkle);

            FadeTransition fade = new FadeTransition(Duration.millis(600), sparkle);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setAutoReverse(true);
            fade.setCycleCount(6);
            fade.setDelay(Duration.millis(i * 60));
            ScaleTransition grow = new ScaleTransition(Duration.millis(600), sparkle);
            grow.setFromX(0.3);
            grow.setFromY(0.3);
            grow.setToX(1.3);
            grow.setToY(1.3);
            grow.setAutoReverse(true);
            grow.setCycleCount(6);
            grow.setDelay(Duration.millis(i * 60));
            new ParallelTransition(fade, grow).play();
        }
    }

    /** Uma estrela de quatro pontas, centrada em (x, y). */
    private static Polygon star(double x, double y, double r) {
        double s = r * 0.3;
        return new Polygon(x, y - r, x + s, y - s, x + r, y, x + s, y + s,
                x, y + r, x - s, y + s, x - r, y, x - s, y - s);
    }

    // ================================================ estatísticas e top 5
    private static VBox statCard(String name, String value, Color color) {
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        valueLabel.setTextFill(color);
        Label nameLabel = new Label(name.toUpperCase());
        nameLabel.setFont(Font.font("Georgia", 10));
        nameLabel.setTextFill(NAVY);
        VBox card = new VBox(2, valueLabel, nameLabel);
        card.setAlignment(Pos.CENTER);
        card.setMinWidth(92);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;"
                + "-fx-border-color: #bfe3f7; -fx-border-radius: 12; -fx-border-width: 2;");
        return card;
    }

    private static VBox buildTopTable(RankingBoard ranking, int position) {
        Label heading = new Label("Grandes Navegadores");
        heading.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        heading.setTextFill(NAVY);

        GridPane table = new GridPane();
        table.setHgap(12);
        table.setVgap(3);
        List<RankingBoard.Entry> top = ranking.getTop(RankingBoard.TOP_SIZE);
        for (int i = 0; i < top.size(); i++) {
            RankingBoard.Entry e = top.get(i);
            boolean isPlayer = (i + 1 == position);
            Node[] cells = {
                    cell((i + 1) + ".", isPlayer), cell(e.name(), isPlayer),
                    cell(e.destination(), isPlayer), cell(String.valueOf(e.points()), isPlayer)
            };
            for (int c = 0; c < cells.length; c++) {
                table.add(cells[c], c, i);
            }
        }

        VBox box = new VBox(6, heading, table);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setMinWidth(330);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12;"
                + "-fx-border-color: #bfe3f7; -fx-border-radius: 12; -fx-border-width: 2;");
        return box;
    }

    private static Label cell(String text, boolean highlight) {
        Label label = new Label(text);
        label.setFont(Font.font("Georgia", highlight ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        label.setTextFill(highlight ? CORAL : NAVY);
        return label;
    }

    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }
}
