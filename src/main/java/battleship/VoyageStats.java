package battleship;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;

/**
 * Acompanha o desempenho do jogador numa partida e calcula até onde chegou
 * a sua viagem na Rota da Índia (issue #6).
 *
 * <p>A pontuação vai de 0 a 1000 e tem três componentes:</p>
 * <ul>
 *   <li><b>Pontaria</b> (até 400): percentagem de tiros válidos que acertaram num navio;</li>
 *   <li><b>Eficiência</b> (até 400): quantas rajadas foram precisas, comparado com o mínimo possível (9);</li>
 *   <li><b>Rapidez</b> (até 200): tempo total da partida.</li>
 * </ul>
 */
public class VoyageStats {

    /**
     * Destinos da Rota da Índia, por ordem de chegada.
     * Cada destino é alcançado a partir de uma pontuação mínima.
     */
    public enum Destination {
        CEUTA("Ceuta", 0),
        MADEIRA("Madeira", 280),
        CABO_BOJADOR("Cabo Bojador", 400),
        BOA_ESPERANCA("Cabo da Boa Esperança", 500),
        INDIA("Índia", 600);

        private final String displayName;
        private final int minPoints;

        Destination(String displayName, int minPoints) {
            this.displayName = displayName;
            this.minPoints = minPoints;
        }

        /** @return o nome do destino tal como aparece ao jogador */
        public String getDisplayName() {
            return displayName;
        }

        /** @return a pontuação mínima para alcançar este destino */
        public int getMinPoints() {
            return minPoints;
        }

        /**
         * Devolve o destino mais distante que a pontuação permite alcançar.
         *
         * @param points a pontuação da partida
         * @return o destino alcançado
         */
        public static Destination fromPoints(int points) {
            Destination reached = CEUTA;
            for (Destination d : values()) {
                if (points >= d.minPoints) {
                    reached = d;
                }
            }
            return reached;
        }
    }

    // Pesos de cada componente da pontuação (somam 1000)
    private static final int MAX_ACCURACY_POINTS = 400;
    private static final int MAX_EFFICIENCY_POINTS = 400;
    private static final int MAX_SPEED_POINTS = 200;

    // A frota ocupa 25 posições: o melhor jogo possível são 9 rajadas (27 tiros)
    private static final int BEST_POSSIBLE_VOLLEYS = 9;

    // Até 2 minutos ganha a rapidez máxima; a partir de 15 minutos não ganha nada
    private static final long FULL_SPEED_MILLIS = 2 * 60 * 1000L;
    private static final long ZERO_SPEED_MILLIS = 15 * 60 * 1000L;

    private final StopWatch stopWatch = new StopWatch();
    private int volleys;
    private int validShots;
    private int hits;

    /**
     * Começa (ou recomeça) a contar o tempo de uma nova partida.
     * Deve ser chamado quando a frota é gerada ou carregada.
     */
    public void start() {
        stopWatch.reset();
        stopWatch.start();
    }

    /**
     * Para o cronómetro e guarda os números finais da partida.
     * Deve ser chamado quando o último navio é afundado.
     *
     * @param game a partida que terminou
     */
    public void finish(IGame game) {
        if (stopWatch.isStarted()) {
            stopWatch.stop();
        }
        volleys = game.getAlienMoves().size();
        int totalShots = volleys * Game.NUMBER_SHOTS;
        validShots = totalShots - game.getInvalidShots() - game.getRepeatedShots();
        hits = game.getHits();
    }

    /** @return o número de rajadas disparadas */
    public int getVolleys() {
        return volleys;
    }

    /** @return a pontaria, entre 0.0 e 1.0 (tiros certeiros / tiros válidos) */
    public double getAccuracy() {
        return validShots == 0 ? 0.0 : (double) hits / validShots;
    }

    /** @return o tempo da partida em milissegundos */
    public long getElapsedMillis() {
        return stopWatch.getTime();
    }

    /** @return o tempo da partida formatado, por exemplo "3m 42s" */
    public String getFormattedTime() {
        return DurationFormatUtils.formatDuration(getElapsedMillis(), "m'm' ss's'");
    }

    /** @return os pontos de pontaria (0 a 400) */
    public int getAccuracyPoints() {
        return (int) Math.round(getAccuracy() * MAX_ACCURACY_POINTS);
    }

    /** @return os pontos de eficiência (0 a 400); 9 rajadas dão o máximo */
    public int getEfficiencyPoints() {
        if (volleys == 0) {
            return 0;
        }
        double ratio = (double) BEST_POSSIBLE_VOLLEYS / volleys;
        return (int) Math.round(Math.min(1.0, ratio) * MAX_EFFICIENCY_POINTS);
    }

    /** @return os pontos de rapidez (0 a 200), a descer linearmente entre 2 e 15 minutos */
    public int getSpeedPoints() {
        long t = getElapsedMillis();
        if (t <= FULL_SPEED_MILLIS) {
            return MAX_SPEED_POINTS;
        }
        if (t >= ZERO_SPEED_MILLIS) {
            return 0;
        }
        double remaining = (double) (ZERO_SPEED_MILLIS - t) / (ZERO_SPEED_MILLIS - FULL_SPEED_MILLIS);
        return (int) Math.round(remaining * MAX_SPEED_POINTS);
    }

    /** @return a pontuação total da partida (0 a 1000) */
    public int getPoints() {
        return getAccuracyPoints() + getEfficiencyPoints() + getSpeedPoints();
    }

    /** @return o destino alcançado na Rota da Índia */
    public Destination getDestination() {
        return Destination.fromPoints(getPoints());
    }

    /**
     * Mostra no terminal o resumo da viagem.
     * (Mais tarde, este resumo passa a aparecer numa janela com a caravela animada.)
     */
    public void printSummary() {
        System.out.println();
        System.out.println("+------------------- ROTA DA ÍNDIA -------------------+");
        System.out.printf("  Chegaste a: %s!%n", getDestination().getDisplayName());
        System.out.printf("  Tempo: %s   Rajadas: %d   Pontaria: %d%%%n",
                getFormattedTime(), getVolleys(), Math.round(getAccuracy() * 100));
        System.out.printf("  Pontos: %d  (pontaria %d + eficiência %d + rapidez %d)%n",
                getPoints(), getAccuracyPoints(), getEfficiencyPoints(), getSpeedPoints());
        System.out.println("+-----------------------------------------------------+");
    }
}